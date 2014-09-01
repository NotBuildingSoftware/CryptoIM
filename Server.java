/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package im;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ALiebert
 */
public class Server {
    private ServerSocket serverSocket;
    private ArrayList<Socket> list;
    
    public Server() throws IOException{
        try {
            serverSocket = new ServerSocket(4000);
            serveClients();
        } catch (Exception ex) {
            //fängt Exceptions aus serveClients() und von serverSocket.accept()
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void serveClients()
            throws Exception {
        // wiederhole
        while (true) {
            // warte auf Verbindung
            Socket client = serverSocket.accept();
            this.list.add(client);
            // erstelle neuen Thread für den Client
            ServerThread st = new ServerThread(client, list);
        }
    }
    
}
