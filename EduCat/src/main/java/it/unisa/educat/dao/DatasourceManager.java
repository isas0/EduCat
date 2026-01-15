package it.unisa.educat.dao;

import java.sql.Connection;
import java.sql.DriverManager;
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
        return DriverManager.getConnection("jdbc:mysql://100.108.252.88:3307/educat", "anna", "anna");
    }
}