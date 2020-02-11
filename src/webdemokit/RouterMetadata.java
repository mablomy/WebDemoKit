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
public class RouterMetadata {
    JsonArray items;
    class RouterMetadataEntry {String name;}
    private Gson gson = new Gson();
    
    public String getMetadataName () {
        if (items.size() < 1) {
            System.out.println ("WARNING: Router has no Metadata defined");
            return "";
        }
        
        RouterMetadataEntry temp = gson.fromJson(items.get(items.size()-1), RouterMetadataEntry.class);
        if (items.size() > 1) {
            System.out.println ("WARNING: Router has more than one metadata sets defined. Please sanitize. Picking "+temp);
        }
        return temp.name;
    }
}
