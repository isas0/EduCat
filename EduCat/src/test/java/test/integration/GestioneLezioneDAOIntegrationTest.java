package test.integration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;
import it.unisa.educat.model.LezioneDTO.StatoLezione;
import it.unisa.educat.model.LezioneDTO.ModalitaLezione;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

@TestInstance(Lifecycle.PER_CLASS) // Un'istanza per tutti i test
public class GestioneLezioneDAOIntegrationTest {
    
    private DataSource dataSource;
    private GestioneLezioneDAO dao;
    private Connection conn;
    
    @BeforeAll
    void setUpDatabase() throws SQLException {
        // 1. Crea DataSource H2 in memoria
        org.h2.jdbcx.JdbcDataSource h2DataSource = new org.h2.jdbcx.JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");
        
        this.dataSource = h2DataSource;
        this.dao = new GestioneLezioneDAO(dataSource);
        this.conn = dataSource.getConnection();
        
        // 2. Crea schema del database
        createSchema();
        
        // 3. Inserisci dati di test
        insertTestData();
    }
    
    private void createSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Tabella Utente (semplificata per test)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS Utente (" +
                "    idUtente INT PRIMARY KEY AUTO_INCREMENT," +
                "    nome VARCHAR(50)," +
                "    cognome VARCHAR(50)," +
                "    email VARCHAR(100)," +
                "    password VARCHAR(255)," +
                "    tipo VARCHAR(20)," +
                "    citta VARCHAR(100)" +
                ")"
            );
            
            // Tabella Lezione
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS Lezione (" +
                "    idLezione INT PRIMARY KEY AUTO_INCREMENT," +
                "    materia VARCHAR(100)," +
                "    dataInizio TIMESTAMP," +
                "    dataFine TIMESTAMP," +
                "    durata FLOAT," +
                "    prezzo FLOAT," +
                "    modalitaLezione VARCHAR(20)," +
                "    idTutor INT," +
                "    citta VARCHAR(100)," +
                "    statoLezione VARCHAR(20)," +
                "    idStudente INT," +
                "    idPrenotazione INT," +
                "    FOREIGN KEY (idTutor) REFERENCES Utente(idUtente)," +
                "    FOREIGN KEY (idStudente) REFERENCES Utente(idUtente)" +
                ")"
            );
            
            // Tabella Prenotazione
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS Prenotazione (" +
                "    idPrenotazione INT PRIMARY KEY AUTO_INCREMENT," +
                "    idStudente INT," +
                "    idLezione INT," +
                "    dataPrenotazione DATE," +
                "    stato VARCHAR(20)," +
                "    importoPagato FLOAT," +
                "    indirizzoFatturazione VARCHAR(200)," +
                "    intestatario VARCHAR(100)," +
                "    numeroCarta VARCHAR(20)," +
                "    scadenza VARCHAR(10)," +
                "    cvv INT," +
                "    idTutor INT," +
                "    FOREIGN KEY (idStudente) REFERENCES Utente(idUtente)," +
                "    FOREIGN KEY (idLezione) REFERENCES Lezione(idLezione)," +
                "    FOREIGN KEY (idTutor) REFERENCES Utente(idUtente)" +
                ")"
            );
        }
    }

    
    private void insertTestData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Inserisci utenti di test
            stmt.execute(
                "INSERT INTO Utente (idUtente, nome, cognome, email, tipo, citta) VALUES " +
                "(1, 'Mario', 'Rossi', 'mario@email.com', 'TUTOR', 'Napoli'), " +
                "(2, 'Luigi', 'Verdi', 'luigi@email.com', 'STUDENTE', 'Roma'), " +
                "(3, 'Anna', 'Bianchi', 'anna@email.com', 'STUDENTE', 'Milano')"
            );
            
            // Inserisci lezioni di test
            stmt.execute(
                "INSERT INTO Lezione (idLezione, materia, dataInizio, dataFine, durata, prezzo, " +
                "                     modalitaLezione, idTutor, citta, statoLezione) VALUES " +
                "(1, 'Matematica', '2024-12-01 10:00:00', '2024-12-01 12:00:00', 2.0, 20.0, " +
                " 'ONLINE', 1, 'Roma', 'PIANIFICATA'), " +
                "(2, 'Fisica', '2026-12-02 14:00:00', '2026-12-02 16:00:00', 2.0, 25.0, " +
                " 'PRESENZA', 1, 'Milano', 'PIANIFICATA'), " +
                "(3, 'Chimica', '2024-11-01 10:00:00', '2024-11-01 12:00:00', 2.0, 18.0, " +
                " 'ONLINE', 1, 'Napoli', 'CONCLUSA')"
            );
        }
    }    
    @AfterAll
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    @BeforeEach
    void clearPrenotazioni() throws SQLException {
        // Pulisci solo le prenotazioni prima di ogni test
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Prenotazione");
        }
    }
    
    // ============== TEST REALI CON DATABASE ==============
    
    @Test
    void testDoSaveLezione_Success() throws SQLException {
        // Arrange
        UtenteDTO tutor = new UtenteDTO();
        tutor.setUID(1);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setMateria("Informatica");
        lezione.setDataInizio(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        lezione.setDataFine(lezione.getDataInizio().plusHours(2));
        lezione.setDurata(2.0f);
        lezione.setPrezzo(30.0f);
        lezione.setModalitaLezione(ModalitaLezione.ONLINE);
        lezione.setTutor(tutor);
        lezione.setCitta("Torino");
        
        // Act
        boolean result = dao.doSaveLezione(lezione);
        
        // Assert
        assertTrue(result);
        assertTrue(lezione.getIdLezione() > 0);
        
        // Verifica nel database
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Lezione WHERE idLezione = ?")) {
            ps.setInt(1, lezione.getIdLezione());
            ResultSet rs = ps.executeQuery();
            
            assertTrue(rs.next());
            assertEquals("Informatica", rs.getString("materia"));
            assertEquals("PIANIFICATA", rs.getString("statoLezione"));
        }
    }
    
    @Test
    void testDoRetrieveByCriteria_FindsLezioni() throws SQLException {
        // Arrange
        GestioneLezioneDAO.CriteriRicerca criteri = new GestioneLezioneDAO.CriteriRicerca();
        criteri.setMateria("Fisica");
        //criteri.setCitta("Roma");
        
        // Act
        List<LezioneDTO> lezioni = dao.doRetrieveByCriteria(criteri);
        
        // Assert
        assertNotNull(lezioni);
        assertFalse(lezioni.isEmpty());
        
        LezioneDTO primaLezione = lezioni.get(0);
        assertEquals("Fisica", primaLezione.getMateria());
        //assertEquals("Roma", primaLezione.getCitta());
        assertEquals(StatoLezione.PIANIFICATA, primaLezione.getStato());
    }
    
    @Test
    void testPrenotaLezione_Success() throws SQLException {
        // Arrange
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        studente.setTipo(TipoUtente.STUDENTE);
        
        LezioneDTO lezione = dao.getLezioneById(1); // Matematica
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Via Roma 1");
        prenotazione.setIntestatario("Luigi Verdi");
        prenotazione.setNumeroCarta("1234567812345678");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(lezione.getTutor().getUID());
        
        // Act
        boolean result = dao.prenotaLezione(prenotazione);
        
        // Assert
        assertTrue(result);
        assertTrue(prenotazione.getIdPrenotazione() > 0);
        
        // Verifica nel database
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM Prenotazione WHERE idPrenotazione = ?")) {
            ps.setInt(1, prenotazione.getIdPrenotazione());
            ResultSet rs = ps.executeQuery();
            
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("idStudente"));
            assertEquals("ATTIVA", rs.getString("stato"));
            
            // Verifica che la lezione sia stata aggiornata a PRENOTATA
            try (PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT statoLezione FROM Lezione WHERE idLezione = 1")) {
                ResultSet rs2 = ps2.executeQuery();
                assertTrue(rs2.next());
                assertEquals("PRENOTATA", rs2.getString("statoLezione"));
            }
        }
    }
    
    @Test
    void testHasStudentePrenotatoLezione_ReturnsTrueAfterPrenotazione() throws SQLException {
        // 1. Prima prenota una lezione
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = dao.getLezioneById(1);
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        dao.prenotaLezione(prenotazione);
        
        // 2. Testa che lo studente ha prenotato
        boolean hasPrenotato = dao.hasStudentePrenotatoLezione(2, 1);
        
        // Assert
        assertTrue(hasPrenotato);
    }
    
    @Test
    void testAnnullaPrenotazione_Success() throws SQLException {
        // 1. Crea una prenotazione
        testPrenotaLezione_Success(); // Usa il test precedente
        
        // 2. Trova l'ID della prenotazione appena creata
        int idPrenotazione;
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MAX(idPrenotazione) FROM Prenotazione")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            idPrenotazione = rs.getInt(1);
        }
        
        // 3. Annulla la prenotazione
        boolean result = dao.annullaPrenotazione(idPrenotazione);
        
        // Assert
        assertTrue(result);
        
        // Verifica che la prenotazione sia ANNULLATA
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT stato FROM Prenotazione WHERE idPrenotazione = ?")) {
            ps.setInt(1, idPrenotazione);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("ANNULLATA", rs.getString("stato"));
        }
        
        // Verifica che la lezione sia tornata PIANIFICATA
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT statoLezione FROM Lezione WHERE idLezione = 1")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("PIANIFICATA", rs.getString("statoLezione"));
        }
    }
    
 // ============== TEST METODI NUOVI ==============

    @Test
    void testSetLezioneAsConclusa() throws SQLException {
        // Arrange - Crea una lezione prenotata
        // Prima crea una prenotazione
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = dao.getLezioneById(1);
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/27");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        dao.prenotaLezione(prenotazione);
        
        // Act - Segna come conclusa
        boolean result = dao.setLezioneAsConclusa(2);
        
        // Assert
        assertTrue(result);
        
        // Verifica nel database
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT statoLezione FROM Lezione WHERE idLezione = 2")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("CONCLUSA", rs.getString("statoLezione"));
        }
    }

    @Test
    void testHasStudentePrenotazioneInFasciaOraria_Trovata() throws SQLException {
        // Arrange - Crea una prenotazione per lo studente 2
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = dao.getLezioneById(1); // Matematica 10:00-12:00
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        dao.prenotaLezione(prenotazione);
        
        // Act - Cerca sovrapposizione
        LocalDateTime dataInizioRicerca = LocalDateTime.of(2024, 12, 1, 11, 0); // 11:00
        LocalDateTime dataFineRicerca = LocalDateTime.of(2024, 12, 1, 13, 0);   // 13:00
        
        boolean result = dao.hasStudentePrenotazioneInFasciaOraria(
            2, dataInizioRicerca, dataFineRicerca);
        
        // Assert - Dovrebbe trovare sovrapposizione (11:00-13:00 si sovrappone a 10:00-12:00)
        assertTrue(result);
    }

    @Test
    void testHasStudentePrenotazioneInFasciaOraria_NonTrovata() throws SQLException {
        // Arrange - Nessuna prenotazione per fascia diversa
        LocalDateTime dataInizioRicerca = LocalDateTime.of(2024, 12, 1, 14, 0); // 14:00
        LocalDateTime dataFineRicerca = LocalDateTime.of(2024, 12, 1, 16, 0);   // 16:00
        
        // Act
        boolean result = dao.hasStudentePrenotazioneInFasciaOraria(
            2, dataInizioRicerca, dataFineRicerca);
        
        // Assert - Nessuna sovrapposizione
        assertFalse(result);
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_TutorOccupato() throws SQLException {
        // Arrange - Tutor 1 ha lezione Matematica 10:00-12:00
        LocalDateTime dataInizioRicerca = LocalDateTime.of(2024, 12, 1, 11, 0); // 11:00
        LocalDateTime dataFineRicerca = LocalDateTime.of(2024, 12, 1, 13, 0);   // 13:00
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(
            1, dataInizioRicerca, dataFineRicerca);
        
        // Assert - Dovrebbe trovare sovrapposizione
        assertTrue(result);
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_TutorLibero() throws SQLException {
        // Arrange - Cerca fascia oraria libera per tutor
        LocalDateTime dataInizioRicerca = LocalDateTime.of(2024, 12, 1, 8, 0); // 8:00
        LocalDateTime dataFineRicerca = LocalDateTime.of(2024, 12, 1, 9, 0);   // 9:00
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(
            1, dataInizioRicerca, dataFineRicerca);
        
        // Assert - Nessuna lezione in questa fascia
        assertFalse(result);
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_NonContaConcluse() throws SQLException {
        // Arrange - Cerca nella fascia della lezione CONCLUSA (Chimica)
        LocalDateTime dataInizioRicerca = LocalDateTime.of(2024, 11, 1, 10, 0); // 10:00
        LocalDateTime dataFineRicerca = LocalDateTime.of(2024, 11, 1, 12, 0);   // 12:00
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(
            1, dataInizioRicerca, dataFineRicerca);
        
        // Assert - Lezioni CONCLUSE non vengono contate
        assertFalse(result);
    }

    @Test
    void testGetPrenotazioniByTutor() throws SQLException {
        // Arrange - Crea una prenotazione per il tutor 1
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = dao.getLezioneById(1);
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        dao.prenotaLezione(prenotazione);
        
        // Act
        List<PrenotazioneDTO> prenotazioni = dao.getPrenotazioniByTutor(1);
        
        // Assert
        assertNotNull(prenotazioni);
        assertFalse(prenotazioni.isEmpty());
        
        PrenotazioneDTO primaPrenotazione = prenotazioni.get(0);
        assertEquals(1, primaPrenotazione.getLezione().getTutor().getUID());
        assertEquals("Matematica", primaPrenotazione.getLezione().getMateria());
    }

    @Test
    void testGetPrenotazioneById() throws SQLException {
        // Arrange - Crea una prenotazione
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = dao.getLezioneById(1);
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        dao.prenotaLezione(prenotazione);
        
        int idPrenotazione = prenotazione.getIdPrenotazione();
        
        // Act
        PrenotazioneDTO retrieved = dao.getPrenotazioneById(idPrenotazione);
        
        // Assert
        assertNotNull(retrieved);
        assertEquals(idPrenotazione, retrieved.getIdPrenotazione());
        assertEquals(2, retrieved.getStudente().getUID());
        assertEquals("Matematica", retrieved.getLezione().getMateria());
    }

    @Test
    void testGetStoricoLezioni() throws SQLException {
        // Arrange - Le lezioni passate (Chimica) è già nello storico
        // Act
        List<LezioneDTO> storico = dao.getStoricoLezioni(1); // Tutor 1
        
        // Assert
        assertNotNull(storico);
        // Dovrebbe trovare almeno Chimica (lezione passata)
        boolean foundChimica = storico.stream()
            .anyMatch(l -> l.getMateria().equals("Chimica"));
        assertTrue(foundChimica);
    }

    @Test
    void testDoUpdateStatoPrenotazione() throws SQLException {
        // Arrange - Crea una prenotazione
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = dao.getLezioneById(1);
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        dao.prenotaLezione(prenotazione);
        
        int idPrenotazione = prenotazione.getIdPrenotazione();
        
        // Act - Cambia stato
        boolean result = dao.doUpdateStatoPrenotazione(idPrenotazione, "CONCLUSA");
        
        // Assert
        assertTrue(result);
        
        // Verifica nel database
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT stato FROM Prenotazione WHERE idPrenotazione = ?")) {
            ps.setInt(1, idPrenotazione);
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("CONCLUSA", rs.getString("stato"));
        }
    }

    @Test
    void testDoUpdateStatoPrenotazione_NonEsistente() throws SQLException {
        // Act - Prova ad aggiornare prenotazione inesistente
        boolean result = dao.doUpdateStatoPrenotazione(999, "CONCLUSA");
        
        // Assert
        assertFalse(result);
    }

    // ============== TEST CASI LIMITE ==============

    @Test
    void testPrenotaLezione_LezioneNonEsistente() throws SQLException {
        // Arrange
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(999); // ID non esistente
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        // Act & Assert - Dovrebbe fallire per foreign key violation
        assertFalse(dao.prenotaLezione(prenotazione));
        
    }

    @Test
    void testGetLezioneById_NonEsistente() throws SQLException {
        // Act
        LezioneDTO result = dao.getLezioneById(999);
        
        // Assert
        assertNull(result);
    }

    @Test
    void testHasStudentePrenotatoLezione_PrenotazioneAnnullata() throws SQLException {
        // Arrange - Crea e poi annulla una prenotazione
        UtenteDTO studente = new UtenteDTO();
        studente.setUID(2);
        
        LezioneDTO lezione = dao.getLezioneById(1);
        
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Test");
        prenotazione.setIntestatario("Test");
        prenotazione.setNumeroCarta("1111222233334444");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        
        dao.prenotaLezione(prenotazione);
        
        // Annulla la prenotazione
        dao.doUpdateStatoPrenotazione(prenotazione.getIdPrenotazione(), "ANNULLATA");
        
        // Act - Verifica che non abbia prenotazioni ATTIVE
        boolean result = dao.hasStudentePrenotatoLezione(2, 1);
        
        // Assert - Dovrebbe essere false perché ANNULLATA ≠ ATTIVA
        assertFalse(result);
    }
}
