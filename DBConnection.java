/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package im;

import com.mysql.jdbc.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ALiebert
 */
public class DBConnection {
    
    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;
    
    //Notiz: Passwort des root accounts ist nun crypto. Das soll verhindern das während der Präsentation jemand die DB kapert
    //Wer mit XAMPP arbeitet muss im xampp Ordner unter phpMyAdmin die config.inc.php bearbeiten und dort das neue passwort eintragen.
    public DBConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.con = (Connection) DriverManager.getConnection("jdbc:mysql:localhost", "admin", "crypto");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public ResultSet dbQueue(String queue){
        try{
            this.stmt = (PreparedStatement)  con.prepareStatement(queue);    
            this.rs = this.stmt.executeQuery();
            con.close();
            stmt.close();
            return this.rs;
            
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Das Queue :' "+queue+"' konnte nicht ausgeführt werden.");
            return this.rs;
        }
        finally {
        try {
            if (con != null) {
                con.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            } catch (SQLException sqlee) {
                sqlee.printStackTrace();
            }
        }
    }
}
