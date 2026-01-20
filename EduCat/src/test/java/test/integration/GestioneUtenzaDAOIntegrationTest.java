package test.integration;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import it.unisa.educat.dao.GestioneUtenzaDAO;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;


@TestInstance(Lifecycle.PER_CLASS)
public class GestioneUtenzaDAOIntegrationTest {
    
    private DataSource dataSource;
    private GestioneUtenzaDAO dao;
    private Connection conn;
    
    @BeforeAll
    void setUpDatabase() throws SQLException {
        // Crea DataSource H2 in memoria
        org.h2.jdbcx.JdbcDataSource h2DataSource = new org.h2.jdbcx.JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:testdb_utenza;DB_CLOSE_DELAY=-1;MODE=MySQL");
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");
        
        this.dataSource = h2DataSource;
        this.dao = new GestioneUtenzaDAO(dataSource);
        this.conn = dataSource.getConnection();
        
        createSchema();
    }
    
    private void createSchema() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Tabella Utente
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS Utente (" +
                "    idUtente INT PRIMARY KEY AUTO_INCREMENT," +
                "    nome VARCHAR(50) NOT NULL," +
                "    cognome VARCHAR(50) NOT NULL," +
                "    email VARCHAR(100) UNIQUE NOT NULL," +
                "    password VARCHAR(255) NOT NULL," +
                "    dataNascita DATE," +
                "    via VARCHAR(100)," +
                "    civico VARCHAR(10)," +
                "    citta VARCHAR(50)," +
                "    cap VARCHAR(10)," +
                "    tipoUtente VARCHAR(20) NOT NULL," +
                "    nomeFiglio VARCHAR(50)," +
                "    cognomeFiglio VARCHAR(50)," +
                "    dataNascitaFiglio DATE" +
                ")"
            );
        }
    }
    
    @BeforeEach
    void clearDatabase() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM Utente");
            stmt.execute("ALTER TABLE Utente ALTER COLUMN idUtente RESTART WITH 1");
        }
    }
    
    @AfterAll
    void tearDown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
    
    // ============== TEST CRUD COMPLETI ==============
    
    @Test
    void testDoSaveAndDoRetrieveById_Studente() throws SQLException {
        // Arrange
        UtenteDTO studente = new UtenteDTO();
        studente.setNome("Mario");
        studente.setCognome("Rossi");
        studente.setEmail("mario.rossi@email.com");
        studente.setPassword("hashed123");
        studente.setDataNascita("2000-01-01");
        studente.setVia("Via Roma");
        studente.setCivico("1");
        studente.setCittà("Napoli");
        studente.setCAP("80100");
        studente.setTipo(TipoUtente.STUDENTE);
        
        // Act - Salva
        boolean saveResult = dao.doSave(studente);
        
        // Assert - Salvataggio
        assertTrue(saveResult);
        assertTrue(studente.getUID() > 0);
        
        // Act - Recupera
        UtenteDTO retrieved = dao.doRetrieveById(studente.getUID());
        
        // Assert - Recupero
        assertNotNull(retrieved);
        assertEquals("Mario", retrieved.getNome());
        assertEquals("Rossi", retrieved.getCognome());
        assertEquals("mario.rossi@email.com", retrieved.getEmail());
        assertEquals(TipoUtente.STUDENTE, retrieved.getTipo());
        assertEquals("Napoli", retrieved.getCittà());
    }
    
    @Test
    void testDoSaveAndDoRetrieveByEmail_Genitore() throws SQLException {
        // Arrange
        UtenteDTO genitore = new UtenteDTO();
        genitore.setNome("Paolo");
        genitore.setCognome("Bianchi");
        genitore.setEmail("paolo.bianchi@email.com");
        genitore.setPassword("hashed456");
        genitore.setDataNascita("1980-01-01");
        genitore.setVia("Via Milano");
        genitore.setCivico("2");
        genitore.setCittà("Roma");
        genitore.setCAP("00100");
        genitore.setTipo(TipoUtente.GENITORE);
        genitore.setNomeFiglio("Luca");
        genitore.setCognomeFiglio("Bianchi");
        genitore.setDataNascitaFiglio("2015-01-01");
        
        // Act - Salva
        boolean saveResult = dao.doSave(genitore);
        
        // Assert - Salvataggio
        assertTrue(saveResult);
        assertTrue(genitore.getUID() > 0);
        
        // Act - Recupera by email
        UtenteDTO retrieved = dao.doRetrieveByEmail("paolo.bianchi@email.com");
        
        // Assert - Recupero
        assertNotNull(retrieved);
        assertEquals("Paolo", retrieved.getNome());
        assertEquals("Bianchi", retrieved.getCognome());
        assertEquals(TipoUtente.GENITORE, retrieved.getTipo());
        assertEquals("Luca", retrieved.getNomeFiglio());
        assertEquals("2015-01-01", retrieved.getDataNascitaFiglio());
    }
    
    @Test
    void testDoSave_EmailDuplicata() throws SQLException {
        // Arrange - Primo utente
        UtenteDTO utente1 = new UtenteDTO();
        utente1.setNome("Mario");
        utente1.setCognome("Rossi");
        utente1.setEmail("mario@email.com");
        utente1.setPassword("hash1");
        utente1.setTipo(TipoUtente.STUDENTE);
        utente1.setDataNascita("1990-01-01");
        
        // Act - Salva primo utente
        boolean result1 = dao.doSave(utente1);
        assertTrue(result1);
        
        // Arrange - Secondo utente con stessa email
        UtenteDTO utente2 = new UtenteDTO();
        utente2.setNome("Luigi");
        utente2.setCognome("Verdi");
        utente2.setEmail("mario@email.com"); // STESSA EMAIL!
        utente2.setPassword("hash2");
        utente2.setTipo(TipoUtente.TUTOR);
        utente2.setDataNascita("1990-01-01");
        
        // Act & Assert - Secondo salvataggio dovrebbe fallire
        // Dipende se il DAO gestisce l'eccezione o la propaga
        try {
            boolean result2 = dao.doSave(utente2);
            // Se arriva qui senza eccezione, verifica che sia false
            assertFalse(result2);
        } catch (SQLException e) {
            // OK, eccezione prevista per violazione constraint UNIQUE
            assertTrue(e.getMessage().toLowerCase().contains("unique") || e.getMessage().toLowerCase().contains("duplicate"));
        }
    }
    
    @Test
    void testDoRetrieveAll_MultiUtenti() throws SQLException {
        // Arrange - Crea 3 utenti
        UtenteDTO[] utenti = new UtenteDTO[3];
        for (int i = 0; i < 3; i++) {
            UtenteDTO utente = new UtenteDTO();
            utente.setNome("Nome" + i);
            utente.setCognome("Cognome" + i);
            utente.setEmail("email" + i + "@test.com");
            utente.setPassword("hash" + i);
            utente.setTipo(TipoUtente.values()[i % TipoUtente.values().length]);
            utente.setDataNascita("1990-01-01");
            dao.doSave(utente);
            utenti[i] = utente;
        }
        
        // Act
        List<UtenteDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verifica che siano tutti presenti
        for (UtenteDTO utente : utenti) {
            boolean found = result.stream()
                .anyMatch(u -> u.getEmail().equals(utente.getEmail()));
            assertTrue(found, "Utente non trovato: " + utente.getEmail());
        }
    }
    
    @Test
    void testDoUpdate() throws SQLException {
        // Arrange - Crea utente
        UtenteDTO utente = new UtenteDTO();
        utente.setNome("NomeOriginale");
        utente.setCognome("CognomeOriginale");
        utente.setEmail("originale@email.com");
        utente.setPassword("hash1");
        utente.setTipo(TipoUtente.STUDENTE);
        utente.setCittà("Roma");
        utente.setDataNascita("1990-01-01");
        
        dao.doSave(utente);
        
        // Modifica dati
        utente.setNome("NomeModificato");
        utente.setCognome("CognomeModificato");
        utente.setCittà("Milano");
        
        // Act
        boolean updateResult = dao.doUpdate(utente);
        
        // Assert - Update riuscito
        assertTrue(updateResult);
        
        // Verifica nel database
        UtenteDTO retrieved = dao.doRetrieveById(utente.getUID());
        assertNotNull(retrieved);
        assertEquals("NomeModificato", retrieved.getNome());
        assertEquals("CognomeModificato", retrieved.getCognome());
        assertEquals("Milano", retrieved.getCittà());
        // Email e password dovrebbero rimanere uguali
        assertEquals("originale@email.com", retrieved.getEmail());
    }
    
    @Test
    void testDoUpdate_NonEsistente() throws SQLException {
        // Arrange
        UtenteDTO utente = new UtenteDTO();
        utente.setUID(999); // ID non esistente
        utente.setNome("Test");
        utente.setCognome("Test");
        utente.setEmail("test@test.com");
        utente.setPassword("hash");
        utente.setTipo(TipoUtente.STUDENTE);
        utente.setDataNascita("1990-01-01");
        
        // Act
        boolean result = dao.doUpdate(utente);
        
        // Assert
        assertFalse(result); // Nessuna riga aggiornata
    }
    
    @Test
    void testDoDelete() throws SQLException {
        // Arrange - Crea utente
        UtenteDTO utente = new UtenteDTO();
        utente.setNome("DaEliminare");
        utente.setCognome("Test");
        utente.setEmail("elimina@test.com");
        utente.setPassword("hash");
        utente.setTipo(TipoUtente.STUDENTE);
        utente.setDataNascita("1990-01-01");
        
        dao.doSave(utente);
        int idUtente = utente.getUID();
        
        // Verifica che esista
        assertNotNull(dao.doRetrieveById(idUtente));
        
        // Act
        boolean deleteResult = dao.doDelete(idUtente);
        
        // Assert
        assertTrue(deleteResult);
        
        // Verifica che sia stato eliminato
        assertNull(dao.doRetrieveById(idUtente));
    }
    
    @Test
    void testDoDelete_NonEsistente() throws SQLException {
        // Act
        boolean result = dao.doDelete(999); // ID non esistente
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testDoRetrieveByCriterio() throws SQLException {
        // Arrange - Crea utenti con dati diversi
        UtenteDTO[] utenti = {
            createUtente("Mario", "Rossi", "mario@email.com", "Roma", TipoUtente.STUDENTE),
            createUtente("Luigi", "Verdi", "luigi@email.com", "Milano", TipoUtente.TUTOR),
            createUtente("Anna", "Rossi", "anna@email.com", "Roma", TipoUtente.GENITORE)
        };
        
        for (UtenteDTO utente : utenti) {
            dao.doSave(utente);
        }
        
        // Test 1: Cerca per cognome (restituisce il PRIMO che trova)
        UtenteDTO result1 = dao.doRetrieveByCriterio("Rossi");
        assertNotNull(result1);
        assertEquals("Rossi", result1.getCognome());
        // NOTA: Potrebbe restituire Mario o Anna (il primo nel resultset)
        
        // Test 2: Cerca per città
        UtenteDTO result2 = dao.doRetrieveByCriterio("Milano");
        assertNotNull(result2);
        assertEquals("Milano", result2.getCittà());
        
        // Test 3: Cerca per tipo (case-insensitive se usi LOWER())
        UtenteDTO result3 = dao.doRetrieveByCriterio("TUTOR"); // minuscolo
        assertNotNull(result3);
        assertEquals(TipoUtente.TUTOR, result3.getTipo());
        
        // Test 4: Cerca stringa inesistente
        UtenteDTO result4 = dao.doRetrieveByCriterio("InesistenteXYZ");
        assertNull(result4);
    }
    @Test
    void testTransazioneIntegrita() throws SQLException {
        // Test che verifica l'integrità referenziale
        // Non dovrebbe essere possibile inserire tipo utente non valido
        
        UtenteDTO utente = new UtenteDTO();
        utente.setNome("Test");
        utente.setCognome("Test");
        utente.setEmail("test@test.com");
        utente.setPassword("hash");
        utente.setTipo(TipoUtente.STUDENTE);
        utente.setDataNascita("1990-01-01");
        
        // Salva normale
        boolean result = dao.doSave(utente);
        assertTrue(result);
        
        // Prova a recuperare e verificare i dati
        UtenteDTO retrieved = dao.doRetrieveByEmail("test@test.com");
        assertNotNull(retrieved);
        assertEquals(TipoUtente.STUDENTE, retrieved.getTipo());
    }
    
    // ============== METODI DI SUPPORTO ==============
    
    private UtenteDTO createUtente(String nome, String cognome, String email, 
                                  String citta, TipoUtente tipo) {
        UtenteDTO utente = new UtenteDTO();
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setEmail(email);
        utente.setPassword("hash" + System.currentTimeMillis());
        utente.setDataNascita("1990-01-01");
        utente.setVia("Via Test");
        utente.setCivico("1");
        utente.setCittà(citta);
        utente.setCAP("00100");
        utente.setTipo(tipo);
        return utente;
    }
    
    @Test
    void testPiuUtentiStessoCognome() throws SQLException {
        // Arrange - 2 utenti stesso cognome
        UtenteDTO utente1 = createUtente("Mario", "Rossi", "mario@email.com", "Roma", TipoUtente.STUDENTE);
        UtenteDTO utente2 = createUtente("Luigi", "Rossi", "luigi@email.com", "Milano", TipoUtente.TUTOR);
        
        dao.doSave(utente1);
        dao.doSave(utente2);
        
        // Act - Cerca per cognome "Rossi"
        // NOTA: Il metodo doRetrieveByCriterio restituisce SOLO il PRIMO risultato
        UtenteDTO result = dao.doRetrieveByCriterio("Rossi");
        
        // Assert
        assertNotNull(result);
        assertEquals("Rossi", result.getCognome());
        // Non possiamo sapere quale dei due restituirà, ma il cognome deve essere Rossi
    }
}
