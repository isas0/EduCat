package it.unisa.educat.dao;

import it.unisa.educat.model.LezioneDTO;
import it.unisa.educat.model.PrenotazioneDTO;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO per la gestione delle lezioni e prenotazioni
 */
public class GestioneLezioneDAO {
    
    // Costanti per le query SQL
    private static final String INSERT_LEZIONE = 
        "INSERT INTO Lezione (materia, dataLezione, durata, prezzo, modalitaLezione, " +
        "idTutor, citta, statoLezione) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String INSERT_PRENOTAZIONE = 
        "INSERT INTO Prenotazione (idStudente, idLezione, dataPrenotazione, stato, " +
        "metodoPagamento, statoPagamento, importoPagato) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_STATO_PRENOTAZIONE = 
        "UPDATE Prenotazione SET stato = ? WHERE idPrenotazione = ?";
    
    private static final String SELECT_LEZIONI_BASE = 
        "SELECT l.*, u.nome as tutor_nome, u.cognome as tutor_cognome " +
        "FROM Lezione l " +
        "JOIN Utente u ON l.idTutor = u.idUtente " +
        "WHERE 1=1";
    
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
        	DatasourceManager.close(conn, ps, rs);
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
        	DatasourceManager.close(conn, ps, rs);
        }
    }
    
    /**
     * Salva una nuova prenotazione
     */
    public boolean doSavePrenotazione(PrenotazioneDTO prenotazione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DatasourceManager.getConnection();
            ps = conn.prepareStatement(INSERT_PRENOTAZIONE, Statement.RETURN_GENERATED_KEYS);
            
            // Calcola importo in base a durata e prezzo della lezione
            float importo = prenotazione.getLezione().getPrezzo() * 
                          prenotazione.getLezione().getDurata();
            
            ps.setInt(1, prenotazione.getStudente().getUID());
            ps.setInt(2, prenotazione.getLezione().getIdLezione());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, "ATTIVA"); // Stato iniziale
            ps.setString(5, prenotazione.getMetodoPagamento());
            ps.setString(6, "COMPLETATO"); // Assumiamo pagamento completato
            ps.setFloat(7, importo);
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                // Recupera l'ID generato
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    prenotazione.setIdPrenotazione(rs.getInt(1));
                }
                return true;
            }
            
            return false;
            
        } finally {
        	DatasourceManager.close(conn, ps, rs);
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
        	DatasourceManager.close(conn, ps, null);
        }
    }
    
    /**
     * Metodi aggiuntivi utili
     */
    
    /**
     * Recupera lezioni di un tutor specifico
     */
    /*
    public List<LezioneDTO> getLezioniByTutor(int idTutor) throws SQLException {
        CriteriRicerca criteri = new CriteriRicerca();
        criteri.setIdTutor(idTutor);
        return doRetrieveByCriteria(criteri);
    }
    
    /**
     * Recupera prenotazioni di uno studente
     */
    /*
    public List<PrenotazioneDTO> getPrenotazioniByStudente(int idStudente) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
        
        try {
            conn = DatasourceManager.getConnection();
            String sql = 
                "SELECT p.*, l.*, u.nome as tutor_nome, u.cognome as tutor_cognome " +
                "FROM Prenotazione p " +
                "JOIN Lezione l ON p.idLezione = l.idLezione " +
                "JOIN Utente u ON l.idTutor = u.idUtente " +
                "WHERE p.idStudente = ? " +
                "ORDER BY p.dataPrenotazione DESC";
            
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idStudente);
            rs = ps.executeQuery();
            
            while (rs.next()) {
            	PrenotazioneDTO prenotazione = mapResultSetToPrenotazione(rs);
                prenotazioni.add(prenotazione);
            }
            
            return prenotazioni;
            
        } finally {
            closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Controlla se uno studente ha già prenotato una lezione
     */
    /*
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
            closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Conta posti disponibili per una lezione
     */
    /*
    public int getPostiDisponibili(int idLezione) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            // Ottieni massimo partecipanti della lezione
            conn = DatasourceManager.getConnection();
            String sql = "SELECT maxPartecipanti FROM Lezione WHERE idLezione = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idLezione);
            rs = ps.executeQuery();
            
            if (!rs.next()) {
                return 0;
            }
            
            int maxPartecipanti = rs.getInt(1);
            
            // Conta prenotazioni attive
            rs.close();
            ps.close();
            
            sql = "SELECT COUNT(*) FROM Prenotazione WHERE idLezione = ? AND stato = 'ATTIVA'";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idLezione);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                int prenotazioniAttive = rs.getInt(1);
                return Math.max(0, maxPartecipanti - prenotazioniAttive);
            }
            
            return maxPartecipanti;
            
        } finally {
            closeResources(conn, ps, rs);
        }
    }
    
    /**
     * Mappa un ResultSet a un oggetto Lezione
     */
    /*
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
        
        // Crea tutor fittizio (solo con nome e cognome)
        // In una implementazione completa, dovresti caricare tutto l'oggetto Tutor
        it.unisa.educat.model.Tutor tutor = new it.unisa.educat.model.Tutor();
        tutor.setId(rs.getInt("idTutor"));
        tutor.setNome(rs.getString("tutor_nome"));
        tutor.setCognome(rs.getString("tutor_cognome"));
        lezione.setTutor(tutor);
        
        lezione.setCitta(rs.getString("citta"));
        //lezione.setDescrizione(rs.getString("descrizione"));
        //lezione.setMaxPartecipanti(rs.getInt("maxPartecipanti"));
        
        return lezione;
    }
    
    /**
     * Mappa un ResultSet a un oggetto Prenotazione
     */
    /*
    private PrenotazioneDTO mapResultSetToPrenotazione(ResultSet rs) throws SQLException {
    	PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        
        prenotazione.setIdPrenotazione(rs.getInt("idPrenotazione"));
        prenotazione.setDataPrenotazione(rs.getTimestamp("dataPrenotazione").toLocalDateTime().toLocalDate());
        
        // Stato
        String stato = rs.getString("stato");
        if ("ATTIVA".equals(stato)) {
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        } else if ("ANNULLATA".equals(stato)) {
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ANNULLATA);
        } else if ("CONCLUSA".equals(stato)) {
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.CONCLUSA);
        }
        
        // Crea studente fittizio
        it.unisa.educat.model.Studente studente = new it.unisa.educat.model.Studente();
        studente.setId(rs.getInt("idStudente"));
        prenotazione.setStudente(studente);
        
        // Crea lezione
        LezioneDTO lezione = mapResultSetToLezione(rs);
        prenotazione.setLezione(lezione);
        
        return prenotazione;
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
            e.printStackTrace();
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
