/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

import com.google.gson.JsonArray;
import com.google.gson.Gson;

/**
 *
 * @author Mario
 */
public class RouterConnections {
    static int Gb = 1024*1024*1024;
    static int Mb = 1024*1024;
    static int Kb = 1024;
        
    class RouterConnection {
        int bytesFromServer;
        int bytesToServer;
        String sourceAddress;
        String destinationAddress;
        String timeStarted;
        String timeConnectedToServer;
        String timeLastSentToServer;
        String timeLastReceivedFromServer;
    }
    
    JsonArray items;
    Gson gson = new Gson();
    
    public int getConnectionCount() {if (items==null) return 0; return items.size();}
    public String getReport() {
        String result="";
        RouterConnection rc;
        if (items == null) return "---"; // protection from unitialized array during startup
        for (int i=0; i<items.size(); ++i) {
            rc = gson.fromJson(items.get(i), RouterConnection.class);
            result += "<tr><td>"+rc.sourceAddress+"</td><td>"+rc.destinationAddress+"</td><td>"+cut(rc.bytesToServer)+"</td><td>"+cut(rc.bytesFromServer)+"</td></tr>";
        }
        
        return "<table><tr><th>From</th><th>To</th><th>Bytes in</th><th>Bytes out</th></tr>"+result+"</table>";
    }

    String cut(int t) {

        if (t>=Gb) return (t/Gb+"Gb");
        if (t>=Mb) return (t/Mb+"Mb");
        if (t>=Kb) return (t/Kb+"Kb");
        return new Integer(t).toString();
    }
}
