/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package im;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;

/**
 *
 * @author ALiebert
 */
public class Client {
    
    JsonObject model;
    Socket sendSocket;
    OutputStream os;
    
    public Client(){
        try {
            //Per Socket eine TCP-Verbindung mit dem Server aufbauen. Danach JsonObject per OutputStream an den Server senden.
            this.sendSocket = new Socket("192.168.43.195", 4000);
            this.sendSocket.setKeepAlive(true);
            this.os = this.sendSocket.getOutputStream();
            ClientThread ct = new ClientThread(this.sendSocket);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
                 
    }
    public Client(JsonObject obj){
        //Konstruktor wenn ein JSON Object bereits vorher erzeugt wurde
        this.model = obj;
    }
    
    public void sendData(String sender, String empfaenger, String date, String msg, String publicKey) throws MalformedURLException, IOException{
        //Erstellen der JSON Datei
        JsonObject model = Json.createObjectBuilder()
                .add("sender", sender)
                .add("empfaenger", empfaenger)
                .add("Datum", date)
                .add("publicKey", publicKey)
                .add("nachicht", msg)
                .add("typ", "msg")
                .build();
            processData(model);
    }
    
    public void goOnline(String eMail){
            //erstelle JSON Object
            JsonObject model = Json.createObjectBuilder()
                    .add("user", eMail)
                    .add("typ", "onlineNotify")
                    .build();
            processData(model);
        
    }
    
    public void changeStatus(String eMail, int status){
    JsonObject model = Json.createObjectBuilder()
                .add("user", eMail)
                .add("status", status)
                .add("typ", "onlineNotifier")
                .build();
            processData(model);
    }
    public void changeAlias(String eMail, String alias){
        JsonObject model = Json.createObjectBuilder()
                .add("user", eMail)
                .add("alias", alias)
                .add("typ", "aliasChange")
                .build();
            processData(model);
    }
    
    public void registerUser(String eMail, String publicKey, String alias){
        JsonObject model = Json.createObjectBuilder()
                .add("user", eMail)
                .add("publicKey", publicKey)
                .add("alias",alias)
                .add("typ", "registerUser")
                .build();
        processData(model);
    }
    
    public void checkForUpdates(String eMail){
        JsonObject model = Json.createObjectBuilder()
                .add("user", eMail)
                .add("typ", "checkUpdates")
                .build();
        processData(model);
    }
    
    public void changePicture(String eMail, String bild_pfad){
        JsonObject model = Json.createObjectBuilder()
                .add("user", eMail)
                .add("bild_pfad", bild_pfad)
                .add("typ", "changePicture")
                .build();
            processData(model);
    }
    
    private void processData(JsonObject transfer){
        try {
            //Umformen des JsonObject in einen BitStream. Der BitStream wird dann zur Übertragung genutzt.
            StringWriter stWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
                jsonWriter.writeObject(transfer);
            }
            String jsonData = stWriter.toString();
            os.write(jsonData.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Kein Weg zum Server!");
        }
    }
}
