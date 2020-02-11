package webdemokit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mario
 *
 * Represents different elements of the GUI. Subclasses are
 *  - sql node
 *  - mgmt node
 *  - data node
 *  - load generator
 *  - load visualizer
 *  - connection
 */

import java.util.ArrayList;
import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

abstract public class Element extends Thread implements HttpHandler {

    static int UID_GENERATOR = 0;
    protected int uid=++UID_GENERATOR;
    
    int x, y;
    ArrayList<Element> siblings;
    boolean isRunning=true;
    
    protected Element (ArrayList<Element> sibs, int x, int y, int w, int h) {
        siblings=sibs;
        this.x=x;
        this.y=y;
        //his.id = -1;
    }
    
    abstract public String toJson();
    abstract public String getIdString();
    public Element whoIs(String connstr) {return null;} // Override if you want to be a connection target
    public String getURL() {return "";}
    public void handle(HttpExchange t) throws IOException {}
    public void terminate() {isRunning=false;};
   
}
