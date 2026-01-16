package it.unisa.educat.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatasourceManager {
   
    public static Connection getConnection() throws SQLException {
        try {
            // Carica il driver (necessario per le Web App su Tomcat)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Se manca il driver, rilanciamo l'errore come SQLException
            throw new SQLException("Driver MySQL non trovato nel classpath!", e);
        }

        // Ritorna la connessione
        return DriverManager.getConnection("jdbc:mysql://100.90.52.35:3307/educat", "isabella", "root");
    }
    
    public static void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}