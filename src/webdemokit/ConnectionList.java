/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

import java.util.ArrayList;

/**
 *
 * @author testy
 */
public class ConnectionList extends ArrayList<ConnectionItem> {
    public void removeInboundReplChannels(Element e) {
        int i=0;
        while (i<size()) {
            if ((get(i).to == e) && (get(i).getConnectionType() == ConnectionItem.REPLICATION))
                remove(i);
            else
                i++;
        }
        
    }
}
