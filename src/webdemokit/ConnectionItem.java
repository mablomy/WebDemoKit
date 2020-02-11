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
public class ConnectionItem {
    static int idGenerator=0;
    final private int uid=++idGenerator;
    Element from, to;
    protected int connectionType;
    final static int REPLICATION = 1;
    final static int CLIENT = 2;
    final static int CLIENT_VIA_ROUTER = 3;
    
    public ConnectionItem (int type, Element f, Element t) {
        from = f;
        to = t;
        connectionType = type;
    }

    public String toString() {
        return ("# Connection from "+from.getIdString()+" to "+to.getIdString()+"\n");
    }

    public int getConnectionType() {return connectionType;}
    
    public String toJson() {
        return ("{\"id\":\"conn_"+uid+"\",\"from\":\"" + from.getIdString() + "\",\"to\":\"" + to.getIdString() + "\",\"type\":"+connectionType+"}");
    }
}
