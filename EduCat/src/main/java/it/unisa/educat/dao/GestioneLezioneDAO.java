package it.unisa.educat.dao;

import it.unisa.educat.model.LezioneDTO;
import it.unisa.educat.model.PrenotazioneDTO;
import it.unisa.educat.model.SlotDTO;
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
    private static final String INSERT_LEZIONE = 
        "INSERT INTO Lezione (materia, dataLezione, durata, prezzo, modalitaLezione, " +
        "idTutor, citta, statoLezione) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_LEZIONI_BASE = 
        "SELECT l.*, u.nome as tutor_nome, u.cognome as tutor_cognome " +
        "FROM Lezione l " +
        "JOIN Utente u ON l.idTutor = u.idUtente " +
        "WHERE 1=1";
    
    private static final String SELECT_LEZIONE_BY_ID = 
            "SELECT l.*, u.nome as tutor_nome, u.cognome as tutor_cognome, " +
            "u.email as tutor_email " +
            "FROM Lezione l " +
            "JOIN Utente u ON l.idTutor = u.idUtente " +
            "WHERE l.idLezione = ?";
        
        private static final String CHECK_PRENOTAZIONE_ESISTENTE = 
            "SELECT COUNT(*) FROM Prenotazione " +
            "WHERE idStudente = ? AND idLezione = ? AND stato = 'ATTIVA'";
        
    
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
            ps.setTimestamp(2, Timestamp.valueOf(lezione.getData()));
            ps.setFloat(3, lezione.getDurata());
            ps.setFloat(4, lezione.getPrezzo());
            ps.setString(5, lezione.getModalitaLezione().name());
            ps.setInt(6, lezione.getTutor().getUID());
            ps.setString(7, lezione.getCitta());
            ps.setString(8, "PIANIFICATA"); // Stato iniziale
            
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
    
    public boolean doSaveSlot(SlotDTO slot) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "INSERT INTO Slot (idLezione, idTutor, dataOraInizio, dataOraFine, " +
                "stato, prezzo, idStudente, idPrenotazione) " +
                "VALUES (?, ?, ?, ?, ?, ?, NULL, NULL)";
            
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, slot.getLezione().getIdLezione());
            ps.setInt(2, slot.getTutor().getUID());
            ps.setTimestamp(3, Timestamp.valueOf(slot.getDataOraInizio()));
            ps.setTimestamp(4, Timestamp.valueOf(slot.getDataOraFine()));
            ps.setString(5, slot.getStato().name());
            ps.setFloat(6, slot.getPrezzo());
            
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    slot.setIdSlot(rs.getInt(1));
                }
                return true;
            }
            
            return false;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, null);
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
            List<Object> parameters = new ArrayList<>();
            
            // Applica filtri dai criteri
            if (criteri != null) {
                // Filtro per materia
                if (criteri.getMateria() != null && !criteri.getMateria().isEmpty()) {
                    sql.append(" AND l.materia = ?");
                    parameters.add(criteri.getMateria());
                }
                
                // Filtro per città
                if (criteri.getCitta() != null && !criteri.getCitta().isEmpty()) {
                    sql.append(" AND l.citta = ?");
                    parameters.add(criteri.getCitta());
                }
                
                // Filtro per modalità (ONLINE/PRESENZA)
                if (criteri.getModalita() != null && !criteri.getModalita().isEmpty()) {
                    sql.append(" AND l.modalitaLezione = ?");
                    parameters.add(criteri.getModalita());
                }
                
                // Filtro per data (da)
                if (criteri.getDataDa() != null) {
                    sql.append(" AND l.dataLezione >= ?");
                    parameters.add(Timestamp.valueOf(criteri.getDataDa()));
                }
                
                // Filtro per data (a)
                if (criteri.getDataA() != null) {
                    sql.append(" AND l.dataLezione <= ?");
                    parameters.add(Timestamp.valueOf(criteri.getDataA()));
                }
                
                // Filtro per tutor
                if (criteri.getIdTutor() > 0) {
                    sql.append(" AND l.idTutor = ?");
                    parameters.add(criteri.getIdTutor());
                }
                
                // Filtro per prezzo massimo
                if (criteri.getPrezzoMax() > 0) {
                    sql.append(" AND l.prezzo <= ?");
                    parameters.add(criteri.getPrezzoMax());
                }
                
                // Filtro per stato lezione
                if (criteri.getStatoLezione() != null && !criteri.getStatoLezione().isEmpty()) {
                    sql.append(" AND l.statoLezione = ?");
                    parameters.add(criteri.getStatoLezione());
                } else {
                    // Di default mostra solo lezioni pianificate/future
                    sql.append(" AND l.statoLezione = 'PIANIFICATA'");
                    sql.append(" AND l.dataLezione >= NOW()");
                }
                
                // Ordina per data (più vicina per prima)
                sql.append(" ORDER BY l.dataLezione ASC");
                
                // Paginazione
                if (criteri.getLimit() > 0) {
                    sql.append(" LIMIT ?");
                    parameters.add(criteri.getLimit());
                    
                    if (criteri.getOffset() > 0) {
                        sql.append(" OFFSET ?");
                        parameters.add(criteri.getOffset());
                    }
                }
            }
            
            // Prepara ed esegui la query
            ps = conn.prepareStatement(sql.toString());
            
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
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
    
    public List<PrenotazioneDTO> getPrenotazioniByStudente(int idStudente) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            
            // Query semplificata
            String sql = 
            		"SELECT p.idPrenotazione, p.dataPrenotazione, p.stato, p.idSlot, " +
            			    "p.idStudente, p.idLezione, l.materia, s.dataOraInizio, " +
            			    "u.nome as tutor_nome, u.cognome as tutor_cognome " +
            			    "FROM Prenotazione p " +
            			    "JOIN Lezione l ON p.idLezione = l.idLezione " +
            			    "LEFT JOIN Slot s ON p.idSlot = s.idSlot " +  // LEFT JOIN perché idSlot potrebbe essere NULL
            			    "JOIN Utente u ON l.idTutor = u.idUtente " +  // Per avere nome tutor
            			    "WHERE p.idStudente = ? " +
            			    "ORDER BY COALESCE(s.dataOraInizio, p.dataPrenotazione) DESC";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idStudente);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                PrenotazioneDTO prenotazione = new PrenotazioneDTO();
                
                prenotazione.setIdPrenotazione(rs.getInt("idPrenotazione"));
                prenotazione.setDataPrenotazione(rs.getDate("dataPrenotazione").toLocalDate());
                
                String stato = rs.getString("stato");
                switch (stato) {
                    case "ATTIVA": prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA); break;
                    case "ANNULLATA": prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ANNULLATA); break;
                    case "CONCLUSA": prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.CONCLUSA); break;
                    default: prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
                }
                
                // Crea studente
                UtenteDTO studente = new UtenteDTO();
                studente.setUID(rs.getInt("idStudente"));
                prenotazione.setStudente(studente);
                
                // Crea lezione minima
                LezioneDTO lezione = new LezioneDTO();
                lezione.setIdLezione(rs.getInt("idLezione"));
                lezione.setMateria(rs.getString("materia"));
                lezione.setData(rs.getTimestamp("dataLezione").toLocalDateTime());
                prenotazione.setLezione(lezione);
                
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
            String sql = 
            		"SELECT p.*, l.*, s.dataOraInizio, s.dataOraFine, " +
            			    "u.idUtente as tutor_id, u.nome as tutor_nome, u.cognome as tutor_cognome, " +
            			    "st.idUtente as studente_id, st.nome as studente_nome, st.cognome as studente_cognome " +
            			    "FROM Prenotazione p " +
            			    "JOIN Lezione l ON p.idLezione = l.idLezione " +
            			    "LEFT JOIN Slot s ON p.idSlot = s.idSlot " +  // LEFT JOIN per slot
            			    "JOIN Utente u ON l.idTutor = u.idUtente " +
            			    "JOIN Utente st ON p.idStudente = st.idUtente " +
            			    "WHERE p.idPrenotazione = ?";
            
            ps = conn.prepareStatement(sql);
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
     * Mappa un ResultSet a un oggetto Lezione
     */
    
    private LezioneDTO mapResultSetToLezione(ResultSet rs) throws SQLException {
    	LezioneDTO lezione = new LezioneDTO();
        
        lezione.setIdLezione(rs.getInt("idLezione"));
        lezione.setMateria(rs.getString("materia"));
        lezione.setData(rs.getTimestamp("dataLezione").toLocalDateTime());
        lezione.setDurata(rs.getFloat("durata"));
        lezione.setPrezzo(rs.getFloat("prezzo"));
        
        // Modalità lezione
        String modalita = rs.getString("modalitaLezione");
        if ("ONLINE".equals(modalita)) {
            lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.ONLINE);
        } else {
            lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.PRESENZA);
        }
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs1 = null;
        
        UtenteDTO tutor = new UtenteDTO();
        
        try {
        	conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement("SELECT * FROM Utente WHERE idUtente = ?");
            ps.setInt(1, rs.getInt("idTutor"));
            rs1 = ps.executeQuery();
            
            if (rs1.next()) {
            	
            	String indirizzo = rs1.getString("via") + ", " + 
                        rs1.getString("civico") + ", " + 
                        rs1.getString("citta") + ", " + 
                        rs1.getString("cap");
	
            	tutor.setUID(rs1.getInt("idUtente"));
            	tutor.setNome(rs1.getString("nome"));
            	tutor.setCognome(rs1.getString("cognome"));
            	tutor.setEmail(rs1.getString("email"));
            	tutor.setPassword(rs1.getString("password"));
            	tutor.setDataNascita(rs1.getString("dataNascita"));
            	tutor.setIndirizzo(indirizzo);
            	tutor.setTipo(UtenteDTO.TipoUtente.TUTOR);
            	return lezione;
            }
        
        lezione.setTutor(tutor);
        
        //lezione.setDescrizione(rs.getString("descrizione"));
        //lezione.setMaxPartecipanti(rs.getInt("maxPartecipanti"));
        
        }catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
		return null;
    }
    /**
     * Recupera una lezione dal suo ID
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
     * Controlla se uno studente ha già prenotato una lezione
     */
    public boolean hasStudentePrenotatoLezione(int idStudente, int idLezione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(CHECK_PRENOTAZIONE_ESISTENTE);
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
    
    
    
    /**
     * Mappa un ResultSet a un oggetto Prenotazione
     */
    
    private PrenotazioneDTO mapResultSetToPrenotazione(ResultSet rs) throws SQLException {
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        
        // Dati obbligatori
        prenotazione.setIdPrenotazione(rs.getInt("idPrenotazione"));
        
        // Data prenotazione
        Timestamp dataPrenotazioneTs = rs.getTimestamp("dataPrenotazione");
        if (dataPrenotazioneTs != null) {
            prenotazione.setDataPrenotazione(dataPrenotazioneTs.toLocalDateTime().toLocalDate());
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
        
        // Studente (solo ID per ora)
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(rs.getInt("idStudente"));
        studente.setTipo(TipoUtente.STUDENTE);
        prenotazione.setStudente(studente);
        
        // Crea lezione base
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(rs.getInt("idLezione"));
        lezione.setMateria(rs.getString("materia"));
        
        Timestamp dataLezioneTs = rs.getTimestamp("dataLezione");
        if (dataLezioneTs != null) {
            lezione.setData(dataLezioneTs.toLocalDateTime());
        }
        
        // Tutor (importante per la verifica)
        UtenteDTO tutor = new UtenteDTO();
        tutor.setUID(rs.getInt("idTutor")); // Assicurati che questa colonna esista nella query
        tutor.setTipo(TipoUtente.TUTOR);
        
        lezione.setTutor(tutor);
        prenotazione.setLezione(lezione);
        
        try {
            // Controlla se c'è l'idSlot
            int idSlot = rs.getInt("idSlot");
            if (idSlot > 0) {
                // Se vuoi puoi creare un oggetto SlotDTO minimale
                SlotDTO slot = new SlotDTO();
                slot.setIdSlot(idSlot);
                
                // Recupera dataOraInizio se presente
                Timestamp dataOraInizio = rs.getTimestamp("dataOraInizio");
                if (dataOraInizio != null) {
                    slot.setDataOraInizio(dataOraInizio.toLocalDateTime());
                    
                    // Aggiorna anche la data nella lezione per retrocompatibilità
                    if (prenotazione.getLezione() != null) {
                        prenotazione.getLezione().setData(dataOraInizio.toLocalDateTime());
                    }
                }
                
                // Recupera dataOraFine se presente
                Timestamp dataOraFine = rs.getTimestamp("dataOraFine");
                if (dataOraFine != null) {
                    slot.setDataOraFine(dataOraFine.toLocalDateTime());
                }
                
                prenotazione.setSlot(slot);
            }
        } catch (SQLException e) {
            // Campo idSlot potrebbe non esistere (vecchie prenotazioni)
            // Ignora l'errore
        }
        
        return prenotazione;
    }
   
    public List<PrenotazioneDTO> getPrenotazioniByTutor(int idTutor) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            
            String sql = 
            		"SELECT p.*, l.*, s.dataOraInizio, s.dataOraFine, " +
            			    "u.idUtente as tutor_id, u.nome as tutor_nome, u.cognome as tutor_cognome, " +
            			    "st.idUtente as studente_id, st.nome as studente_nome, st.cognome as studente_cognome, " +
            			    "st.email as studente_email " +
            			    "FROM Prenotazione p " +
            			    "JOIN Lezione l ON p.idLezione = l.idLezione " +
            			    "LEFT JOIN Slot s ON p.idSlot = s.idSlot " +  // LEFT JOIN per slot
            			    "JOIN Utente u ON l.idTutor = u.idUtente " +
            			    "JOIN Utente st ON p.idStudente = st.idUtente " +
            			    "WHERE l.idTutor = ? " +
            			    "ORDER BY COALESCE(s.dataOraInizio, l.dataLezione) DESC, p.dataPrenotazione DESC";
            
            ps = conn.prepareStatement(sql);
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
    
    //SLOT
    public SlotDTO getSlotById(int idSlot) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "SELECT s.*, l.*, u_tutor.nome as tutor_nome, u_tutor.cognome as tutor_cognome, " +
                "u_tutor.email as tutor_email, u_studente.idUtente as studente_id " +
                "FROM Slot s " +
                "JOIN Lezione l ON s.idLezione = l.idLezione " +
                "JOIN Utente u_tutor ON l.idTutor = u_tutor.idUtente " +
                "LEFT JOIN Utente u_studente ON s.idStudente = u_studente.idUtente " +
                "WHERE s.idSlot = ?";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idSlot);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSlot(rs);
            }
            
            return null;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Verifica se uno studente ha già prenotato uno specifico slot
     */
    public boolean hasStudentePrenotatoSlot(int idStudente, int idSlot) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "SELECT COUNT(*) FROM Slot " +
                "WHERE idSlot = ? AND idStudente = ? AND stato = 'PRENOTATO'";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idSlot);
            ps.setInt(2, idStudente);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
            return false;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Prenota uno slot (transazione)
     */
    public boolean prenotaSlot(int idSlot, PrenotazioneDTO prenotazione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            conn.setAutoCommit(false); // Inizia transazione
            
            // 1. Inserisci la prenotazione
            String sqlPrenotazione = 
                "INSERT INTO Prenotazione (idStudente, dataPrenotazione, stato, importoPagato) " +
                "VALUES (?, ?, ?, ?)";
            
            ps = conn.prepareStatement(sqlPrenotazione, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, prenotazione.getStudente().getUID());
            ps.setDate(2, Date.valueOf(prenotazione.getDataPrenotazione()));
            ps.setString(3, prenotazione.getStato().name());
            ps.setFloat(4, prenotazione.getImportoPagato());
            
            int rows = ps.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return false;
            }
            
            // Ottieni l'ID generato
            rs = ps.getGeneratedKeys();
            int idPrenotazione = 0;
            if (rs.next()) {
                idPrenotazione = rs.getInt(1);
                prenotazione.setIdPrenotazione(idPrenotazione);
            }
            
            // 2. Aggiorna lo slot a "PRENOTATO"
            String sqlSlot = 
                "UPDATE Slot SET stato = 'PRENOTATO', idStudente = ?, idPrenotazione = ? " +
                "WHERE idSlot = ? AND stato = 'DISPONIBILE'";
            
            ps = conn.prepareStatement(sqlSlot);
            ps.setInt(1, prenotazione.getStudente().getUID());
            ps.setInt(2, idPrenotazione);
            ps.setInt(3, idSlot);
            
            rows = ps.executeUpdate();
            if (rows == 0) {
                conn.rollback();
                return false;
            }
            
            conn.commit(); // Conferma transazione
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Mappa ResultSet a SlotDTO
     */
    private SlotDTO mapResultSetToSlot(ResultSet rs) throws SQLException {
        SlotDTO slot = new SlotDTO();
        
        slot.setIdSlot(rs.getInt("idSlot"));
        
        // Data e ora
        Timestamp inizioTs = rs.getTimestamp("dataOraInizio");
        Timestamp fineTs = rs.getTimestamp("dataOraFine");
        if (inizioTs != null) {
            slot.setDataOraInizio(inizioTs.toLocalDateTime());
        }
        if (fineTs != null) {
            slot.setDataOraFine(fineTs.toLocalDateTime());
        }
        
        // Stato
        String stato = rs.getString("stato");
        if (stato != null) {
            slot.setStato(SlotDTO.StatoSlot.valueOf(stato));
        }
        
        // Prezzo
        slot.setPrezzo(rs.getFloat("prezzo"));
        
        // Lezione
        LezioneDTO lezione = mapResultSetToLezione(rs);
        slot.setLezione(lezione);
        
        // Tutor (già incluso nella lezione ma possiamo anche settarlo direttamente)
        UtenteDTO tutor = new UtenteDTO();
        tutor.setUID(rs.getInt("idTutor"));
        tutor.setNome(rs.getString("tutor_nome"));
        tutor.setCognome(rs.getString("tutor_cognome"));
        tutor.setEmail(rs.getString("tutor_email"));
        tutor.setTipo(TipoUtente.TUTOR);
        slot.setTutor(tutor);
        
        // Studente (se presente)
        int studenteId = rs.getInt("studente_id");
        if (studenteId > 0) {
            UtenteDTO studente = new UtenteDTO();
            studente.setUID(studenteId);
            studente.setTipo(TipoUtente.STUDENTE);
            slot.setStudente(studente);
        }
        
        return slot;
    }
    
    /**
     * Recupera tutti gli slot disponibili per una lezione
     */
    public List<SlotDTO> getSlotDisponibiliPerLezione(int idLezione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<SlotDTO> slotList = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "SELECT s.*, l.*, u.nome as tutor_nome, u.cognome as tutor_cognome, " +
                "u.email as tutor_email " +
                "FROM Slot s " +
                "JOIN Lezione l ON s.idLezione = l.idLezione " +
                "JOIN Utente u ON l.idTutor = u.idUtente " +
                "WHERE s.idLezione = ? AND s.stato = 'DISPONIBILE' " +
                "AND s.dataOraInizio > NOW() " +
                "ORDER BY s.dataOraInizio ASC";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idLezione);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                SlotDTO slot = mapResultSetToSlot(rs);
                slotList.add(slot);
            }
            
            return slotList;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Recupera una prenotazione con i dettagli dello slot
     */
    public PrenotazioneDTO getPrenotazioneConSlotById(int idPrenotazione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "SELECT p.*, s.dataOraInizio, s.dataOraFine, l.*, " +
                "u_studente.nome as studente_nome, u_studente.cognome as studente_cognome, " +
                "u_tutor.idUtente as tutor_id, u_tutor.nome as tutor_nome, u_tutor.cognome as tutor_cognome " +
                "FROM Prenotazione p " +
                "LEFT JOIN Slot s ON p.idSlot = s.idSlot " +
                "JOIN Lezione l ON s.idLezione = l.idLezione " +
                "JOIN Utente u_studente ON p.idStudente = u_studente.idUtente " +
                "JOIN Utente u_tutor ON l.idTutor = u_tutor.idUtente " +
                "WHERE p.idPrenotazione = ?";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idPrenotazione);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPrenotazioneConSlot(rs);
            }
            
            return null;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Recupera lo slot associato a una prenotazione
     */
    public SlotDTO getSlotByPrenotazioneId(int idPrenotazione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "SELECT s.*, l.*, u.nome as tutor_nome, u.cognome as tutor_cognome " +
                "FROM Slot s " +
                "JOIN Lezione l ON s.idLezione = l.idLezione " +
                "JOIN Utente u ON l.idTutor = u.idUtente " +
                "WHERE s.idPrenotazione = ?";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idPrenotazione);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSlot(rs);
            }
            
            return null;
            
        } finally {
            DatasourceManager.closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Annulla una prenotazione e libera lo slot (transazione)
     */
    public boolean annullaPrenotazioneESlot(int idPrenotazione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        
        try {
            conn = DatasourceManager.getConnection();
            conn.setAutoCommit(false);
            
            // 1. Annulla la prenotazione
            String sqlPrenotazione = 
                "UPDATE Prenotazione SET stato = 'ANNULLATA' WHERE idPrenotazione = ? AND stato = 'ATTIVA'";
            
            ps = conn.prepareStatement(sqlPrenotazione);
            ps.setInt(1, idPrenotazione);
            int rowsPrenotazione = ps.executeUpdate();
            
            if (rowsPrenotazione == 0) {
                conn.rollback();
                return false;
            }
            
            // 2. Libera lo slot (imposta a DISPONIBILE e rimuovi riferimenti)
            String sqlSlot = 
                "UPDATE Slot SET stato = 'DISPONIBILE', idStudente = NULL, idPrenotazione = NULL " +
                "WHERE idPrenotazione = ?";
            
            ps = conn.prepareStatement(sqlSlot);
            ps.setInt(1, idPrenotazione);
            int rowsSlot = ps.executeUpdate();
            
            if (rowsSlot == 0) {
                conn.rollback();
                return false;
            }
            
            conn.commit();
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            DatasourceManager.closeResources(conn, ps, null);
        }
    }
    
    /**
     * Mappa ResultSet a PrenotazioneDTO con slot
     */
    private PrenotazioneDTO mapResultSetToPrenotazioneConSlot(ResultSet rs) throws SQLException {
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        
        prenotazione.setIdPrenotazione(rs.getInt("idPrenotazione"));
        
        // Data prenotazione
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
        studente.setTipo(TipoUtente.STUDENTE);
        prenotazione.setStudente(studente);
        
        // Lezione
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(rs.getInt("idLezione"));
        lezione.setMateria(rs.getString("materia"));
        lezione.setDurata(rs.getFloat("durata"));
        lezione.setPrezzo(rs.getFloat("prezzo"));
        
        // Modalità
        String modalita = rs.getString("modalitaLezione");
        if ("ONLINE".equals(modalita)) {
            lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.ONLINE);
        } else {
            lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.PRESENZA);
        }
        
        // Tutor della lezione
        UtenteDTO tutor = new UtenteDTO();
        tutor.setUID(rs.getInt("tutor_id"));
        tutor.setNome(rs.getString("tutor_nome"));
        tutor.setCognome(rs.getString("tutor_cognome"));
        tutor.setTipo(TipoUtente.TUTOR);
        lezione.setTutor(tutor);
        
        // Data della lezione (ora è nello slot)
        Timestamp dataSlot = rs.getTimestamp("dataOraInizio");
        if (dataSlot != null) {
            lezione.setData(dataSlot.toLocalDateTime());
        }
        
        lezione.setCitta(rs.getString("citta"));
        prenotazione.setLezione(lezione);
        
        return prenotazione;
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
