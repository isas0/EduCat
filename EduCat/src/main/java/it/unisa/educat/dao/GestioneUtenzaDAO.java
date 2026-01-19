package it.unisa.educat.dao;

import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Implementazione concreta di GestioneUtenzaDAO usando JDBC e MySQL
 */
public class GestioneUtenzaDAO {
    
    private static final Logger LOGGER = Logger.getLogger(GestioneUtenzaDAO.class.getName());
    
    // Query SQL
    private static final String INSERT_UTENTE = 
        "INSERT INTO Utente (nome, cognome, email, password, dataNascita, via, civico, citta, cap, tipoUtente) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    public static final String INSERT_UTENTE_CON_FIGLIO = 
            "INSERT INTO Utente (nome, cognome, email, password, dataNascita, via, civico, citta, cap, tipoUtente, nomeFiglio, cognomeFiglio, dataNascitaFiglio) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_BY_EMAIL = 
        "SELECT * FROM Utente WHERE email = ?";
    
    private static final String SELECT_ALL = 
            "SELECT * FROM Utente";
    
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
    
    
    
    private DataSource dataSource; // Nullable
    
    // Metodo protetto per ottenere connessione (facilita il mocking)
    protected Connection getConnection() throws SQLException {
    	if (dataSource != null) {
            return dataSource.getConnection();
        }
        // Fallback al DatasourceManager originale
        return DatasourceManager.getConnection();
    }
    
    // Costruttore per injection (opzionale)
    public GestioneUtenzaDAO() {
        // Costruttore vuoto per compatibilità
    }
    
    public GestioneUtenzaDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public boolean doSave(UtenteDTO u) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            if(u.getTipo().toString().equals("GENITORE"))
            	ps = conn.prepareStatement(INSERT_UTENTE_CON_FIGLIO, Statement.RETURN_GENERATED_KEYS);
            else
            	ps = conn.prepareStatement(INSERT_UTENTE, Statement.RETURN_GENERATED_KEYS);
            
            // Estrai i componenti dell'indirizzo
            //String[] indirizzoParts = parseIndirizzo(u.getIndirizzo());
            
            // Imposta i parametri
            ps.setString(1, u.getNome());
            ps.setString(2, u.getCognome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword()); // Password già hashed
            ps.setString(5, u.getDataNascita().toString());
            ps.setString(6, u.getVia()); // via
            ps.setString(7, u.getCivico()); // civico
            ps.setString(8, u.getCittà()); // città
            ps.setString(9, u.getCAP()); // cap
            ps.setString(10, u.getTipo().toString());
            
            //Parametri in più per genitore
            if(u.getTipo().toString().equals("GENITORE")) {
            	ps.setString(11, u.getNomeFiglio());
            	ps.setString(12, u.getCognomeFiglio());
            	ps.setString(13, u.getDataNascitaFiglio());
            	
            }
            
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
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    public UtenteDTO doRetrieveByEmail(String email) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
        	conn = getConnection();
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
    
    public List<UtenteDTO> doRetrieveAll() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        List<UtenteDTO> utenti = new ArrayList<>();
        
        try {
        	conn = getConnection();
            ps = conn.prepareStatement(SELECT_ALL);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                utenti.add(mapResultSetToUtente(rs));
            }
            
            return utenti;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero degli utenti: ", e);
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
        	conn = getConnection();
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
        	conn = getConnection();
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
        	conn = getConnection();
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
        	conn = getConnection();
            ps = conn.prepareStatement(UPDATE_UTENTE);
            
            
            // Imposta i parametri
            ps.setString(1, u.getNome());
            ps.setString(2, u.getCognome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPassword());
            ps.setDate(5, java.sql.Date.valueOf(u.getDataNascita()));
            ps.setString(6, u.getVia()); // via
            ps.setString(7, u.getCivico()); // civico
            ps.setString(8, u.getCittà()); // città
            ps.setString(9, u.getCAP()); // cap
            
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
    	UtenteDTO utente = new UtenteDTO();
        
        // Determina il tipo di utente in base al campo tipoUtente
        String tipoUtente = rs.getString("tipoUtente");
        
        // Crea l'utente in base al tipo
        switch (tipoUtente) {
            case "STUDENTE":
                utente.setTipo(TipoUtente.STUDENTE);
                break;
            case "TUTOR":
            	utente.setTipo(TipoUtente.TUTOR);
                break;
            case "GENITORE":
            	utente.setTipo(TipoUtente.TUTOR);
                break;
            case "AMMINISTRATORE_UTENTI":
            	utente.setTipo(TipoUtente.AMMINISTRATORE_UTENTI);
                break;
        }
        
        // Imposta gli attributi comuni
        utente.setUID(rs.getInt("idUtente"));
        utente.setNome(rs.getString("nome"));
        utente.setCognome(rs.getString("cognome"));
        utente.setEmail(rs.getString("email"));
        utente.setPassword(rs.getString("password"));
        utente.setDataNascita(rs.getString("dataNascita"));
        utente.setCittà(rs.getString("citta"));
        utente.setCAP(rs.getString("cap"));
        utente.setVia(rs.getString("via"));
        utente.setCivico(rs.getString("civico"));
        
        return utente;
    }
    
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