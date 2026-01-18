package it.unisa.educat.dao;

import it.unisa.educat.model.LezioneDTO;
import it.unisa.educat.model.PrenotazioneDTO;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * DAO per la gestione delle lezioni e prenotazioni
 */
public class GestioneLezioneDAO {
    
	// Costanti per le query SQL
	//Lezioni
	private static final String INSERT_LEZIONE = 
			"INSERT INTO Lezione (materia, dataInizio, dataFine, durata, prezzo, " +
					"modalitaLezione, idTutor, citta, statoLezione) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_STATO_LEZIONE = 
			"UPDATE Lezione SET statoLezione = ?, idStudente = ?, idPrenotazione = ? " +
					"WHERE idLezione = ?";

	private static final String SELECT_LEZIONI_BASE = 
			"SELECT l.*, u.nome as tutor_nome, u.cognome as tutor_cognome, u.citta as tutor_citta "
			+ " FROM Lezione l "
			+ "JOIN Utente u ON l.idTutor = u.idUtente "
			+ "WHERE 1=1";

	private static final String SELECT_LEZIONE_BY_ID = 
			"SELECT l.*, u.nome as tutor_nome, u.cognome as tutor_cognome, u.citta as tutor_citta " +
					//	"u.email as tutor_email, s.nome as studente_nome, s.cognome as studente_cognome " +
					"FROM Lezione l " +
					"JOIN Utente u ON l.idTutor = u.idUtente " +
					//"LEFT JOIN Utente s ON l.idStudente = s.idUtente " +
					"WHERE l.idLezione = ?";

	private static final String SELECT_STORICO_LEZIONI = 
			"SELECT * FROM Lezione " +
					"WHERE (idTutor = ? OR idStudente = ?) " +
					"AND dataInizio < NOW() " +
					"ORDER BY dataInizio DESC";

	//Prenotazione
	private static final String INSERT_PRENOTAZIONE = 
	        "INSERT INTO Prenotazione (idStudente, idLezione, dataPrenotazione, stato, importoPagato, indirizzoFatturazione, intestatario, "
	        + "numeroCarta, scadenza, cvv, idTutor) " +
	        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    
	    private static final String UPDATE_STATO_PRENOTAZIONE = 
	        "UPDATE Prenotazione SET stato = ? WHERE idPrenotazione = ?";
	    
	    private static final String SELECT_PRENOTAZIONE_BY_ID = 
	        "SELECT p.*, l.*, " +
	        "u_studente.idUtente as studente_id, u_studente.nome as studente_nome, " +
	        "u_studente.cognome as studente_cognome, u_studente.email as studente_email, " +
	        "u_tutor.idUtente as tutor_id, u_tutor.nome as tutor_nome, " +
	        "u_tutor.cognome as tutor_cognome, u_tutor.email as tutor_email " +
	        "FROM Prenotazione p " +
	        "JOIN Lezione l ON p.idLezione = l.idLezione " +
	        "JOIN Utente u_studente ON p.idStudente = u_studente.idUtente " +
	        "JOIN Utente u_tutor ON l.idTutor = u_tutor.idUtente " +
	        "WHERE p.idPrenotazione = ?";
	    
	    private static final String SELECT_PRENOTAZIONI_BY_STUDENTE = 
	    	"SELECT p.*, l.*, " +
	    	"u_studente.idUtente as studente_id, u_studente.nome as studente_nome, " +
	        "u_tutor.nome as tutor_nome, u_tutor.cognome as tutor_cognome, u_tutor.email as tutor_email, " + 
	        "u_studente.cognome as studente_cognome, u_studente.email as studente_email " +
	        "FROM Prenotazione p " +
	        "JOIN Lezione l ON p.idLezione = l.idLezione " +
	        "Join Utente u_tutor ON p.idTutor = u_tutor.idUtente " +
	        "JOIN Utente u_studente ON p.idStudente = u_studente.idUtente " +
	        "WHERE p.idStudente = ? " +
	        "ORDER BY p.dataPrenotazione DESC";
	    
	    private static final String SELECT_PRENOTAZIONI_BY_TUTOR = 
	        "SELECT p.*, l.*, " +
	        "u_studente.idUtente as studente_id, u_studente.nome as studente_nome, " +
	        "u_tutor.nome as tutor_nome, u_tutor.cognome as tutor_cognome, u_tutor.email as tutor_email, " + 
	        "u_studente.cognome as studente_cognome, u_studente.email as studente_email " +
	        "FROM Prenotazione p " +
	        "JOIN Lezione l ON p.idLezione = l.idLezione " +
	        "Join Utente u_tutor ON p.idTutor = u_tutor.idUtente " +
	        "JOIN Utente u_studente ON p.idStudente = u_studente.idUtente " +
	        "WHERE l.idTutor = ? " +
	        "ORDER BY l.dataInizio DESC";
        
    
    /**
     * Salva una nuova lezione nel database
     */
    public boolean doSaveLezione(LezioneDTO lezione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(INSERT_LEZIONE, Statement.RETURN_GENERATED_KEYS);
            
            // Imposta i parametri
            ps.setString(1, lezione.getMateria());
            ps.setTimestamp(2, Timestamp.valueOf(lezione.getDataInizio()));
            ps.setTimestamp(3, Timestamp.valueOf(lezione.getDataFine()));
            ps.setFloat(4, lezione.getDurata());
            ps.setFloat(5, lezione.getPrezzo());
            ps.setString(6, lezione.getModalitaLezione().name());
            ps.setInt(7, lezione.getTutor().getUID());
            ps.setString(8, lezione.getCitta());
            ps.setString(9, "PIANIFICATA"); // Stato iniziale
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                // Recupera l'ID generato
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    lezione.setIdLezione(rs.getInt(1));
                }
                return true;
            }
            
            return false;
            
        } finally {
        	DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Recupera lezioni in base a criteri di ricerca
     */
    public List<LezioneDTO> doRetrieveByCriteria(CriteriRicerca criteri) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<LezioneDTO> lezioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            
            // Costruzione dinamica della query
            StringBuilder sql = new StringBuilder(SELECT_LEZIONI_BASE);
            List<Object> params = new ArrayList<>();
            
            if (criteri != null) {
                // Solo lezioni PIANIFICATE e future
                sql.append(" AND l.statoLezione = 'PIANIFICATA'");
                sql.append(" AND l.dataInizio > NOW()");
                
                if (criteri.getMateria() != null && !criteri.getMateria().isEmpty()) {
                    sql.append(" AND l.materia LIKE ?");
                    params.add("%" + criteri.getMateria() + "%");
                }
                
                if (criteri.getCitta() != null && !criteri.getCitta().isEmpty()) {
                    sql.append(" AND l.citta LIKE ?");
                    params.add("%" + criteri.getCitta() + "%");
                }
                
                if (criteri.getModalita() != null && !criteri.getModalita().isEmpty()) {
                    sql.append(" AND l.modalitaLezione = ?");
                    params.add(criteri.getModalita());
                }
                
                if (criteri.getDataDa() != null) {
                    sql.append(" AND l.dataInizio >= ?");
                    params.add(Timestamp.valueOf(criteri.getDataDa()));
                }
                
                if (criteri.getDataA() != null) {
                    sql.append(" AND l.dataInizio <= ?");
                    params.add(Timestamp.valueOf(criteri.getDataA()));
                }
                
                if (criteri.getIdTutor() > 0) {
                    sql.append(" AND l.idTutor = ?");
                    params.add(criteri.getIdTutor());
                }
                
                if (criteri.getPrezzoMax() > 0) {
                    sql.append(" AND l.prezzo <= ?");
                    params.add(criteri.getPrezzoMax());
                }
            }
            
            sql.append(" ORDER BY l.dataInizio ASC");                
                // Paginazione
            if (criteri != null && criteri.getLimit() > 0) {
                sql.append(" LIMIT ?");
                params.add(criteri.getLimit());
                
                if (criteri.getOffset() > 0) {
                    sql.append(" OFFSET ?");
                    params.add(criteri.getOffset());
                }
            }
            
            
            // Prepara ed esegui la query
            ps = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            rs = ps.executeQuery();
            
            // Mappa i risultati
            while (rs.next()) {
            	LezioneDTO lezione = mapResultSetToLezione(rs);
                lezioni.add(lezione);
            }
            
            return lezioni;
            
        } finally {
        	DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Prenota una lezione (aggiorna stato lezione + crea prenotazione)
     */
    public boolean prenotaLezione(PrenotazioneDTO prenotazione) throws SQLException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;

    	try {
    		conn = DatasourceManager.getConnection();

    		// Se l'importo non è specificato, calcolalo dalla lezione
    		if (prenotazione.getImportoPagato() <= 0 && prenotazione.getLezione() != null) {
    			float importo = prenotazione.getLezione().getPrezzo() * 
    					prenotazione.getLezione().getDurata();
    			prenotazione.setImportoPagato(importo);
    		}
    		// indirizzoFatturazione, intestatario, "
    			//        + "numeroCarta, scadenza, cv
    		ps = conn.prepareStatement(INSERT_PRENOTAZIONE, Statement.RETURN_GENERATED_KEYS);

    		ps.setInt(1, prenotazione.getStudente().getUID());
    		ps.setInt(2, prenotazione.getLezione().getIdLezione());
    		ps.setDate(3, Date.valueOf(LocalDate.now()));
    		ps.setString(4, prenotazione.getStato().name());
    		ps.setFloat(5, prenotazione.getImportoPagato());
    		ps.setString(6, prenotazione.getIndirizzoFatturazione());
    		ps.setString(7, prenotazione.getIntestatario());
    		ps.setString(8, prenotazione.getNumeroCarta());
    		ps.setString(9, prenotazione.getDataScadenza());
    		ps.setInt(10, prenotazione.getCvv());
    		ps.setInt(11, prenotazione.getIdTutor());

    		int rowsAffected = ps.executeUpdate();

    		if (rowsAffected > 0) {
    			rs = ps.getGeneratedKeys();
    			if (rs.next()) {
    				prenotazione.setIdPrenotazione(rs.getInt(1));
    			}
    			return true;
    		}

    		return false;

    	} catch (SQLException e) {
    		e.printStackTrace();
    		return false;
    	} finally {
    		DatasourceManager.closeResources(conn, ps, rs);
    	}
    }
    
    /**
     * Aggiorna lo stato di una prenotazione
     */
    public boolean doUpdateStatoPrenotazione(int idPrenotazione, String nuovoStato) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(UPDATE_STATO_PRENOTAZIONE);
            
            ps.setString(1, nuovoStato);
            ps.setInt(2, idPrenotazione);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, null);
        }
    }
    
    /**
     * Annulla una prenotazione (transazione con aggiornamento lezione)
     */
    public boolean annullaPrenotazione(int idPrenotazione) throws SQLException {
        Connection conn = null;
        PreparedStatement psPrenotazione = null;
        PreparedStatement psLezione = null;
        
        try {
            conn = DatasourceManager.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Annulla la prenotazione
            String sqlPrenotazione = 
                "UPDATE Prenotazione SET stato = 'ANNULLATA' " +
                "WHERE idPrenotazione = ? AND stato = 'ATTIVA'";
            
            psPrenotazione = conn.prepareStatement(sqlPrenotazione);
            psPrenotazione.setInt(1, idPrenotazione);
            
            int rowsPrenotazione = psPrenotazione.executeUpdate();
            
            if (rowsPrenotazione == 0) {
                conn.rollback();
                return false;
            }
            
            // 2. Aggiorna la lezione associata a PIANIFICATA
            String sqlLezione = 
                "UPDATE Lezione SET statoLezione = 'PIANIFICATA', " +
                "idStudente = NULL, idPrenotazione = NULL " +
                "WHERE idPrenotazione = ?";
            
            psLezione = conn.prepareStatement(sqlLezione);
            psLezione.setInt(1, idPrenotazione);
            
            int rowsLezione = psLezione.executeUpdate();
            
            if (rowsLezione == 0) {
                conn.rollback();
                return false;
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
            if (psPrenotazione != null) psPrenotazione.close();
            if (psLezione != null) psLezione.close();
        }
    }
    
    /**
     * Ottieni storico lezioni di un utente (sia come tutor che come studente)
     */
    public List<LezioneDTO> getStoricoLezioni(int idUtente) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<LezioneDTO> lezioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            
            ps = conn.prepareStatement(SELECT_STORICO_LEZIONI);
            ps.setInt(1, idUtente);
            ps.setInt(2, idUtente);
            
            rs = ps.executeQuery();
            
            while (rs.next()) {
                LezioneDTO lezione = mapResultSetToLezione(rs);
                lezioni.add(lezione);
            }
            
            return lezioni;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Mappa ResultSet a LezioneDTO
     */
    private LezioneDTO mapResultSetToLezione(ResultSet rs) throws SQLException {
        LezioneDTO lezione = new LezioneDTO();
        
        lezione.setIdLezione(rs.getInt("idLezione"));
        lezione.setMateria(rs.getString("materia"));
        
        // Date
        Timestamp dataInizio = rs.getTimestamp("dataInizio");
        Timestamp dataFine = rs.getTimestamp("dataFine");
        
        if (dataInizio != null) {
            lezione.setDataInizio(dataInizio.toLocalDateTime());
        }
        
        if (dataFine != null) {
            lezione.setDataFine(dataFine.toLocalDateTime());
        }
        
        lezione.setDurata(rs.getFloat("durata"));
        lezione.setPrezzo(rs.getFloat("prezzo"));
        
        // Modalità
        String modalita = rs.getString("modalitaLezione");
        if (modalita != null) {
            if ("ONLINE".equalsIgnoreCase(modalita)) {
                lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.ONLINE);
            } else {
                lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.PRESENZA);
            }
        }
        
        // Crea tutor con solo ID (i dati completi saranno caricati separatamente se necessario)
        UtenteDTO tutor = new UtenteDTO();
        tutor.setUID(rs.getInt("idTutor"));
        tutor.setNome(rs.getString("tutor_nome"));
        tutor.setCognome(rs.getString("tutor_cognome"));
        lezione.setTutor(tutor);
        
        lezione.setCitta(rs.getString("tutor_citta"));
        
        // Stato
        String stato = rs.getString("statoLezione");
        if (stato != null) {
            if ("PIANIFICATA".equalsIgnoreCase(stato)) {
                lezione.setStato(LezioneDTO.StatoLezione.PIANIFICATA);
            } else if ("PRENOTATA".equalsIgnoreCase(stato)) {
                lezione.setStato(LezioneDTO.StatoLezione.PRENOTATA);
            } else if ("CONCLUSA".equalsIgnoreCase(stato)) {
                lezione.setStato(LezioneDTO.StatoLezione.CONCLUSA);
            } else if ("ANNULLATA".equalsIgnoreCase(stato)) {
                lezione.setStato(LezioneDTO.StatoLezione.ANNULLATA);
            }
        }
        
        // Studente e prenotazione
        /*int idStudente = rs.getInt("idStudente");
        if (idStudente > 0) {
            lezione.setIdStudentePrenotato(idStudente);
        }
        
        int idPrenotazione = rs.getInt("idPrenotazione");
        if (idPrenotazione > 0) {
            lezione.setIdPrenotazione(idPrenotazione);
        }*/
        
        return lezione;
    }    
    /**
     * Ottieni lezione by ID
     */
    public LezioneDTO getLezioneById(int idLezione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_LEZIONE_BY_ID);
            ps.setInt(1, idLezione);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToLezione(rs);
            }
            
            return null;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Verifica se uno studente ha già prenotato una lezione
     */
    public boolean hasStudentePrenotatoLezione(int idStudente, int idLezione) throws SQLException {
    	Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "SELECT COUNT(*) FROM Prenotazione " +
                "WHERE idStudente = ? AND idLezione = ? AND stato = 'ATTIVA'";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idStudente);
            ps.setInt(2, idLezione);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }    
    }

    public List<PrenotazioneDTO> getPrenotazioniByStudente(int idStudente) throws SQLException {
    	System.out.println("DEBUG: Cerca prenotazioni per studente ID: " + idStudente);
        System.out.println("DEBUG: Query: " + SELECT_PRENOTAZIONI_BY_STUDENTE);
    	
    	Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_PRENOTAZIONI_BY_STUDENTE);
            ps.setInt(1, idStudente);
            rs = ps.executeQuery();
            
            if(rs==null) System.out.println("Null");
            
            while (rs.next()) {
            	System.out.println("Non null");
                PrenotazioneDTO prenotazione = mapResultSetToPrenotazione(rs);
                prenotazioni.add(prenotazione);
            }
            
            
            return prenotazioni;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }    
    }

    public PrenotazioneDTO getPrenotazioneById(int idPrenotazione) throws SQLException {
    	Connection conn = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;

    	try {
    		conn = DatasourceManager.getConnection();
    		ps = conn.prepareStatement(SELECT_PRENOTAZIONE_BY_ID);
    		ps.setInt(1, idPrenotazione);
    		rs = ps.executeQuery();

    		if (rs.next()) {
    			return mapResultSetToPrenotazione(rs);
    		}

    		return null;

    	} finally {
    		DatasourceManager.closeResources(conn, ps, rs);
    	}    
    }



    /**
     * Mappa un ResultSet a un oggetto Prenotazione
     */
    
    private PrenotazioneDTO mapResultSetToPrenotazione(ResultSet rs) throws SQLException {
        
    	PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        
        // Dati base prenotazione
        prenotazione.setIdPrenotazione(rs.getInt("idPrenotazione"));
        
        Date dataPrenotazioneSql = rs.getDate("dataPrenotazione");
        if (dataPrenotazioneSql != null) {
            prenotazione.setDataPrenotazione(dataPrenotazioneSql.toLocalDate());
        }
        
        // Stato
        String stato = rs.getString("stato");
        if ("ATTIVA".equals(stato)) {
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        } else if ("ANNULLATA".equals(stato)) {
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ANNULLATA);
        } else if ("CONCLUSA".equals(stato)) {
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.CONCLUSA);
        }
        
        prenotazione.setImportoPagato(rs.getFloat("importoPagato"));
        
        // Studente
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(rs.getInt("idStudente"));
        studente.setNome(rs.getString("studente_nome"));
        studente.setCognome(rs.getString("studente_cognome"));
        studente.setEmail(rs.getString("studente_email"));
        studente.setTipo(TipoUtente.STUDENTE);
        prenotazione.setStudente(studente);
        
        // Lezione
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(rs.getInt("idLezione"));
        lezione.setMateria(rs.getString("materia"));
        lezione.setDataInizio(rs.getTimestamp("dataInizio").toLocalDateTime());
        lezione.setDataFine(rs.getTimestamp("dataFine").toLocalDateTime());
        lezione.setDurata(rs.getFloat("durata"));
        lezione.setPrezzo(rs.getFloat("prezzo"));
        
        // Modalità lezione
        String modalita = rs.getString("modalitaLezione");
        if ("ONLINE".equals(modalita)) {
            lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.ONLINE);
        } else {
            lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.PRESENZA);
        }
        
        // Tutor della lezione
        UtenteDTO tutor = new UtenteDTO();
        tutor.setUID(rs.getInt("idTutor"));
        tutor.setNome(rs.getString("tutor_nome"));
        tutor.setCognome(rs.getString("tutor_cognome"));
        tutor.setEmail(rs.getString("tutor_email"));
        tutor.setTipo(TipoUtente.TUTOR);
        lezione.setTutor(tutor);
        
        lezione.setCitta(rs.getString("citta"));
        
        // Stato lezione
        String statoLezione = rs.getString("statoLezione");
        if ("PIANIFICATA".equals(statoLezione)) {
            lezione.setStato(LezioneDTO.StatoLezione.PIANIFICATA);
        } else if ("PRENOTATA".equals(statoLezione)) {
            lezione.setStato(LezioneDTO.StatoLezione.PRENOTATA);
        } else if ("CONCLUSA".equals(statoLezione)) {
            lezione.setStato(LezioneDTO.StatoLezione.CONCLUSA);
        } else if ("ANNULLATA".equals(statoLezione)) {
            lezione.setStato(LezioneDTO.StatoLezione.ANNULLATA);
        }
        
        // Riferimenti incrociati (se presenti)
        lezione.setIdStudentePrenotato(rs.getInt("idStudente"));
        lezione.setIdPrenotazione(rs.getInt("idPrenotazione"));
        
        prenotazione.setLezione(lezione);
        
       
        
        return prenotazione;
    }
   
    public List<PrenotazioneDTO> getPrenotazioniByTutor(int idTutor) throws SQLException {
    	Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(SELECT_PRENOTAZIONI_BY_TUTOR);
            ps.setInt(1, idTutor);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                PrenotazioneDTO prenotazione = mapResultSetToPrenotazione(rs);
                prenotazioni.add(prenotazione);
            }
            
            return prenotazioni;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }        
    }
    
    
    
    /**
     * Classe interna per i criteri di ricerca
     */
    public static class CriteriRicerca {
        private String materia;
        private String citta;
        private String modalita;
        private LocalDateTime dataDa;
        private LocalDateTime dataA;
        private int idTutor;
        private float prezzoMax;
        private String statoLezione;
        private int limit;
        private int offset;
        
        // Getters e setters
        public String getMateria() { return materia; }
        public void setMateria(String materia) { this.materia = materia; }
        
        public String getCitta() { return citta; }
        public void setCitta(String citta) { this.citta = citta; }
        
        public String getModalita() { return modalita; }
        public void setModalita(String modalita) { this.modalita = modalita; }
        
        public LocalDateTime getDataDa() { return dataDa; }
        public void setDataDa(LocalDateTime dataDa) { this.dataDa = dataDa; }
        
        public LocalDateTime getDataA() { return dataA; }
        public void setDataA(LocalDateTime dataA) { this.dataA = dataA; }
        
        public int getIdTutor() { return idTutor; }
        public void setIdTutor(int idTutor) { this.idTutor = idTutor; }
        
        public float getPrezzoMax() { return prezzoMax; }
        public void setPrezzoMax(float prezzoMax) { this.prezzoMax = prezzoMax; }
        
        public String getStatoLezione() { return statoLezione; }
        public void setStatoLezione(String statoLezione) { this.statoLezione = statoLezione; }
        
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        
        public int getOffset() { return offset; }
        public void setOffset(int offset) { this.offset = offset; }
    }

	
}
