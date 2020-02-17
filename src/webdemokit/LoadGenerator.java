/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package webdemokit;

/**
 *
 * @author mario
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.ArrayList;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class LoadGenerator extends Element implements HttpHandler {

    Connection conn;
    Statement sqlcmd;
    int owner;
    int color;
    int waittime=0;
    String connectstring;
    boolean connected = false;
    boolean running=false;
    ResultSet data;
    ConnectionList panel;
    Element connTarget;
    String URL;

    public String toString() {
        return (uid+"|LoadGenerator|"+x+"|"+y+"|"+connectstring+"|"+owner+"|"+color);
    }

    @Override public String getIdString() {return (URL);}
    @Override public String getURL() {return ("/"+URL);}
    
    public LoadGenerator(ArrayList<Element> sibs, ConnectionList panel,int x,int y,String connstring,int owner,int color,String action) {

        super(sibs,x,y,40,40);

/*        switch (color) {
            case 1: filename="pen_black.png"; break;
            case 2: filename="pen_blue.png"; break;
            case 5: filename="pen_green.png"; break;
            case 9: filename="pen_red.png";break;
            default: filename=""; break;
        } */
        connectstring = connstring;
        this.owner=owner;
        this.color=color;
        this.panel = panel;
        this.URL = "load_generator_"+uid;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
//        System.out.println("Es geht los mit dem Button!!!");
        running=!running;
        String result;
        if (running) 
            result = "{\"result\":true}";
        else
            result = "{\"result\":false}";

        t.getRequestBody().close();
        t.getResponseHeaders().set("Content-Type","application/json");
        t.sendResponseHeaders(200, result.length());
        OutputStream os = t.getResponseBody();
        os.write(result.getBytes());
        os.close();
    }

    @Override
    public String toJson() {
        return ("{\"id\":\""+getIdString()+"\","+
                "\"ObjType\":\"LoadGenerator\"," +
                "\"xPos\":"+x+"," +
                "\"yPos\":"+y+"," +
                "\"color\":"+color+"," +
                "\"running\":"+running+"," +
                "\"url\":\""+URL+"\"}");
    }
    

    public void findConnections() {
        // identify and register the connection line
        int i=0;
        while (i < siblings.size()) {
            connTarget=siblings.get(i).whoIs(connectstring);
            if (connTarget != null) {
System.out.println("LoadGenerator "+color+" found it's partner: id="+i+"; string="+connTarget.getIdString());
                panel.add(new ConnectionItem(ConnectionItem.CLIENT, this, connTarget));
                break;
            }
            i++;
        }
System.out.println("LoadGenerator "+color+" found no partner... so sad...");
    }


    public void run() {

        int i, erg, counter, dx1, dx2, dy1, dy2, x1=50, x2=100, y1=50, y2=100;
        Random rnd=new Random();
        while (isRunning) {
            // Identify connections
            
            while (!running) {
                if (connTarget == null) findConnections();
                // Wating for someone to click the start button
                try{Thread.sleep(100);}
                catch(InterruptedException e){System.out.println("LoadGenerator: Sleep 1 Interrupted");}
            }
            dx1=rnd.nextInt(4)-7;
            dx2=rnd.nextInt(4)-7;
            dy1=rnd.nextInt(4)-7;
            dy2=rnd.nextInt(4)-7;
            for (i=0; i < 100; ++i) {
                // Close connection if we just stopped running
                try {
                    if (!running && connected) {
                        conn.close();
                        connected=false;
                    }
                } catch (SQLException e) {
                    System.out.println ("SQL Exception in LoadGenerator while closing connection");
                    e.printStackTrace();
                }

                while (!running) {
                    if (connTarget == null) findConnections();
                    // Wating for someone to call this.startLoad()
                    try{Thread.sleep(100);}
                    catch(InterruptedException e){System.out.println("LoadGenerator: Sleep 2 Interrupted");}
                }
                x1+=dx1; if ((x1<0) || (x1>127)) {dx1=-dx1; x1+=dx1;}
                x2+=dx2; if ((x2<0) || (x2>127)) {dx2=-dx2; x2+=dx2;}
                y1+=dy1; if ((y1<0) || (y1>127)) {dy1=-dy1; y1+=dy1;}
                y2+=dy2; if ((y2<0) || (y2>127)) {dy2=-dy2; y2+=dy2;}

                try {
                    if (!connected) {
                        try{Thread.sleep(1000);}  // Wait 1s before reconnecting
                        catch(InterruptedException e){System.out.println("Sleep Interrupted");}
                        conn = DriverManager.getConnection(connectstring);
                        sqlcmd = conn.createStatement(); //ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                        connected=true;
//                        System.out.println("Reconnected after "+waittime+" seconds");
                        waittime=0;
                    }
                    erg = sqlcmd.executeUpdate("INSERT INTO mariosdata.line set " +
                        "  x1="+x1+
                        ", y1="+y1+
                        ", x2="+x2+
                        ", y2="+y2+
                       ", color="+color+
                        ", owner="+owner);
                    data = sqlcmd.executeQuery("SELECT COUNT(*) as count FROM mariosdata.line WHERE owner="+owner);
                    data.next();
                    counter=data.getInt("count");
                    if (counter>20) {
                      erg = sqlcmd.executeUpdate("DELETE FROM mariosdata.line WHERE owner="+owner+" ORDER BY id ASC LIMIT "+(counter-20));
                    }
                    try{Thread.sleep(100);}
                    catch(InterruptedException e){System.out.println("Sleep Interrupted");}

                } catch (CommunicationsException ex) { 
                    System.out.println("LoadGenerator: Connection Communication Error");
                    connected=false; 
                    running=false;
                    try {if(conn!=null) conn.close();} catch(SQLException ex2){System.out.println("LoadGenerator: Error in closing connection during exception");}            
                } catch (SQLException ex) {
                    if (ex.getErrorCode()==1146 || ex.getErrorCode()==1049) {  // Table mariosdata.lines does not exist 8.0/5.7
                        System.out.println("LoadGenerator: Seems that demo schema is not yet installed. Will do now");
                        this.installDemoSchema();
                        connected = false; 
                        try {conn.close();} catch(SQLException ex2){System.out.println("LoadGenerator: Error in closing connection during exception");}                        
                    } else {
                        System.out.println("LoadGenerator<" + color + ">: SQL Error in database connection<" + connectstring + ">");
                        System.out.println(ex);
                        System.out.println("Reconnecting...");
                        connected = false;
                        running = false;
                        try {conn.close();} catch(SQLException ex2){System.out.println("LoadGenerator: Error in closing connection during exception");}
//                    exceptionHandler(ex);
                    }
                } catch (Exception ex) {
                    System.out.println("LoadGenerator<" + color + ">: Error in database connection<" + connectstring + ">");
                    System.out.println(ex);
                    ex.printStackTrace();
                }
            }
        }
        // Thread is being stopped (isRunning==false)
        try {
            if (conn!=null && !conn.isClosed())
                conn.close();
        } catch (Exception e) {
            System.out.println("Exception when closing connection in LoadGenerator.run()");
            System.out.println(e);
        }
//        System.out.println("LoadGenerator "+hostname+" is shut down.");

    }

    private void installDemoSchema() {
           
        String cmds[]={
                "DROP DATABASE IF EXISTS `mariosdata`",
                "CREATE DATABASE `mariosdata`",
                "USE `mariosdata`",
                "CREATE TABLE `line` ("+
                    "`x1` tinyint(3) unsigned NOT NULL,"+
                    "`y1` tinyint(3) unsigned NOT NULL,"+
                    "`x2` tinyint(3) unsigned NOT NULL,"+
                    "`y2` tinyint(3) unsigned NOT NULL,"+
                    "`color` enum('BLACK','BLUE','CYAN','GRAY','GREEN',"+
                    "'MAGENTA','ORANGE','PINK','RED','WHITE','YELLOW') default 'BLACK',"+
                    "`owner` tinyint(3) unsigned default NULL,"+
                    "`id` int(10) unsigned NOT NULL auto_increment,"+
                    "PRIMARY KEY  (`id`), KEY `owner` (`owner`)"+
                    ") AUTO_INCREMENT=9 DEFAULT CHARSET=latin1",
                "CREATE TABLE maxload (i INT PRIMARY KEY AUTO_INCREMENT, a INT, b VARCHAR(30), c FLOAT)"
          };
        try {
            Statement select = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int i;
            for (i = 0; i < cmds.length; ++i)
                select.executeUpdate(cmds[i]);
            conn.close();

        } catch (SQLException ex) {
        }
    }

public void exceptionHandler(SQLException ex) {
        System.out.println(ex);
        if ((ex.getSQLState().compareTo("08S01")==0) || (ex.getSQLState().compareTo("08003")==0)) {
            if (connected) {
//                            System.out.println("SQLException: " + ex.getMessage());
//                            System.out.println("Trying to reconnect");
                connected=false;
                waittime=1;
            } else {
                waittime++;
                try{Thread.sleep(1000);}
                catch(InterruptedException e){System.out.println("Sleep Interrupted");}
            }
        } else if (ex.getSQLState().compareTo("HY000")==0) { // Transaction aborted due to node down: ignore
            connected=true;
        } else if (ex.getSQLState().compareTo("42S02")==0) {
            // means that the schema is not yet available.
            // Message to user and ignore the error
            running=false;
        } else {
            System.out.println("Database error occured in LoadGenerator color="+color+":");
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            System.exit(-3);
        }
        try{Thread.sleep(100);}
        catch(InterruptedException e){System.out.println("Sleep Interrupted");}
    }
}
