/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

/**
 *
 * @author testy
 */


import java.util.ArrayList;


public class GroupReplication extends Element {
    
    static final int lrOffset = 60;
    static final int topOffsetInGroup = 30;
    static final int topOffsetOutsideGroup = 180 + topOffsetInGroup;
    static final int topOffsetNearlyIn = 180;
    static final int serverSpacing = 150;
    int xPos, yPos;
    String[] hostports;
    ArrayList<GRnode> groupReplicationMembers;
    String uUID;
    String resilienceLevel;
    
    public GroupReplication (ArrayList<Element> sibs, ConnectionList panel, int x, int y, String hostportlist, String connectTrailer) {
        super(sibs,x,y, 3*serverSpacing+2*lrOffset, topOffsetOutsideGroup+100);
        xPos = x;
        yPos = y;
        groupReplicationMembers = new ArrayList<>();
        hostports = hostportlist.split(",");
        for (int i=0; i<hostports.length; ++i) {
            groupReplicationMembers.add(new GRnode(sibs, panel, lrOffset+serverSpacing*i, topOffsetInGroup, "jdbc:mysql://" + hostports[i] + connectTrailer)); 
        }
    }
    
    @Override public Element whoIs (String connstr) {
        Element temp=null;
        for (int i=0; i<groupReplicationMembers.size(); ++i) {
            temp=groupReplicationMembers.get(i).whoIs(connstr);
            if (temp != null) break;
        }
        return temp;
    }
    
    @Override public String getIdString() {return "group_replication_"+uid;};
    
    @Override public void start() {
        super.start();
        for (int i=0; i < groupReplicationMembers.size(); ++i) {
            groupReplicationMembers.get(i).start();
        }
    }
    
    @Override public void terminate() {
        super.terminate();
        for (int i=0; i < groupReplicationMembers.size(); ++i) {
            groupReplicationMembers.get(i).terminate();
        }
    }
    
    @Override public String toJson() {
        int onlinecount = 0;
        String result="";
                
        for (int i=0; i < groupReplicationMembers.size(); ++i) {
            if (groupReplicationMembers.get(i).getGRState() == GRnode.ONLINE) {
                onlinecount++;
                uUID = groupReplicationMembers.get(i).getGroupName();
            }
            result += groupReplicationMembers.get(i).toJson() +",";
        }            
              
        return ("{\"id\":\"" + getIdString() + "\"," +
                "\"ObjType\":\"GroupReplication\"," +
                "\"xPos\":" + xPos + "," +
                "\"yPos\":" + yPos + "," +
                "\"uUID\":\"" + uUID + "\"," +
                "\"resilience\":\"Resilience: " + (int)((onlinecount-1)/2)+" failure(s) acceptable.\"," +
                "\"nodes\":["+result.substring(0, result.length()-1)+"]}");            
    }
}
