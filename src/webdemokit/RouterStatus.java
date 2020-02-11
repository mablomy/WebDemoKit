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
public class RouterStatus {
  
    private String hostname;
    private String version;
    
    public RouterStatus() {}
    
    public RouterStatus (String hn, String ver) {
        hostname=hn;
        version=ver;
    }
    
    public String getHostname() {
        if (hostname==null) return "unknown host";
        return hostname;
    }
    
    public String getVersion() {
        if (version==null) return "unknown";
        return version;
    }
    
    public boolean isNotInitialized() {
        return (hostname==null || version==null );
    }
}
