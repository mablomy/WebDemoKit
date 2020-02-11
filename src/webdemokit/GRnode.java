/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

import static webdemokit.SQLNode.CONNECTED;
import static webdemokit.SQLNode.NOT_CONNECTED;
import java.util.ArrayList;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 *
 * @author testy
 */
public class GRnode extends SQLNode {

    final static public int UNKNOWN=0;
    final static public int ONLINE=1;
    final static public int RECOVERING=2;
    final static public int OFFLINE=3;
    final static public int ERROR=4;
    final static public int UNREACHABLE=5;
    
    private int grState;
    private int trx_to_recover;
    private boolean superReadOnly;
    private String groupName;

  
    public GRnode(ArrayList<Element> sibs, ConnectionList panel,int x,int y,String conn_str) {
        super(sibs, panel, x, y, conn_str);
        
        grState = UNKNOWN;
        trx_to_recover = 0;
        superReadOnly = false;
    
    }
            
            
    public int getGRState() {return grState;}
    public String getGroupName() {return groupName;}
    public String getNodeState() {
        String result;
   
        switch (grState) {
            case ONLINE: result="ONLINE"; break;
            case RECOVERING: result="RECOVERING"; break;
            case OFFLINE: result="OFFLINE"; break;
            case ERROR: result="ERROR"; break;
            case UNREACHABLE: result="UNREACHABLE"; break;
            default: result="UNKNOWN";
        }
        return result;
    }
    
    @Override public String toJson() {
        String baseNode;
        
        baseNode = super.toJson();
        return (baseNode.substring(0, baseNode.length()-1) +
                ",\"nodeState\":\"" + getNodeState() + "\"," +
                "\"trxToRecover\":" + trx_to_recover  +"," +
                "\"superReadOnly\":" + superReadOnly + "}");
    }
    
    @Override protected void checkServer() {
        super.checkServer();
        if (state != CONNECTED) {
            grState = OFFLINE;
        } else try {
            data = select.executeQuery("SELECT MEMBER_STATE AS Value FROM performance_schema.replication_group_members WHERE MEMBER_ID=@@server_uuid AND CHANNEL_NAME='group_replication_applier'");
//            data = select.executeQuery("SELECT MEMBER_ID, MEMBER_STATE FROM performance_schema.replication_group_members WHERE CHANNEL_NAME='group_replication_applier'");
//            while (data.next()) {
                
//            }
            if (!data.next()) {
                //Empty result set == server is offline
                grState=OFFLINE;
            } else {
                String label1 = data.getString("Value");
                data.close();
                switch (label1) {
                    case "ONLINE": grState=ONLINE; break;
                    case "RECOVERING": grState=RECOVERING; break;
                    case "OFFLINE": grState=OFFLINE; break;
                    case "ERROR": grState=ERROR; break;
                    case "UNREACHABLE": grState=UNREACHABLE; break;
                    default: grState=UNKNOWN;
                }
            }
            
            data = select.executeQuery("SELECT @@super_read_only AS Value");
            data.next();
            superReadOnly = (data.getInt("Value")==1);
            data.close();
            
            if (grState != OFFLINE) {
//System.out.println("GRnode.check: state="+grState);
                data = select.executeQuery("SELECT @@group_replication_group_name as Value");
//              data = select.executeQuery("SHOW VARIABLES LIKE 'group_replication_group_name'");
                if (data.next()) // when cloning node is in recovery but has group_name not defined. So empty result set
                    groupName = data.getString("Value");
//System.out.println("GRnode.check:Groupname = " + groupName);
            }
            
            if (grState == RECOVERING) {
                data = select.executeQuery("select substring_index(substring_index(GTID_SUBTRACT(RECEIVED_TRANSACTION_SET,@@gtid_executed),':',-1),'-',-1)-substring_index(substring_index(GTID_SUBTRACT(RECEIVED_TRANSACTION_SET,@@gtid_executed),':',-1),'-',1) as trx_to_recover from performance_schema.replication_connection_status where channel_name='group_replication_recovery'");
                if (data.next()) // when cloning there is no trx_to_recover
                    trx_to_recover = data.getInt("trx_to_recover");
//System.out.println("GRnode: trx to recover: "+ Integer.toString(trx_to_recover));
            } else
                trx_to_recover = 0;
            
        } catch (SQLException ex) {
            //state = NOT_CONNECTED;
            grState=UNKNOWN;
            System.out.println("SQLException in GRNode.checkServer:");
            System.out.println(ex);
        } catch (Exception e) {
            System.out.println("Exception in GRnode.checkServer():");
            System.out.println(e.getMessage());
        }
    }

    
}

