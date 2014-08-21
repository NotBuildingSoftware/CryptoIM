/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cryptoimserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Puchi
 */
public class dbAction {
    
    
    public static void loadDB () {
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            new com.mysql.jdbc.Driver();
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
            String connectionUrl = "jdbc:mysql://localhost:3306/cryptoIM_test";
            String connectionUser = "root";
            String connectionPassword ="";
            conn = DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM conn_test");
            while (rs.next()) {
                String user_id = rs.getString("user_id");
                String vorname = rs.getString("vorname");
                String nachname = rs.getString ("nachname");
                String penislaenge = rs.getString ("penislaenge");
                System.out.println ("user id: " + user_id + ", vorname: " + vorname + ", nachname: " + nachname + ", penislaenge:" + penislaenge);
            }                       
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace();}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {e.printStackTrace();}
            try { if (conn != null) conn.close(); } catch (SQLException e) {e.printStackTrace();}
        }
    }
    
    
}
