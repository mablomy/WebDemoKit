/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;


/**
 *
 * @author mario
 */
public class TpsMeter extends Element {
    public static final int BUFSIZE = 100;
    public int timer;
    ConnectionList panel;
    Element connTarget;
    String connectstring;
    String hostname;
    boolean connected;
    Connection conn;
    Statement select;
    ResultSet data;
    int lasttps;
    int maxtps;
    int waittime;
    String lastSQLerror;
    int[] tps = new int[BUFSIZE];
    int nextpos;

    public String toString() {
        return ("0|TpsMeter|"+x+"|"+y+"|"+connectstring);
    }

    @Override
    public String toJson() {
                    
        String tps_string;
        
        tps_string = Integer.toString(tps[0]);
        for (int i=1; i<BUFSIZE; ++i) {
            tps_string += ","+tps[(i+nextpos-1)%BUFSIZE];
        }
        return ("{\"id\":\""+getIdString()+"\","+
                "\"ObjType\":\"TPSMeter\"," +
                "\"xPos\":"+x+"," +
                "\"yPos\":"+y+"," +
                "\"hostname\":\""+hostname+"\"," +
                "\"connected\":"+connected+"," +
                "\"waittime\":"+waittime+"," +
                "\"lastSqlError\":\""+lastSQLerror+"\"," +
                "\"maxTps\":"+maxtps+"," +
                "\"tps\":["+tps_string+"]}");

    }
    
    @Override
    public String getIdString() {return ("tpsmeter_"+uid);}    
        
    
    public TpsMeter (ArrayList<Element> sibs, ConnectionList connlist, int x, int y, String connstr) {
        super(sibs,x,y,128,128+20);
        lasttps = -1;
        maxtps = 10;
        nextpos = 0;
        lastSQLerror = new String("");
        hostname = new String("");
        connectstring = connstr;
        connected = false;
        this.panel = connlist;

    }

    public void findConnections() {
        if (connTarget == null) {
            // identify and create the connection line
            int i=0;
            while (i < siblings.size()) {
                connTarget=siblings.get(i).whoIs(connectstring);
                if (connTarget != null) {
//System.out.println("TPS Meter found it's partner: id="+i);
                    panel.add(new ConnectionItem(ConnectionItem.CLIENT, this, connTarget));
                    break;
                }
                i++;
            }
        }

    }


    public void run() {
        int t;
        for (;;) {
            try {
                if (!connected) {
                    try{Thread.sleep(1000);}  // Wait 1s before reconnecting
                    catch(InterruptedException e){System.out.println("Sleep Interrupted");}
                    conn = DriverManager.getConnection(connectstring);
                    select = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    connected=true;

                }
                data = select.executeQuery("SELECT LEFT(@@hostname,IF(LOCATE('.',@@hostname)=0,LENGTH(@@hostname),LOCATE('.',@@hostname)-1)) AS Value");
                data.next();
                hostname = data.getString("Value");
                data = select.executeQuery("SHOW VARIABLES LIKE 'port'");
                data.next();
                hostname = hostname + ":" + data.getString("Value");

                data = select.executeQuery("SELECT SUM(count_star) AS 'sum' FROM performance_schema.events_statements_summary_global_by_event_name WHERE event_name LIKE 'statement/sql/%'");
                data.next();
                t = data.getInt("sum");
                if (lasttps == -1) {
                    // This is the first time we read data
                    lasttps = t;
                } else {
                    tps[nextpos] = t - lasttps;
                    lasttps = t;
                    nextpos = (++nextpos) % BUFSIZE;
                }
                maxtps = 10;
                for (int i=0; i < BUFSIZE; ++i) {
                    if (tps[i] > maxtps) {
                        maxtps = tps[i];
                    }
                }


                try{Thread.sleep(1000);}
                catch(InterruptedException e){System.out.println("Sleep Interrupted");}
            } catch (SQLException ex) {
//ex.printStackTrace();
                if ((ex.getSQLState().compareTo("08S01")==0) || (ex.getSQLState().compareTo("08003")==0)) {
                    if (connected) {
                        connected=false;
                        waittime=1;
                    } else {
                        waittime++;
                        hostname="???";
                        try{Thread.sleep(1000);}
                        catch(InterruptedException e){System.out.println("Sleep Interrupted");}
                    }
                } else if (ex.getSQLState().compareTo("HY000")==0) {// Transaction aborted due to node down: ignore
                    connected=true;
                    lastSQLerror=ex.getMessage();
                    try{Thread.sleep(100);}
                    catch(InterruptedException e){System.out.println("Sleep Interrupted");}
                } else if (ex.getSQLState().compareTo("42S02")==0) {
                    // Means that the requested schema does not exist.
                    // we ignore this error. No schema, no data to display
                    try{Thread.sleep(1000);}
                    catch(InterruptedException e){System.out.println("Sleep Interrupted");}
                } else{
                    System.out.println("TPS-Meter: Database connection failed.");
                    System.out.println(connectstring);
                    System.out.println("SQLException: " + ex.getMessage());
                    System.out.println("SQLState: " + ex.getSQLState());
                    System.out.println("VendorError: " + ex.getErrorCode());
                    System.exit(-2);
                }
            }
        }

    }
}
