/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.util.Base64;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Mario
 */
public class Router extends Element {
    
    public static final int NOT_CONNECTED=1;
    public static final int CONNECTED=2;
    private String connString;
    private String authHeaderValue;
    protected int state=NOT_CONNECTED;
    
    private RouterStatus status = new RouterStatus();
    private URL rest_status, rest_ro, rest_rw;
    private Gson gson = new Gson();
    private RouterConnections cxn_ro = new RouterConnections();
    private RouterConnections cxn_rw = new RouterConnections(); 
    private RouterConnections cxn_x_rw = new RouterConnections();
    private RouterConnections cxn_x_ro = new RouterConnections();
    private String metadata;
    private int port_rw, port_ro, port_x_rw, port_x_ro;
    
    @Override public String getIdString() {
        return ("router_"+uid);
    }
    
    @Override public String toString() {
        return (uid+"|Router|"+x+"|"+y+"|"+connString);
    }

    @Override public String toJson() {
        return "{ \"id\":\""+getIdString()+"\"," +
                "\"ObjType\": \"router\"," +
                "\"xPos\":"+x+"," +
                "\"yPos\":"+y+"," +
                "\"connected\":"+(state==CONNECTED)+"," +
                "\"hostname\":\""+status.getHostname()+"\"," +
                "\"version\":\""+status.getVersion()+"\"," +
                "\"cxnstatus\":\""+getCxnSummary()+"\"," +
                "\"routerstatus\":\""+getCxnStatus()+"\"}";
    }
    
    
    @Override public Element whoIs(String connstr) {
        if (connstr.startsWith("jdbc:mysql://"+status.getHostname()+":"+port_rw+"/"))
            return this;
        else if (connstr.startsWith("jdbc:mysql://"+status.getHostname()+":"+port_ro+"/"))
            return this;
        else if (connstr.startsWith("jdbc:mysql://"+status.getHostname()+":"+port_x_rw+"/"))
            return this;
        else if (connstr.startsWith("jdbc:mysql://"+status.getHostname()+":"+port_x_ro+"/"))
            return this;
        else
            return null;
    }
        
    public Router(ArrayList<Element> sibs, ConnectionList panel, int x, int y, String conn_str, String auth) {
        super(sibs,x,y,60,80);
        // setup REST connection
        connString = conn_str;
        setName("RouterChecker:'"+connString+"'");

        // Router requries http authentication. Here we prepare the header element for later use
        authHeaderValue = "Basic " + new String(Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8)));
	    }

        // Thread routine to check server status
    public void run() {
        while(isRunning) {
            checkRouter();
            try {Thread.sleep(1000);} catch(Exception e) {System.out.println("Sleep interrupted");}
        }
        //System.out.println("RouterChecker "+hostname+" is shut down.");
    }


    protected void checkRouter() {
        
        try {
            
            // Check router status only once. If Hostname and Version have been set before, skip the next section
//            if (state == NOT_CONNECTED) {
//            } else {
                // Retrieve current connection statistics
                getRouterStatus();
                getMetadataName();              
                port_rw = getInPort(connString+"/routes/"+metadata+"_rw/config");
                port_ro = getInPort(connString+"/routes/"+metadata+"_ro/config");
                port_x_rw = getInPort(connString+"/routes/"+metadata+"_x_rw/config");
                port_x_ro = getInPort(connString+"/routes/"+metadata+"_x_ro/config");
                
                cxn_rw = getConnections(connString+"/routes/"+metadata+"_default_rw/connections");
                cxn_ro = getConnections(connString+"/routes/"+metadata+"_default_ro/connections");
                cxn_x_rw = getConnections(connString+"/routes/"+metadata+"_default_x_rw/connections");
                cxn_x_ro = getConnections(connString+"/routes/"+metadata+"_default_x_ro/connections");
  //          }
            
        } catch (ConnectException e) {
            state = NOT_CONNECTED;
            // If Router is unavailable, wait a bit longer before retry
            try {Thread.sleep(1000);} catch(Exception ex) {System.out.println("Sleep in RouterCheck interrupted");}
        } catch (NoRouteToHostException e) {
            state = NOT_CONNECTED;
            // If Router is unavailable, wait a bit longer before retry
            try {Thread.sleep(1000);} catch(Exception ex) {System.out.println("Sleep in RouterCheck interrupted");}
            
        }catch (Exception e) {
            System.out.println("Exception in RouterChecker.checkRouter():");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    int getInPort (String path) throws Exception {
        // reads the port of an incoming channel via REST API
        rest_status = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) rest_status.openConnection();
        conn.setRequestProperty("Authorization", authHeaderValue);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("CheckRouter.getRouterConnections failed for URL "+path+" : " + conn.getResponseCode());
        }
        state = CONNECTED;
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;
        RouterConfig temp = new RouterConfig();
            while ((output = br.readLine()) != null) {
                temp = gson.fromJson(output, RouterConfig.class);
            }
        conn.disconnect();
        return temp.bindPort;       
    }
    
    
    RouterConnections getConnections (String path) throws Exception {
        // reads the list of connections via REST API
        rest_status = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) rest_status.openConnection();
        conn.setRequestProperty("Authorization", authHeaderValue);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("CheckRouter.getRouterConnections failed : HTTP error code : " + conn.getResponseCode());
        }
        state = CONNECTED;
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;
        RouterConnections temp = new RouterConnections();
            while ((output = br.readLine()) != null) {
                temp = gson.fromJson(output, RouterConnections.class);
            }
        conn.disconnect();
        return temp;
    }
    
    
    void getRouterStatus() throws Exception {
        rest_status = new URL(connString+"/router/status");
        HttpURLConnection conn = (HttpURLConnection) rest_status.openConnection();
        conn.setRequestProperty("Authorization", authHeaderValue);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("CheckRouter.getRouterStatus failed : HTTP error code : " + conn.getResponseCode());
        }
        state = CONNECTED;
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;
            while ((output = br.readLine()) != null) {
                status = gson.fromJson(output, RouterStatus.class);
            }
        conn.disconnect();
    }
    
    
    void getMetadataName() throws Exception {
        rest_status = new URL(connString+"/metadata");
        HttpURLConnection conn = (HttpURLConnection) rest_status.openConnection();
        conn.setRequestProperty("Authorization", authHeaderValue);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("CheckRouter.getMetadataName failed : HTTP error code : " + conn.getResponseCode());
        }
        state = CONNECTED;
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String output;
        while ((output = br.readLine()) != null) {
            RouterMetadata temp = gson.fromJson(output, RouterMetadata.class);
            metadata = temp.getMetadataName();
        }
        conn.disconnect();
        
    }
    
    public String getCxnSummary() {
        return ("R/O:  "+(cxn_ro.getConnectionCount()+cxn_x_ro.getConnectionCount())+" cxn<br>" +
                "R/W: "+(cxn_rw.getConnectionCount()+cxn_x_rw.getConnectionCount())+" cxn");
    }
    
    public String getCxnStatus() {
        return ("<b>Default_r/w</b><br>"+cxn_rw.getReport()+"<br>"
              + "<b>Default_r/o</b><br>"+cxn_ro.getReport()+"<br>"
              + "<b>Default_X_r/w</b><br>"+cxn_x_rw.getReport()+"<br>"
              + "<b>Default_X_r/o</b><br>"+cxn_x_ro.getReport()+"<br>");
    }        

}


