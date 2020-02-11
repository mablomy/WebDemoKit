/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webdemokit;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.File;
/**
 *
 * @author testy
 */
public class FileServer implements HttpHandler {
    
    static final int BUFFERSIZE = 4096;
    byte[] buffer = new byte[BUFFERSIZE];
    
    public FileServer() {
        
    } 
    
    public void handle(HttpExchange t) throws IOException {
        int bytesRead = 0;
        InputStream is;
        String path;
        
        is =  t.getRequestBody();
        path = t.getRequestURI().getPath();
        
        if (!t.getRequestMethod().equals("GET")) {
            System.out.println("Illegal htp request. Expected GET");
            logRequest(t);
            return404(t);
        } else if (is.read(buffer) != -1) {
            System.out.println("Illegal request: Did not expect request body");
            logRequest(t);
            return404(t);
        } else {
            is.close();
            returnFile(t, path);
        }
    }        
    
    private void returnFile(HttpExchange t, String filename) throws IOException {   
        String root = System.getProperty("user.dir")+"/client/";
        File file = new File(root + filename).getCanonicalFile();
        if (!file.getPath().startsWith(root)) {
            // Suspected path traversal attack: reject with 403 error.
            String response = "403 (Forbidden)\n";
            t.sendResponseHeaders(403, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else if (!file.isFile()) {
            // Object does not exist or is not a file: reject with 404 error.
            String response = "404 (Not Found)\n";
            t.sendResponseHeaders(404, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            // Object exists and is a file: accept with response code 200.
            t .sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer,0,count);
            }
            fs.close();
            os.close();
        }   
    }
 
    
    
    private void logRequest(HttpExchange t) {
        InputStream is = t.getRequestBody();
        System.out.println(t.getRequestHeaders().toString());
    }
    
    
    private void return404(HttpExchange t) throws IOException {
        String response = "Illegal access is logged. I'll get you...";
        OutputStream os = t.getResponseBody();
        t.sendResponseHeaders(404, response.length());
        os.write(response.getBytes());
        os.close();
    }
}
