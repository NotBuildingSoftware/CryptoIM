/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package im;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author ArneLiebert
 */
public class ClientThread extends Thread{
    
    private Socket socket;
    
    public ClientThread(Socket socket){
        this.socket = socket;
    }
    
    public void getResponse() throws IOException{
        while(true){
            InputStream is = socket.getInputStream();
            is.read();
            JsonReader reader = Json.createReader(is);
            JsonObject payload = reader.readObject();
            switch(payload.get("typ").toString()){
                case "msg": //stuff.setJso(payload);
                case "contactInformation": //stuff.setJso(payload);
                default: System.out.println("Konnte Nachicht nicht zuordnen");
            }
            //schreibe Response an GUI
        }
    }
    
    
}
