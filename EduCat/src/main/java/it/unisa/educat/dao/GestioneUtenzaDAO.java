package it.unisa.educat.dao;

import it.unisa.educat.model.UtenteDTO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione concreta di GestioneUtenzaDAO usando JDBC e MySQL
 */
public class GestioneUtenzaDAO {
    
    private static final Logger LOGGER = Logger.getLogger(GestioneUtenzaDAO.class.getName());
    
    // Query SQL
    private static final String INSERT_UTENTE = 
        "INSERT INTO Utente (nome, cognome, email, password, dataNascita, via, civico, citta, cap, tipoUtente) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_EMAIL = 
        "SELECT * FROM Utente WHERE email = ?";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM Utente WHERE idUtente = ?";
    
    private static final String UPDATE_UTENTE = 
        "UPDATE Utente SET nome = ?, cognome = ?, email = ?, password = ?, " +
        "dataNascita = ?, via = ?, civico = ?, citta = ?, cap = ?, tipoUtente = ? " +
        "WHERE idUtente = ?";
    
    private static final String DELETE_UTENTE = 
        "DELETE FROM Utente WHERE idUtente = ?";
    
    private static final String SELECT_BY_CRITERIO = 
    		"SELECT * FROM Utente WHERE nome LIKE '%?%' OR cognome LIKE '%?%' OR email LIKE '%?%' OR citta LIKE '%?%' OR tipoUtente LIKE '%?%'";
    
    public boolean doSave(UtenteDTO u) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(INSERT_UTENTE, Statement.RETURN_GENERATED_KEYS);
            
            // Estrai i componenti dell'indirizzo
           // String[] indirizzoParts = parseIndirizzo(u.getIndirizzo());
            
            // Imposta i parametri
            ps.setString(1, u.getNome());
            ps.setString(2, u.getCognome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword()); // Password già hashed
            ps.setDate(5, null);
            ps.setString(6, null); // via
            ps.setString(7, null); // civico
            ps.setString(8, null); // città
            ps.setString(9, null); // cap
            ps.setString(10, null);
            //ps.setDate(5, java.sql.Date.valueOf(u.getDataNascita()));
            //ps.setString(6, indirizzoParts[0]); // via
            //ps.setString(7, indirizzoParts[1]); // civico
            //ps.setString(8, indirizzoParts[2]); // città
            //ps.setString(9, indirizzoParts[3]); // cap
            
            // Determina tipoUtente in base alla classe concreta
            //String tipoUtente = determineTipoUtente(u);
            //ps.setString(10, tipoUtente);
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                // Recupera l'ID generato
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    u.setUID(rs.getInt(1));
                }
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il salvataggio dell'utente", e);
            throw e;
        } finally {
            DatasourceManager.close(conn, ps, rs);
        }
    }
    
    public UtenteDTO doRetrieveByEmail(String email) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
        	conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_BY_EMAIL);
            ps.setString(1, email);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtente(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero dell'utente per email: " + email, e);
            throw e;
        } finally {
            closeResources(conn, ps, rs);
        }
    }
    
    public UtenteDTO doRetrieveByCriterio(String stringa) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
        	conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_BY_CRITERIO);
            ps.setString(1, stringa);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtente(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore: " + stringa, e);
            throw e;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    
    public boolean doDelete(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
        	conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(DELETE_UTENTE);
            ps.setInt(1, id);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione dell'utente con ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, ps, null);
        }
    }
    
    public UtenteDTO doRetrieveById(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
        	conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_BY_ID);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToUtente(rs);
            }
            
            return null;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero dell'utente per ID: " + id, e);
            throw e;
        } finally {
            closeResources(conn, ps, rs);
        }
    }
    
    public boolean doUpdate(UtenteDTO u) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
        	conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(UPDATE_UTENTE);
            
            // Estrai i componenti dell'indirizzo
            String[] indirizzoParts = parseIndirizzo(u.getIndirizzo());
            
            // Imposta i parametri
            ps.setString(1, u.getNome());
            ps.setString(2, u.getCognome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setDate(5, java.sql.Date.valueOf(u.getDataNascita()));
            ps.setString(6, indirizzoParts[0]); // via
            ps.setString(7, indirizzoParts[1]); // civico
            ps.setString(8, indirizzoParts[2]); // città
            ps.setString(9, indirizzoParts[3]); // cap
            
            // Determina tipoUtente
            //String tipoUtente = determineTipoUtente(u);
            //ps.setString(10, tipoUtente);
            
            ps.setInt(11, u.getUID());
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiornamento dell'utente con ID: " + u.getUID(), e);
            throw e;
        } finally {
            closeResources(conn, ps, null);
        }
    }
    
    /**
     * Mappa un ResultSet a un oggetto Utente
     */
    private UtenteDTO mapResultSetToUtente(ResultSet rs) throws SQLException {
    	UtenteDTO utente = null;
        
        // Determina il tipo di utente in base al campo tipoUtente
        String tipoUtente = rs.getString("tipoUtente");
        
        // Ricostruisce l'indirizzo completo
        String indirizzo = rs.getString("via") + ", " + 
                          rs.getString("civico") + ", " + 
                          rs.getString("citta") + ", " + 
                          rs.getString("cap");
        
        // Crea l'utente in base al tipo
        switch (tipoUtente) {
            case "STUDENTE":
                //utente = new Studente();
                // Imposta attributi specifici studente se necessario
                break;
            case "TUTOR":
                //utente = new Tutor();
                // Imposta attributi specifici tutor se necessario
                break;
            case "AMMINISTRATORE_UTENTI":
                //utente = new Amministratore();
                break;
            default:
                //utente = new Utente(); // Utente generico
        }
        
        // Imposta gli attributi comuni
        utente.setUID(rs.getInt("idUtente"));
        utente.setNome(rs.getString("nome"));
        utente.setCognome(rs.getString("cognome"));
        utente.setEmail(rs.getString("email"));
        utente.setPassword(rs.getString("password"));
        utente.setDataNascita(rs.getDate("dataNascita").toLocalDate());
        utente.setIndirizzo(indirizzo);
        
        return utente;
    }
    
    /**
     * Parsa l'indirizzo in formato "Via, Civico, Città, CAP"
     */
    private String[] parseIndirizzo(String indirizzo) {
        // Formato atteso: "Via Roma, 10, Salerno, 84100"
        String[] parts = indirizzo.split(", ");
        
        if (parts.length == 4) {
            return parts;
        } else {
            // Fallback: metti tutto in via
            return new String[]{indirizzo, "", "", ""};
        }
    }
    
    /**
     * Determina il tipo di utente in base alla classe concreta
     */
    /*private String determineTipoUtente(UtenteDTO u) {
        if (u instanceof Studente) {
            return "STUDENTE";
        } else if (u instanceof Tutor) {
            return "TUTOR";
        } else if (u instanceof Amministratore) {
            return "AMMINISTRATORE";
        } else if (u instanceof Genitore) {
            return "GENITORE";
        } else {
            return "UTENTE";
        }
    }*/
    
    /**
     * Chiude le risorse JDBC
     */
    private void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Errore durante la chiusura delle risorse", e);
        }
    }
 
}