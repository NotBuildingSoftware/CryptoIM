/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package im;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;
import javax.json.stream.JsonGenerator;

/**
 *
 * @author ALiebert
 */
public class ServerBrain {
    
    private JsonObject jso;
    private Socket responseSocket;
    private Socket senderSocket;
    private ArrayList<Socket> list;
    
    public ServerBrain(JsonObject jso, Socket socket, ArrayList<Socket> list){
        this.jso = jso;
        this.senderSocket = socket;
        this.list = list;
    }
    
    public void registerUser(){
        try {
            String email = jso.get("user").toString();
            String publicKey = jso.get("publicKey").toString();
            String alias = jso.get("alias").toString();
            
            String queue = "INSERT INTO `user`(`email`, `online`, `public_key`, `alias`, `status`, `bild_pfad`, `last_update`, `ip`) "
                    + "VALUES ('"+email+"','','"+publicKey+"','"+alias+"','Hey there I am using CryptoIM','',CURRENT_TIMESTAMP(),'')";
            DBConnection db = new DBConnection();
            ResultSet rs = db.dbQueue(queue);
            rs.next();
            if(rs.rowInserted()){
                System.out.println("User succesful inserted!");
            }
            else
            {
                System.out.println("Error. User not Inserted!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
            
    public void changeStatus(){
        try {
            String newStatus = jso.get("status").toString();
            String eMail = jso.get("user").toString();
            
            String queue = "UPDATE `user` SET `status`='"+newStatus+"', last_update = CURRENT_TIMESTAMP() WHERE 'email' LIKE '"+eMail+"';";
            DBConnection db = new DBConnection();
            ResultSet rs = db.dbQueue(queue);
            rs.next();
            if(rs.rowUpdated()){
                System.out.println("Status successfully changed");
            }
            else{
                System.out.println("Status update failed");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void goonline(){
        try {
            this.checkForMessages(this.jso);
            
            //setOnlineStatusToOnline
            String eMail = jso.get("user").toString();
            String queue = "UPDATE `user` SET `online`= 1, last_update = CURRENT_TIMESTAMP() WHERE 'email' LIKE '"+eMail+"';";
            DBConnection dbOnline = new DBConnection();
            ResultSet rs = dbOnline.dbQueue(queue);
            rs.next();
            if(rs.rowUpdated()){
                System.out.println("User successfully went online");
            }
            else{
                System.out.println("Not online. Sorry!");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void goOffline(){}
    
    public void lookupUser(){} 
            
    public void getmsg() {
        try {
            //prüfe ob user online ist
            String eMail = jso.get("sender").toString();
            String q = "SELECT `online` FROM `user` WHERE `email` LIKE '"+eMail+"';";
            DBConnection db = new DBConnection();
            ResultSet rs = db.dbQueue(q);
            rs.next();
            //Should be a number, could be null :o
            int onlineStatus = rs.getInt("online");
            //if status == offline
            if (onlineStatus == 1){
                if(checkForRevocation()){
                    StringWriter stWriter = new StringWriter();
                    try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
                        jsonWriter.writeObject(this.jso);
                    }
                    String jsonData = stWriter.toString();
                    this.sendData(jsonData);
                }
                else{
                    System.out.println("Nachicht nicht weiterleiten. Schlüsselpaar ist ungültig!");
                }
            } 
            else{
                //legt einen Ordner an. Wenn es noch keinen gibt
                String accountName = jso.get("sender").toString();
                String folderPath = "C:/CryptoIM/"+accountName+"/";
                File newFolder = new File(folderPath);
                if(newFolder.isDirectory() == false){
                    newFolder.mkdir();
                }
                //Für jeden user gibt es einen eigenen Ordner. In diesem Ordner werden die JsonDateien gespeichert. Benannt nach einer Kombination aus sender, reciever und timestamp
                String date = jso.get("date").toString();
                String sender = jso.get("sender").toString();
                String reciever = jso.get("reciever").toString();
                String jsonDataName = folderPath.concat(sender+"."+reciever+"."+date);
                FileWriter fr = new FileWriter(jsonDataName);
                //Struktur der Json Datei
                try (JsonGenerator jsg = Json.createGenerator(fr)) {
                    //Struktur der Json Datei
                    jsg.writeStartObject();
                    jsg.write("sender", jso.get("sender").toString());
                    jsg.write("empfanger", jso.get("empfanger").toString());
                    jsg.write("Datum", jso.get("Datum").toString());
                    jsg.write("publicKey", jso.get("publicKey").toString());
                    jsg.write("nachicht", jso.get("nachicht").toString());
                    jsg.write("typ", jso.get("typ").toString());
                    jsg.writeEnd();
                }
                
                String msg = jso.get("msg").toString();
                //Insert is wrong
                String openMsg = "INSERT INTO open_Messages ('sender','reciever','Path') VALUES ('"+sender+"','"+reciever+"','"+jsonDataName+"');";
                DBConnection openmsgCon = new DBConnection();
                ResultSet dbrs = openmsgCon.dbQueue(openMsg);
                if(dbrs.rowInserted()){
                    System.out.println("Sucessfully Stored");
                }
                else{
                    System.out.println("Error. Pls Try Again!");
                }
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }      
    //Funktion mit der der User seinen Anzeigenamen ändern kann        
    public boolean changeAlias(){
        try {
            String eMail = jso.get("user").toString();
            String newAlias = jso.get("alias").toString();
            String dbQueue = "UPDATE user SET alias='"+newAlias+"', last_update = CURRENT_TIMESTAMP() WHERE email LIKE '"+eMail+"';";
            
            DBConnection db = new DBConnection();
            
            ResultSet rs = db.dbQueue(dbQueue);
            if(rs.rowUpdated())
            {
                return true;
            }
            else{
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    public void changePicture(){}
    
    public void revocateKey(){
    //Update auf die user tabelle
    //Insert auf die revocation tabelle
    
    }
    //Gibt alias, profilbild, statusnachicht und onlinestatus zurück an den Client
    public void checkForUpdates(){
        try {
            //select auf die db tabelle
            String eMail = jso.get("user").toString();
            String dbQueue = "SELECT * FROM `user` WHERE `email` LIKE '"+eMail+"';";
            DBConnection db = new DBConnection();
            ResultSet rs = db.dbQueue(dbQueue);
            rs.next();
            String online = rs.getString("email");
            String alias = rs.getString("alias");
            String status = rs.getString("status");
            String bild_pfad = rs.getString("bild_pfad");
            
            JsonObject model = Json.createObjectBuilder()   
                .add("online", online)
                .add("alias", alias)
                .add("status", status)
                .add("bild_path", bild_pfad)
                .add("typ", "contactInformation")
                .build();
            
            StringWriter stWriter = new StringWriter();
            try (JsonWriter jsonWriter = Json.createWriter(stWriter)) {
                jsonWriter.writeObject(model);
            }
            String jsonData = stWriter.toString();
            
            OutputStream os = this.senderSocket.getOutputStream();
            os.write(jsonData.getBytes());
            os.flush();
            
            //notify an den client b
        } catch (SQLException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    //Hilfsfunktionen für die Klasse
    
    //path ist der Pfad unter dem die Datei auf der Platte zufinden ist
    private JsonObject getJsonObjectFromFile(String path){
        try {
            FileInputStream fis = new FileInputStream(path);
            JsonReader jsr = Json.createReader(fis);
            JsonObject js = jsr.readObject();
            return js;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerBrain.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("File not Found. Path is incorrect");
            return null;
        }
    }
    
    private boolean checkForRevocation() throws SQLException{
        //prüfe ob der benutzte publicKey widerrufen wurde
                String usedPublicKey = jso.get("publicKey").toString();
                String checkQueue = "SELECT * FROM revocation_list WHERE public_key = "+usedPublicKey+";";
                DBConnection checkRevocation = new DBConnection();
                ResultSet revocationRS = checkRevocation.dbQueue(checkQueue);
                revocationRS.next();
                int revocationInt = revocationRS.getInt("public_key");
                if(revocationInt == 0){return true;}
                else{return false;}
                //falls der key nicht widerrufen wurde gibt das SQL-Queue null zurück. Aber bei getInt() wird für den Fall null eine 0 zurückgegeben.
                //Falls revocationInt also 0 ist gibt es für den verwendeten publicKey keinen Eintrag.
    }
    
    private void sendData(String jsonData) throws SQLException, IOException{
        //Umwandeln des JsonObjectes in einen BitStream und übertragen durch ReadLine();
                    String usedeMail = jso.get("user").toString();
                    
                    String getIP = "SELECT ip FROM user WHERE email LIKE '"+usedeMail+";";
                    DBConnection getIPCon = new DBConnection();
                    ResultSet iprs = getIPCon.dbQueue(getIP);
                    iprs.next();
                    String targetIP = iprs.getString("ip");
                    //Durchsuchen des Arrays nach dem passenden Socket um die Antwort zu senden
                    for(Socket eintrag : list){
                        if(eintrag.getInetAddress().getHostAddress().equals(targetIP)){
                            this.responseSocket = eintrag;
                        }
                    }
                    
                    OutputStream os = this.responseSocket.getOutputStream();
                    //may charset should be included
                    os.write(jsonData.getBytes());
                    os.flush();
    }
    
    private void checkForMessages(JsonObject jso) throws SQLException, IOException{
        String eMail = jso.get("user").toString();
            //checkForNewMessages
            String checkForNewMessagesQueue = "SELECT `Path` FROM `open_messages` WHERE `reciever` LIKE '"+eMail+"';";
            DBConnection db = new DBConnection();
            ResultSet rsCheck = db.dbQueue(checkForNewMessagesQueue);
            
            LinkedList<String> msgPaths = new LinkedList();
            
            while(rsCheck.next()){
                msgPaths.add(rsCheck.getString("Path"));
            }
            
            //wenn keine nachichten für den user bereit liegen wird der Teil übersprungen
            if(msgPaths.isEmpty()){
            //für jeden Pfad der gefunden wurde wird ein JsonObject erzeugt. Dieses direkt in der Methode sendData an den entsprechenden Client gesendet.
            for(String curPath : msgPaths){
                this.sendData(this.getJsonObjectFromFile(curPath).toString());
            }

            String deleteOldMessagesQueue = "DELETE FROM `open_messages` WHERE `reciever` LIKE '"+eMail+"';";
            DBConnection dbDelete = new DBConnection();
            ResultSet rsDelete = dbDelete.dbQueue(deleteOldMessagesQueue);
            //Eintrag aus der Tabelle löschen
            }
    }
}
