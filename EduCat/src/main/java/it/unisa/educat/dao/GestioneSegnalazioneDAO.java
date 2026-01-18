package it.unisa.educat.dao;

import it.unisa.educat.model.SegnalazioneDTO;
import it.unisa.educat.model.SegnalazioneDTO.StatoSegnalazione;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO per la gestione delle segnalazioni (versione semplificata)
 */
public class GestioneSegnalazioneDAO {
    
    // Costanti per le query SQL
    private static final String INSERT_SEGNALAZIONE = 
        "INSERT INTO Segnalazione (descrizione, idSegnalante, idSegnalato) " +
        "VALUES (?, ?, ?)";
    
    private static final String SELECT_ALL_SEGNALAZIONI = 
        "SELECT s.*, " +
        "u_segnalante.nome as segnalante_nome, u_segnalante.cognome as segnalante_cognome, " +
        "u_segnalato.nome as segnalato_nome, u_segnalato.cognome as segnalato_cognome " +
        "FROM Segnalazione s " +
        "LEFT JOIN Utente u_segnalante ON s.idSegnalante = u_segnalante.idUtente " +
        "LEFT JOIN Utente u_segnalato ON s.idSegnalato = u_segnalato.idUtente " +
        "ORDER BY s.idSegnalazione DESC";
    
    private static final String SET_AS_SOLVED = "UPDATE Segnalazione SET stato = 'RISOLTA' WHERE idSegnalazione = ?";
    
    /*private static final String SELECT_SEGNALAZIONI_BY_SEGNALANTE = 
        "SELECT * FROM Segnalazione WHERE idSegnalante = ? ORDER BY idSegnalazione DESC";
    
    private static final String SELECT_SEGNALAZIONI_BY_SEGNALATO = 
        "SELECT * FROM Segnalazione WHERE idSegnalato = ? ORDER BY idSegnalazione DESC";*/
    
    private static final String DELETE_SEGNALAZIONE = 
        "DELETE FROM Segnalazione WHERE idSegnalazione = ?";
    
    /**
     * Salva una nuova segnalazione nel database
     */
    public boolean doSave(SegnalazioneDTO segnalazione) throws SQLException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;

    	try {
    		conn = DatasourceManager.getConnection();
    		ps = conn.prepareStatement(INSERT_SEGNALAZIONE, Statement.RETURN_GENERATED_KEYS);

    		// Imposta i parametri
    		ps.setString(1, segnalazione.getDescrizione());
    		ps.setInt(2, segnalazione.getIdSegnalante());
    		ps.setInt(3, segnalazione.getIdSegnalato());

    		int rowsAffected = ps.executeUpdate();

    		if (rowsAffected > 0) {
    			// Recupera l'ID generato
    			rs = ps.getGeneratedKeys();
    			if (rs.next()) {
    				segnalazione.setIdSegnalazione(rs.getInt(1));
    			}
    			return true;
    		}
    		return false;
    	}catch(Exception e) {e.printStackTrace();
    	return false;}


    	finally {
    		DatasourceManager.closeResources(conn, ps, rs);
    	}
    }

    /**
     * Recupera tutte le segnalazioni
     */
    public List<SegnalazioneDTO> doRetrieveAll() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<SegnalazioneDTO> segnalazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_ALL_SEGNALAZIONI);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                SegnalazioneDTO segnalazione = mapResultSetToSegnalazione(rs);
                segnalazioni.add(segnalazione);
            }
            
            return segnalazioni;
        }catch(Exception e) {
        	e.printStackTrace();
        	return null;
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Recupera segnalazioni fatte da un utente
     */
   /* public List<SegnalazioneDTO> doRetrieveBySegnalante(int idSegnalante) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<SegnalazioneDTO> segnalazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_SEGNALAZIONI_BY_SEGNALANTE);
            ps.setInt(1, idSegnalante);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                SegnalazioneDTO segnalazione = mapResultSetToSegnalazione(rs);
                segnalazioni.add(segnalazione);
            }
            
            return segnalazioni;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }*/
    
    /**
     * Recupera segnalazioni ricevute da un utente
     */
    /*public List<SegnalazioneDTO> doRetrieveBySegnalato(int idSegnalato) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<SegnalazioneDTO> segnalazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_SEGNALAZIONI_BY_SEGNALATO);
            ps.setInt(1, idSegnalato);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                SegnalazioneDTO segnalazione = mapResultSetToSegnalazione(rs);
                segnalazioni.add(segnalazione);
            }
            
            return segnalazioni;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }*/
    
    /**
     * Imposta una segnalazione come risolta
     */
    public boolean setAsSolved(int idSegnalazione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SET_AS_SOLVED);
            ps.setInt(1, idSegnalazione);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, null);
        }
    }
    
    /**
     * Elimina una segnalazione
     */
    public boolean doDelete(int idSegnalazione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(DELETE_SEGNALAZIONE);
            ps.setInt(1, idSegnalazione);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, null);
        }
    }
    
    /**
     * Mappa un ResultSet a un oggetto SegnalazioneDTO
     */
    private SegnalazioneDTO mapResultSetToSegnalazione(ResultSet rs) throws SQLException {
        SegnalazioneDTO segnalazione = new SegnalazioneDTO();
        
        segnalazione.setIdSegnalazione(rs.getInt("idSegnalazione"));
        segnalazione.setDescrizione(rs.getString("descrizione"));
        String stato = rs.getString("stato");
        switch (stato) {
        case "ATTIVA":
        	segnalazione.setStato(StatoSegnalazione.ATTIVA);
            break;
        case "RISOLTA":
        	segnalazione.setStato(StatoSegnalazione.RISOLTA);
            break;
        }
        
        UtenteDTO segnalante = new UtenteDTO();
        segnalante.setUID(rs.getInt("idSegnalante"));
        segnalante.setNome(rs.getString("segnalante_nome"));
        segnalante.setCognome(rs.getString("segnalante_cognome"));
        
        UtenteDTO segnalato = new UtenteDTO();
        segnalato.setUID(rs.getInt("idSegnalato"));
        segnalato.setNome(rs.getString("segnalato_nome"));
        segnalato.setCognome(rs.getString("segnalato_cognome"));
        
        segnalazione.setSegnalante(segnalante);
        segnalazione.setSegnalato(segnalato);
        
        
        return segnalazione;
    }
   
}
