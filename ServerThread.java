/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package im;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;

/**
 *
 * @author ALiebert
 */
class ServerThread extends Thread {
    private Socket socket;
    private ArrayList<Socket> list;
    
    public ServerThread(Socket client, ArrayList<Socket> list) {
        this.socket = client; 
        this.list = list;
    }
    
    @Override
    public void run(){
        try {
            //JSON auslesen
            JsonReader reader = Json.createReader(socket.getInputStream());
            JsonObject payload = reader.readObject();
            //Verarbeiten der JSON Nachicht bassierend auf dem Inhalt
            process(payload);
        } catch (IOException ex) {
            //Der InputStream kann leer sein. Daher muss eine I/O Exception abgefangen werden
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void process(JsonObject jso){
        ServerBrain sb = new ServerBrain(jso, this.socket, this.list);
        //if-clause für messages
        if(jso.get("typ").toString().equals("msg") == true){
            sb.getmsg();
        } else
        //if clauses für online
        if(jso.get("typ").toString().equals("onlineNotifly") == true){
            sb.goonline();
        } else
        //if clauses für offline
        if(jso.get("typ").toString().equals("changeStatus") == true){
            sb.changeStatus();
        } else
        //if clauses für das Ändern des alias
        if(jso.get("typ").toString().equals("aliasChange") == true){
           sb.changeAlias();
        } else
        //if clauses für das Ändern des alias
        if(jso.get("typ").toString().equals("checkUpdates") == true){
           sb.checkForUpdates();
        } else
        //letzter else-Pharagraph: wenn das packet nicht eindeutig identifiziert werden konnte. Dann wird das Packet verworfen.
        {
            System.out.println("JSON Packet konnte nicht verarbeitet werden");
        }
        
    }
    
    
    
}
