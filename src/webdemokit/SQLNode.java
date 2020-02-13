package webdemokit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mario
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;


public class SQLNode extends Element {

    public static final int NOT_CONNECTED=1;
    public static final int CONNECTED=2;

    protected Connection conn;
    protected Statement select;
    protected ResultSet data;
    private String connString;
    protected int state=NOT_CONNECTED;
    protected String hostname="unknown host", version="unknown version", clonestate="", clonesource="";
    protected int port=-1;
    private boolean cloning = false;
    private boolean superReadOnly;
    protected ConnectionList connPanel;
    protected String clonestatlabel;
    protected ArrayList<String> replicationList;
    
    public String toString() {
        return (uid+"|SQLNode|"+x+"|"+y+"|"+connString);
    }

    public SQLNode(ArrayList<Element> sibs, ConnectionList panel,int x,int y,String conn_str) {

        super(sibs,x,y,60,80);
        // setup sql connection
        connString = conn_str;
        superReadOnly = false;
        setName("ServerChecker:'"+connString+"'");
        connPanel = panel;
        replicationList = new ArrayList<String>();
        // setup GUI element
        clonestatlabel = "Clone Status<br>" +
                         "+-----------+------+<br>" +
                         "| stage     | done |<br>" +
                         "+-----------+------+<br>" +
                         "| DROP DATA |  --  |<br>" +
                         "| FILE COPY | 100% |<br>" +
                         "| PAGE COPY |  --  |<br>" +
                         "| REDO COPY | 100% |<br>" +
                         "| FILE SYNC |  --  |<br>" +
                         "| RESTART   |  --  |<br>" +
                         "| RECOVERY  |  --  |<br>" +
                         "+-----------+------+";
    }


    public String toJson() {
        
        return ( "{\"id\":\""+getIdString()+"\"," +
                 "\"ObjType\":\"server\"," +
                 "\"hostname\":\""+hostname+"\"," +
                 "\"port\":"+port+"," +
                 "\"version\":\""+version+"\"," +
                 "\"xPos\":"+x+"," +
                 "\"yPos\":"+y+"," +
                 "\"connected\":" + getConnected() + "," +
                 "\"cloning\":" + cloning  + "," +
                 "\"clonestate\":\"" + clonestate + "\"," +
                 "\"clonesource\":\"" + clonesource + "\"," +
                 "\"superReadOnly\":" + superReadOnly + "}");                
    }
    
    
    // Thread routine to check server status
    public void run() {
        while(isRunning) {
            checkServer();
            try {Thread.sleep(1000);} catch(Exception e) {System.out.println("Sleep interrupted");}
        }
//        System.out.println("ServerChecker "+hostname+" is shut down.");
    }


    @Override public Element whoIs(String connstr) {
        //System.out.println("   Vergleiche letzt "+hostname+" und "+connstr);
        if (connstr.startsWith("jdbc:mysql://"+hostname+":"+port+"/") || connstr.equals(hostname+":"+port))
            return this;
        else
            return null;
    }
    
    
    protected void checkServer() {
        try {
            if (conn==null || conn.isClosed()) {
                try{Thread.sleep(1000);}  // Wait 1s before reconnecting
                catch(InterruptedException e){System.out.println("Sleep Interrupted");}

                conn = DriverManager.getConnection(connString);
                state=CONNECTED;
                select = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            }

            data = select.executeQuery("SELECT LEFT(@@hostname,IF(LOCATE('.',@@hostname)=0,LENGTH(@@hostname),LOCATE('.',@@hostname)-1)) AS Value");
            data.next();
            String label1 = data.getString("Value");
            data.close();
            data = select.executeQuery("SHOW VARIABLES LIKE 'port'");
            data.next();
            String label2= data.getString(2);
            hostname = label1;
            port = Integer.valueOf(label2);
            data.close();
            data = select.executeQuery("SHOW VARIABLES LIKE 'version'");
            data.next();
            String temp = data.getString(2);
            if (temp.contains("ndb"))
                version = "ndb" + temp.split("ndb")[1];
            else
                version = temp;
            data.close(); 

            data = select.executeQuery("SELECT @@super_read_only AS Value");
            data.next();
            superReadOnly = (data.getInt("Value")==1);
            data.close();

            // Replication sources
            data = select.executeQuery("SELECT channel_name, CONCAT(host,\":\",port) AS 'id' FROM performance_schema.replication_connection_configuration ;");
            ArrayList<String> temp2 = new ArrayList<String>();
            while (data.next()) {
                temp = data.getString("id");
                temp2.add(temp);
            }
            if (!replicationList.equals(temp2)) {
//                for (int i=0; i<replicationList.size(); ++i)
//                    System.out.println ("ReplicationList("+i+")="+replicationList.get(i));
//for (int i=0; i<temp2.size(); ++i)
//                    System.out.println ("temp2("+i+")="+temp2.get(i));
//                System.out.println("Building new replication list for "+hostname+":"+port);
                
                // remove all connection items and build from scratch
                connPanel.removeInboundReplChannels(this);
                replicationList = temp2;
                for (int i=0; i<replicationList.size(); ++i) {
                    int t =0;
                    Element source=null;
                    while ((source==null) && (t<siblings.size())) {
//System.out.println("comparing "+siblings.get(t).getIdString()+" and "+replicationList.get(i));
                        if (siblings.get(t).whoIs(replicationList.get(i))!= null) {
                            source = siblings.get(t);
//System.out.println(hostname+":"+port+" found its replication source: " +source.getIdString());
                        }
                        ++t;
                    }
                    if (source != null)
                        connPanel.add(new ConnectionItem(ConnectionItem.REPLICATION, source, this));
                }
            } 
            data.close();
            
            data = select.executeQuery("SELECT COUNT(*) AS `exists` FROM information_schema.plugins WHERE PLUGIN_NAME='clone' AND PLUGIN_STATUS='active'");
            data.next();
            if (data.getInt("exists") == 1) {
                // cloning is available
                data.close(); // make the resultSet usable for new queries
                data = select.executeQuery("SELECT COUNT(*) FROM performance_schema.clone_status WHERE state = 'In Progress'");
                data.next();
                if (data.getInt(1)==0) {
                    // No cloning in progress
                    cloning = false;
                    data.close();
                } else {
                    // Cloning in progress
                    cloning = true;
                    data.close();
                    data = select.executeQuery("SELECT stage, IF(estimate<>0,CONCAT(ROUND(data/estimate*100,0), '%'),' --') AS done FROM performance_schema.clone_progress");
                    clonestate = "stage     | done<br>----------+-----<br>";
                    while (data.next())
                        clonestate=clonestate + data.getString(1) + " | " + data.getString(2) + "<br>";
//System.out.println("SQLnode.checkServer Clonestate: " + clonestate);
                    data.close();
                    data = select.executeQuery("SELECT DESTINATION FROM performance_schema.clone_status");
//System.out.println("SQLnode clone chekcer: " + data.getFetchSize());
                    if (data.next()) {
                        clonesource = data.getString(1);
                    }
//System.out.println("SQLnode: Cloning from " + clonesource);                   
                }
            } else {
                // no cloning available in server
                cloning = false;
                data.close();
            }
            
        } catch (SQLException ex) {
System.out.println(ex.getMessage());
            state = NOT_CONNECTED;
        } catch (Exception e) {
            System.out.println("Exception in ServerChecker.checkServer():");
            System.out.println(e.getMessage());
        }
    }

    public String getHostname() {return hostname;}
    public String getVersion() {return version;}
    public boolean getConnected() {return (state==CONNECTED);}
    public String getIdString() {return ("server_"+uid);}
    

    public boolean insertDemoData(String engine) {
        try {
            Connection conn = DriverManager.getConnection(connString);
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
                    ") AUTO_INCREMENT=9 DEFAULT CHARSET=latin1 ENGINE="+engine,
/*                "INSERT INTO `line` VALUES (34,50,59,15,'BLUE',NULL,1),"+
                    "(34,50,34,100,'RED',NULL,2),(34,50,84,50,'RED',NULL,3),"+
                    "(34,100,84,100,'RED',NULL,4),(84,100,84,50,'RED',NULL,5),"+
                    "(34,100,84,50,'RED',NULL,6),(84,50,59,15,'BLUE',NULL,7),"+
                    "(34,50,84,100,'RED',NULL,8)", */
                "CREATE TABLE maxload (i INT PRIMARY KEY AUTO_INCREMENT, a INT, b VARCHAR(30), c FLOAT) ENGINE="+engine
          };

            Statement select = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            int i;
            for (i = 0; i < cmds.length; ++i)
                select.executeUpdate(cmds[i]);
            conn.close();
            return (true);
        } catch (SQLException ex) {
            return (false);
        }
    }

}

