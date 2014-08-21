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
import java.net.URL;
import java.net.URLConnection;
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
    
    public Client(){
        /*
            die im Konstruktor übergebenen Daten werden per JsonObject in eine JSON Struktur gepackt
        */
                 
    }
    public Client(JsonObject obj){
        //Konstruktor wenn ein JSON Object bereits vorher erzeugt wurde
        this.model = obj;
    }
    
    public void sendData(String sender, String empfaenger, String date, String msg, String publicKey) throws MalformedURLException, IOException{
        //Erstellen der JSON Datei
        this.model = Json.createObjectBuilder()
                .add("sender", sender)
                .add("empfaenger", empfaenger)
                .add("Datum", date)
                .add("publicKey", publicKey)
                .add("nachicht", msg)
                .build();
            processData(4000);
    }
    
    public void notifyOnline(int id){
            //erstelle JSON Object
            this.model = Json.createObjectBuilder()
                    .add("user", id)
                    .add("status", "online")
                    .build();
            processData(2000);
        
    }
    
    public void notifyOffline(int id){
    this.model = Json.createObjectBuilder()
                .add("user", id)
                .add("status", "offline")
                .build();
            processData(2000);
    }
    
    private void processData(int dPort){
        try {
            //Umformen des JsonObject in einen BitStream. Der BitStream wird dann zur Übertragung genutzt.
            StringWriter stWriter = new StringWriter();
            JsonWriter jsonWriter = Json.createWriter(stWriter);
            jsonWriter.writeObject(this.model);
            jsonWriter.close();
            String jsonData = stWriter.toString();
            //verbidnung zum Server aufbauen und dann per URLConnection ein POST-Request an den Server schicken.
            this.sendSocket = new Socket("http://wi3.dhbw-heidenheim.de", dPort);
            OutputStream os = this.sendSocket.getOutputStream();
            os.write(jsonData.getBytes());
            os.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
