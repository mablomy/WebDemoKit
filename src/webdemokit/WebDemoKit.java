/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;


/**
 *
 * @author testy
 */
public class WebDemoKit implements HttpHandler {

    static String VERSION="Mario's WebDemoKit v 8.0.19 DMR";
    protected int httpPort = 8000;
    ArrayList<Element> elementlist;
    ConnectionList connlist;
    HttpServer server;

    
    public static void main(String[] args) {
        try {
            WebDemoKit myapp = new WebDemoKit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
            while(true) {
            try {Thread.sleep(1000);} catch(Exception e) {System.out.println("Sleep interrupted");}
//            myapp.setState();
        }
    }

    public WebDemoKit () throws Exception {
    System.out.println(VERSION);
    
    FileServer fileServer = new FileServer();
    
    // Setup JDBC driver
        try {
//            Class.forName("com.mysql.fabric.jdbc.FabricMySQLDriver");
        } catch (Exception ex) {
            System.out.println("JDBC driver could not be loaded."+ex);
            System.exit(-2);
        }

        // Setup http server
        server = HttpServer.create(new InetSocketAddress(httpPort), 20);
        server.createContext("/getStatus", this);
        server.createContext("/getStarted", this);
        server.createContext("/", fileServer);
        
        server.setExecutor(null); // creates a default executor
        server.start();

    }
  

    public void setState() {
//        for (int i=0; i<elementlist.size(); ++i) {
//            elementlist.get(i).setGUIState();
//        }
    }
  
    
    public void handle(HttpExchange t) throws IOException {
        
        switch (t.getRequestURI().getPath()) {
            case "/getStarted": getStarted(t); break;
            case "/getStatus": getStatus(t); break;
            default:
                System.out.println("Illegal REST call of URI "+t.getRequestURI().getPath());
                t.getResponseHeaders().set("Content-Type","application/json");
                t.sendResponseHeaders(404, 16);
                OutputStream os = t.getResponseBody();
                os.write(new String("{\"result\":false}").getBytes());
                os.close();
        }
    }
    
    public void getStarted(HttpExchange t) throws IOException {
        String result;
        
        if (elementlist != null) {
            System.out.println("Removing existing elements...");
            for (int i=0; i<elementlist.size(); ++i) 
                elementlist.get(i).terminate();
        }
        
        elementlist = new ArrayList<Element>();
        connlist = new ConnectionList();
        System.out.println("Loading demokit.cfg file...");
        load();
        
        // Start the Element threads
        for (int i=0; i<elementlist.size(); ++i) {
            elementlist.get(i).start();
        }
        for (int i=0; i<elementlist.size(); ++i) {
            // let elements register for REST endpoints
            if (elementlist.get(i).getURL().length() > 0) {
                server.createContext(elementlist.get(i).getURL(), elementlist.get(i));
            }
        }
        result = "{\"result\":true}";
        t.getRequestBody().close();
        t.getResponseHeaders().set("Content-Type","application/json");
        t.sendResponseHeaders(200, result.length());
        OutputStream os = t.getResponseBody();
        os.write(result.getBytes());
        os.close();
    }
    
    public void getStatus(HttpExchange t) throws IOException {
        String result;
        
        t.getRequestBody().close();
        result = "{\"elements\":[";
        if (elementlist.size() == 0) 
            result += "],";
        else {
            for (int i=0; i<elementlist.size()-1; ++i) 
                result += elementlist.get(i).toJson()+",";
            result += elementlist.get(elementlist.size()-1).toJson() + "],";
        }    
        result += "\"connections\":[";
        if (connlist.size() == 0)
            result += "]}";
        else {
            for (int i=0; i<connlist.size()-1; ++i) 
                result += connlist.get(i).toJson()+",";
            result += connlist.get(connlist.size()-1).toJson() + "]}";
        }           
        t.getResponseHeaders().set("Content-Type","application/json");
        t.sendResponseHeaders(200, result.length());
        OutputStream os = t.getResponseBody();
        os.write(result.getBytes());
        os.close();
// System.out.println("Served Status:\n"+result); 
    }

  
    private void load() {

        String line="";
        String items[];
        int id;

        try {
            BufferedReader input = new BufferedReader(new FileReader("demokit.cfg"));
            for(;;) {
                line = input.readLine();
                if (line.length()==0) continue;
                if (line.charAt(0) != '#') {
                    items = line.split("\\|");
                    id = Integer.valueOf(items[0]);
                    if (items[1].compareTo("HttpPort")==0) {
                        if(items.length != 3) {
                            System.out.println("Error in line: "+line);
                        } else {
                            httpPort=Integer.valueOf(items[2]);
                        }
                    } else if (items[1].compareTo("SQLNode")==0) {
                        if(items.length != 5) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new SQLNode(elementlist, connlist,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    items[4]));
                        }
                    } else if (items[1].compareTo("GroupReplication")==0) {
                        if(items.length != 6) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new GroupReplication(elementlist, connlist,
                                                    Integer.valueOf(items[2]), // x Pos
                                                    Integer.valueOf(items[3]), // y Pos
                                                    items[4], // List of host:port tuples, comma separated
                                                    items[5]));  // trailer of JDBC connectstring
                        }
/*                    } else if (items[1].compareTo("MgmtNode")==0) {
                        if(items.length != 7) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new MgmtNode(elementlist, panel,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    Integer.valueOf(items[4]),
                                                    items[5],
                                                    items[6]));
                        }
                    } else if (items[1].compareTo("DataNode")==0) {
                        if(items.length != 7) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new DataNode(elementlist, panel,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    Integer.valueOf(items[4]),
                                                    items[5],
                                                    items[6]));
                        } */
                    } else if (items[1].compareTo("LoadGenerator")==0) {
                       if(items.length != 7) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new LoadGenerator(elementlist, connlist,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    items[4],
                                                    Integer.valueOf(items[5]),
                                                    Integer.valueOf(items[6]),
                                                    "dummyAction"));
                        }
/*                    } else if (items[1].compareTo("MaxLoadGen")==0) {
                       if(items.length != 6) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new MaxLoadGen(elementlist, panel,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    items[4],
                                                    Integer.valueOf(items[5]),
                                                    "dummyAction"));
                        }
                    } else if (items[1].compareTo("LoadVisualizer")==0) {
                        if(items.length != 5) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new LoadVisualizer(elementlist, panel,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    items[4]));
                        } */
                    } else if (items[1].compareTo("TpsMeter")==0) {
                        if(items.length != 5) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new TpsMeter(elementlist, connlist,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    items[4]));
                        } /*
                    } else if (items[1].compareTo("LogViewer")==0) {
                        if(items.length != 7) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new LogViewer(elementlist, panel,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    Integer.valueOf(items[4]),
                                                    Integer.valueOf(items[5]),
                                                    items[6]));
                        }
*/
/*                    } else if (items[1].compareTo("Connection")==0) {
                        if (items.length != 4) {
                            System.out.println("Error in line: "+line);
                        } else {
                            int from = Integer.valueOf(items[2]);
                            int to   = Integer.valueOf(items[3]);
                            panel.addConnection(new ConnectionItem(elementlist.get(from), elementlist.get(to)));
                        }
                    } else if (items[1].compareTo("Image")==0) {
                        if (items.length != 5) {
                            System.out.println("Error in line: "+line);
/                       } else {
                            elementlist.add(new Image(elementlist, panel,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    items[4]));
                        } */
                    } else if (items[1].compareTo("Router")==0) {
                        if(items.length != 6) {
                            System.out.println("Error in line: "+line);
                        } else {
                            elementlist.add(new Router(elementlist, connlist,
                                                    Integer.valueOf(items[2]),
                                                    Integer.valueOf(items[3]),
                                                    items[4],
                                                    items[5]));
                        }
                    } else {
                        System.out.println ("Could not parse line in demokit.cfg: "+line);
                    }
                }
            }
        } catch (Exception e) {
            if (line != null)
                System.out.println("Exception in load:"+e+"\nwhile working on line:"+line);
        }
    }

    private void save() {
/*
        int i;
        String temp;
        try {
            FileWriter file = new FileWriter("demokit.cfg");
            String comment = "# Configuration file for DemoKit\n#\n# Element section\n"+
                             "# The following formats are supported:\n"+
                             "# <id>|SQLnode|<xPos>|<yPos>|<JDBC Connectstring>\n"+
                             "# <id>|GroupReplication|<xPos>|<yPos>|<host:port,host:port,host:port,...>|<JDBC connectstring trailer>\n" +
                             "# <id>|Router|<xPos>|<yPos>|<REST Connectstring>|<REST_user:pass>\n"+
                             "# <id>|MgmtNode|<xPos>|<yPos>|<nodeID>|<hostname>|<ndb connectstring>\n"+
                             "# <id>|DataNode|<xPos>|<yPos>|<nodeID>|<hostname>|<ndb connectstring>\n"+
                             "# <id>|LoadGenerator|<xPos>|<yPos>|<JDBC connectstring>|<owner(use ID again)>|<color>\n"+
                             "# <id>|MaxLoadGen|<xPos>|<yPos>|<JDBC connectstring>|<owner(use ID again)>\n"+
                             "# <id>|LoadVisualizer|<xPos>|<yPos>|<JDBC connectstring>\n"+
                             "# <id>|TpsMeter|<xPos>|<yPos>|<JDBC connectstring>\n"+
                             "# <id>|LogViewer|<xPos>|<yPos>|<cols>|<rows>|filename\n"+
                             "# CAVE: ID must begin with 0 and increment without missing ids!\n#\n";
            file.write(comment,0,comment.length());
            for (i=0; i<elementlist.size(); ++i) {
                temp = elementlist.get(i).toString();
                file.write(temp,0,temp.length());
                file.write("\n",0,1);
            }
            comment = "#\n# Connection section\n"+
                      "# Format is <id>|Connection|<from id>|<to id>\n"+
                      "# <id> is irrelevant.\n#\n";
            file.write(comment,0,comment.length());
            temp = panel.toString();
            file.write(temp,0,temp.length());

            file.close();
            
        } catch (Exception e) {
            System.out.println("Exception in save:"+e);
        }
*/
    }


}
