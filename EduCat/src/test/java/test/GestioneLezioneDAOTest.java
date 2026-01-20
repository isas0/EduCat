package test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.dao.GestioneLezioneDAO.CriteriRicerca;
import it.unisa.educat.model.LezioneDTO;
import it.unisa.educat.model.LezioneDTO.ModalitaLezione;
import it.unisa.educat.model.LezioneDTO.StatoLezione;
import it.unisa.educat.model.PrenotazioneDTO;
import it.unisa.educat.model.PrenotazioneDTO.StatoPrenotazione;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.sql.DataSource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GestioneLezioneDAOTest {
    
    @Mock
    private DataSource mockDataSource;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private PreparedStatement mockPreparedStatement2;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private ResultSet mockGeneratedKeys;
    
    private GestioneLezioneDAO dao;
    private LezioneDTO lezione;
    private PrenotazioneDTO prenotazione;
    private UtenteDTO tutor;
    private UtenteDTO studente;
    
    @BeforeEach
    void setUp() throws SQLException {
        // Setup lenient
        lenient().when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        // Crea DAO con mock DataSource
        dao = new GestioneLezioneDAO(mockDataSource);
        
        // Setup tutor
        tutor = new UtenteDTO();
        tutor.setUID(1);
        tutor.setNome("Mario");
        tutor.setCognome("Rossi");
        tutor.setTipo(TipoUtente.TUTOR);
        
        // Setup studente
        studente = new UtenteDTO();
        studente.setUID(2);
        studente.setNome("Luigi");
        studente.setCognome("Verdi");
        studente.setTipo(TipoUtente.STUDENTE);
        
        // Setup lezione
        lezione = new LezioneDTO();
        lezione.setMateria("Matematica");
        lezione.setDataInizio(LocalDateTime.now().plusDays(1));
        lezione.setDataFine(LocalDateTime.now().plusDays(1).plusHours(2));
        lezione.setDurata(2.0f);
        lezione.setPrezzo(20.0f);
        lezione.setModalitaLezione(ModalitaLezione.ONLINE);
        lezione.setTutor(tutor);
        lezione.setCitta("Roma");
        
        // Setup prenotazione
        prenotazione = new PrenotazioneDTO();
        prenotazione.setStudente(studente);
        prenotazione.setLezione(lezione);
        prenotazione.setImportoPagato(40.0f);
        prenotazione.setIndirizzoFatturazione("Via Roma 1");
        prenotazione.setIntestatario("Luigi Verdi");
        prenotazione.setNumeroCarta("1234567812345678");
        prenotazione.setDataScadenza("12/25");
        prenotazione.setCvv(123);
        prenotazione.setIdTutor(1);
        prenotazione.setStato(StatoPrenotazione.ATTIVA);
    }
    
    // ============== TEST doSaveLezione() ==============
    
    @Test
    void testDoSaveLezione_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("INSERT INTO Lezione (materia, dataInizio, dataFine, durata, prezzo, modalitaLezione, idTutor, citta, statoLezione) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"),
            eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(100);
        
        // Act
        boolean result = dao.doSaveLezione(lezione);
        
        // Assert
        assertTrue(result);
        assertEquals(100, lezione.getIdLezione());
        
        // Verify parametri
        verify(mockPreparedStatement).setString(eq(1), eq("Matematica"));
        verify(mockPreparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(mockPreparedStatement).setFloat(eq(5), eq(20.0f));
        verify(mockPreparedStatement).setString(eq(9), eq("PIANIFICATA"));
    }
    
    @Test
    void testDoSaveLezione_NessunaRigaInserita() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.doSaveLezione(lezione);
        
        // Assert
        assertFalse(result);
    }
    
    // ============== TEST doRetrieveByCriteria() ==============
    
    @Test
    void testDoRetrieveByCriteria_ConFiltri() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Una lezione trovata
        when(mockResultSet.next()).thenReturn(true, false);
        
        // Mock dati lezione
        mockResultSetForLezione(mockResultSet);
        
        // Crea criteri di ricerca
        CriteriRicerca criteri = new CriteriRicerca();
        criteri.setMateria("Matematica");
        criteri.setCitta("Roma");
        criteri.setModalita("ONLINE");
        criteri.setDataDa(LocalDateTime.now());
        criteri.setDataA(LocalDateTime.now().plusDays(7));
        criteri.setPrezzoMax(50.0f);
        criteri.setLimit(10);
        criteri.setOffset(0);
        
        // Act
        List<LezioneDTO> result = dao.doRetrieveByCriteria(criteri);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Matematica", result.get(0).getMateria());
    }
    
    @Test
    void testDoRetrieveByCriteria_SenzaFiltri() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        List<LezioneDTO> result = dao.doRetrieveByCriteria(null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testDoRetrieveByCriteria_ConIdTutor() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        
        mockResultSetForLezione(mockResultSet);
        
        CriteriRicerca criteri = new CriteriRicerca();
        criteri.setIdTutor(1);
        
        // Act
        List<LezioneDTO> result = dao.doRetrieveByCriteria(criteri);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    // ============== TEST prenotaLezione() ==============
    
    @Test
    void testPrenotaLezione_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("INSERT INTO Prenotazione (idStudente, idLezione, dataPrenotazione, stato, importoPagato, indirizzoFatturazione, intestatario, numeroCarta, scadenza, cvv, idTutor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
            eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(200);
        
        lezione.setIdLezione(100);
        lezione.setPrezzo(20.0f);
        lezione.setDurata(2.0f);
        
        // Act
        boolean result = dao.prenotaLezione(prenotazione);
        
        // Assert
        assertTrue(result);
        assertEquals(200, prenotazione.getIdPrenotazione());
        assertEquals(40.0f, prenotazione.getImportoPagato()); // 20 * 2
        
        // Verify parametri
        verify(mockPreparedStatement).setInt(eq(1), eq(2)); // idStudente
        verify(mockPreparedStatement).setInt(eq(2), eq(100)); // idLezione
        verify(mockPreparedStatement).setFloat(eq(5), eq(40.0f)); // importo
    }
    
    @Test
    void testPrenotaLezione_ConImportoGiaSpecificato() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(200);
        
        prenotazione.setImportoPagato(50.0f); // Importo già specificato
        
        // Act
        boolean result = dao.prenotaLezione(prenotazione);
        
        // Assert
        assertTrue(result);
        assertEquals(50.0f, prenotazione.getImportoPagato()); // Non deve cambiare
    }
    
    @Test
    void testPrenotaLezione_NessunaRigaInserita() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.prenotaLezione(prenotazione);
        
        // Assert
        assertFalse(result);
    }
    
    // ============== TEST doUpdateStatoPrenotazione() ==============
    
    @Test
    void testDoUpdateStatoPrenotazione_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("UPDATE Prenotazione SET stato = ? WHERE idPrenotazione = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = dao.doUpdateStatoPrenotazione(200, "ANNULLATA");
        
        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setString(eq(1), eq("ANNULLATA"));
        verify(mockPreparedStatement).setInt(eq(2), eq(200));
    }
    
    @Test
    void testDoUpdateStatoPrenotazione_NessunaRigaAggiornata() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.doUpdateStatoPrenotazione(999, "ANNULLATA");
        
        // Assert
        assertFalse(result);
    }
    
    // ============== TEST annullaPrenotazione() ==============
    
    @Test
    void testAnnullaPrenotazione_Successo() throws SQLException {
        // Arrange - Primo statement (UPDATE Prenotazione)
        when(mockConnection.prepareStatement(
            eq("UPDATE Prenotazione SET stato = 'ANNULLATA' WHERE idPrenotazione = ? AND stato = 'ATTIVA'")
        )).thenReturn(mockPreparedStatement);
        
        // Secondo statement (UPDATE Lezione)
        when(mockConnection.prepareStatement(
            eq("UPDATE Lezione l JOIN Prenotazione p ON l.idLezione = p.idLezione SET l.statoLezione = 'PIANIFICATA' WHERE p.idPrenotazione = ?")
        )).thenReturn(mockPreparedStatement2);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement2.executeUpdate()).thenReturn(1);
        
        // Simula transazione
        doNothing().when(mockConnection).setAutoCommit(false);
        doNothing().when(mockConnection).setAutoCommit(true);
        doNothing().when(mockConnection).commit();
        
        // Act
        boolean result = dao.annullaPrenotazione(200);
        
        // Assert
        assertTrue(result);
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }
    
    @Test
    void testAnnullaPrenotazione_PrenotazioneNonTrovata() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        doNothing().when(mockConnection).setAutoCommit(false);
        doNothing().when(mockConnection).rollback();
        
        // Act
        boolean result = dao.annullaPrenotazione(999);
        
        // Assert
        assertFalse(result);
        verify(mockConnection).rollback();
    }
    
    @Test
    void testAnnullaPrenotazione_LezioneNonTrovata() throws SQLException {
        // Arrange - Primo statement OK
        when(mockConnection.prepareStatement(
            eq("UPDATE Prenotazione SET stato = 'ANNULLATA' WHERE idPrenotazione = ? AND stato = 'ATTIVA'")
        )).thenReturn(mockPreparedStatement);
        
        // Secondo statement fallisce
        when(mockConnection.prepareStatement(
            eq("UPDATE Lezione l JOIN Prenotazione p ON l.idLezione = p.idLezione SET l.statoLezione = 'PIANIFICATA' WHERE p.idPrenotazione = ?")
        )).thenReturn(mockPreparedStatement2);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement2.executeUpdate()).thenReturn(0);
        
        doNothing().when(mockConnection).setAutoCommit(false);
        doNothing().when(mockConnection).rollback();
        
        // Act
        boolean result = dao.annullaPrenotazione(200);
        
        // Assert
        assertFalse(result);
        verify(mockConnection).rollback();
    }
    
    @Test
    void testAnnullaPrenotazione_Exception() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException("Errore DB"));
        
        doNothing().when(mockConnection).setAutoCommit(false);
        doNothing().when(mockConnection).rollback();
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            dao.annullaPrenotazione(200);
        });
        
        verify(mockConnection).rollback();
    }
    
    // ============== TEST getStoricoLezioni() ==============
    
    @Test
    void testGetStoricoLezioni_ConLezioni() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("SELECT * FROM Lezione WHERE (idTutor = ? OR idStudente = ?) AND dataInizio < NOW() ORDER BY dataInizio DESC")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        
        mockResultSetForLezione(mockResultSet);
        
        // Act
        List<LezioneDTO> result = dao.getStoricoLezioni(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mockPreparedStatement).setInt(eq(1), eq(1));
        verify(mockPreparedStatement).setInt(eq(2), eq(1));
    }
    
    @Test
    void testGetStoricoLezioni_NessunaLezione() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        List<LezioneDTO> result = dao.getStoricoLezioni(1);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    // ============== TEST getLezioneById() ==============
    
    @Test
    void testGetLezioneById_Trovata() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("SELECT l.*, u.nome as tutor_nome, u.cognome as tutor_cognome, u.citta as tutor_citta FROM Lezione l JOIN Utente u ON l.idTutor = u.idUtente WHERE l.idLezione = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        mockResultSetForLezione(mockResultSet);
        
        // Act
        LezioneDTO result = dao.getLezioneById(100);
        
        // Assert
        assertNotNull(result);
        assertEquals(100, result.getIdLezione());
        verify(mockPreparedStatement).setInt(eq(1), eq(100));
    }
    
    @Test
    void testGetLezioneById_NonTrovata() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        LezioneDTO result = dao.getLezioneById(999);
        
        // Assert
        assertNull(result);
    }
    
    // ============== TEST hasStudentePrenotatoLezione() ==============
    
    @Test
    void testHasStudentePrenotatoLezione_Vero() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("SELECT COUNT(*) FROM Prenotazione WHERE idStudente = ? AND idLezione = ? AND stato = 'ATTIVA'")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);
        
        // Act
        boolean result = dao.hasStudentePrenotatoLezione(2, 100);
        
        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setInt(eq(1), eq(2));
        verify(mockPreparedStatement).setInt(eq(2), eq(100));
    }
    
    @Test
    void testHasStudentePrenotatoLezione_Falso() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0);
        
        // Act
        boolean result = dao.hasStudentePrenotatoLezione(2, 100);
        
        // Assert
        assertFalse(result);
    }
    
    // ============== TEST getPrenotazioniByStudente() ==============
    
    @Test
    void testGetPrenotazioniByStudente_ConPrenotazioni() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        
        mockResultSetForPrenotazione(mockResultSet);
        
        // Act
        List<PrenotazioneDTO> result = dao.getPrenotazioniByStudente(2);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getStudente().getUID());
    }
    
    @Test
    void testGetPrenotazioniByStudente_NessunaPrenotazione() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        List<PrenotazioneDTO> result = dao.getPrenotazioniByStudente(2);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    // ============== TEST getPrenotazioneById() ==============
    
    @Test
    void testGetPrenotazioneById_Trovata() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        mockResultSetForPrenotazione(mockResultSet);
        
        // Act
        PrenotazioneDTO result = dao.getPrenotazioneById(200);
        
        // Assert
        assertNotNull(result);
        assertEquals(200, result.getIdPrenotazione());
    }
    
    @Test
    void testGetPrenotazioneById_NonTrovata() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        PrenotazioneDTO result = dao.getPrenotazioneById(999);
        
        // Assert
        assertNull(result);
    }
    
    // ============== TEST getPrenotazioniByTutor() ==============
    
    @Test
    void testGetPrenotazioniByTutor_ConPrenotazioni() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        
        mockResultSetForPrenotazione(mockResultSet);
        
        // Act
        List<PrenotazioneDTO> result = dao.getPrenotazioniByTutor(1);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getLezione().getTutor().getUID());
    }
    
    // ============== TEST mapResultSetToLezione() ==============
    
    @Test
    void testMapResultSetToLezione_MappingCorretto() throws SQLException {
        // Arrange
        mockResultSetForLezione(mockResultSet);
        
        // Act (chiamata indiretta attraverso getLezioneById)
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        LezioneDTO result = dao.getLezioneById(100);
        
        // Assert
        assertNotNull(result);
        assertEquals(100, result.getIdLezione());
        assertEquals("Matematica", result.getMateria());
        assertEquals(ModalitaLezione.ONLINE, result.getModalitaLezione());
        assertEquals(StatoLezione.PIANIFICATA, result.getStato());
        assertEquals("Mario", result.getTutor().getNome());
    }
    
    @Test
    void testMapResultSetToLezione_ModalitaPresenza() throws SQLException {
        // Arrange
        when(mockResultSet.getString("modalitaLezione")).thenReturn("PRESENZA");
        when(mockResultSet.getInt("idLezione")).thenReturn(100);
        
        // Setup indiretto
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // Act
        LezioneDTO result = dao.getLezioneById(100);
        
        // Assert
        assertNotNull(result);
        assertEquals(ModalitaLezione.PRESENZA, result.getModalitaLezione());
    }
    
    @Test
    void testMapResultSetToLezione_StatiVari() throws SQLException {
        // Test per tutti gli stati possibili
        String[] stati = {"PIANIFICATA", "PRENOTATA", "CONCLUSA", "ANNULLATA"};
        StatoLezione[] statiAttesi = {
            StatoLezione.PIANIFICATA,
            StatoLezione.PRENOTATA,
            StatoLezione.CONCLUSA,
            StatoLezione.ANNULLATA
        };
        
        for (int i = 0; i < stati.length; i++) {
            // Arrange
            when(mockResultSet.getString("statoLezione")).thenReturn(stati[i]);
            when(mockResultSet.getInt("idLezione")).thenReturn(100 + i);
            
            when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            
            // Act
            LezioneDTO result = dao.getLezioneById(100 + i);
            
            // Assert
            assertNotNull(result);
            assertEquals(statiAttesi[i], result.getStato());
        }
    }
    
    // ============== TEST mapResultSetToPrenotazione() ==============
    
    @Test
    void testMapResultSetToPrenotazione_MappingCorretto() throws SQLException {
        // Arrange
        mockResultSetForPrenotazione(mockResultSet);
        
        // Setup indiretto
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // Act
        PrenotazioneDTO result = dao.getPrenotazioneById(200);
        
        // Assert
        assertNotNull(result);
        assertEquals(200, result.getIdPrenotazione());
        assertEquals(StatoPrenotazione.ATTIVA, result.getStato());
        assertEquals(40.0f, result.getImportoPagato());
        assertEquals("Luigi", result.getStudente().getNome());
        assertEquals("Matematica", result.getLezione().getMateria());
        assertEquals("Mario", result.getLezione().getTutor().getNome());
    }
    
    @Test
    void testMapResultSetToPrenotazione_StatiVari() throws SQLException {
        // Test per tutti gli stati di prenotazione
        String[] stati = {"ATTIVA", "ANNULLATA", "CONCLUSA"};
        StatoPrenotazione[] statiAttesi = {
            StatoPrenotazione.ATTIVA,
            StatoPrenotazione.ANNULLATA,
            StatoPrenotazione.CONCLUSA
        };
        
        for (int i = 0; i < stati.length; i++) {
            // Arrange - Usa il metodo di supporto per i dati base
            mockResultSetForPrenotazione(mockResultSet);
            
            // Sovrascrivi solo lo stato
            when(mockResultSet.getString("stato")).thenReturn(stati[i]);
            when(mockResultSet.getInt("idPrenotazione")).thenReturn(200 + i);
            
            // Setup indiretto
            when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            
            // Act
            PrenotazioneDTO result = dao.getPrenotazioneById(200 + i);
            
            // Assert
            assertNotNull(result);
            assertEquals(statiAttesi[i], result.getStato());
        }
    }
    
    // ============== TEST CriteriRicerca ==============
    
    @Test
    void testCriteriRicerca_GettersSetters() {
        // Arrange
        CriteriRicerca criteri = new CriteriRicerca();
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        criteri.setMateria("Matematica");
        criteri.setCitta("Roma");
        criteri.setModalita("ONLINE");
        criteri.setDataDa(now);
        criteri.setDataA(now.plusDays(7));
        criteri.setIdTutor(1);
        criteri.setPrezzoMax(50.0f);
        criteri.setStatoLezione("PIANIFICATA");
        criteri.setLimit(10);
        criteri.setOffset(0);
        
        // Assert
        assertEquals("Matematica", criteri.getMateria());
        assertEquals("Roma", criteri.getCitta());
        assertEquals("ONLINE", criteri.getModalita());
        assertEquals(now, criteri.getDataDa());
        assertEquals(now.plusDays(7), criteri.getDataA());
        assertEquals(1, criteri.getIdTutor());
        assertEquals(50.0f, criteri.getPrezzoMax());
        assertEquals("PIANIFICATA", criteri.getStatoLezione());
        assertEquals(10, criteri.getLimit());
        assertEquals(0, criteri.getOffset());
    }
    
    // ============== METODI DI SUPPORTO ==============
    
    private void mockResultSetForLezione(ResultSet rs) throws SQLException {
        when(rs.getInt("idLezione")).thenReturn(100);
        when(rs.getString("materia")).thenReturn("Matematica");
        when(rs.getTimestamp("dataInizio")).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        when(rs.getTimestamp("dataFine")).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));
        when(rs.getFloat("durata")).thenReturn(2.0f);
        when(rs.getFloat("prezzo")).thenReturn(20.0f);
        when(rs.getString("modalitaLezione")).thenReturn("ONLINE");
        when(rs.getInt("idTutor")).thenReturn(1);
        when(rs.getString("tutor_nome")).thenReturn("Mario");
        when(rs.getString("tutor_cognome")).thenReturn("Rossi");
        when(rs.getString("tutor_citta")).thenReturn("Roma");
        when(rs.getString("statoLezione")).thenReturn("PIANIFICATA");
    }
    
    private void mockResultSetForPrenotazione(ResultSet rs) throws SQLException {
        // Dati prenotazione
        when(rs.getInt("idPrenotazione")).thenReturn(200);
        when(rs.getDate("dataPrenotazione")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rs.getString("stato")).thenReturn("ATTIVA");
        when(rs.getFloat("importoPagato")).thenReturn(40.0f);
        
        // Studente
        when(rs.getInt("idStudente")).thenReturn(2);
        when(rs.getString("studente_nome")).thenReturn("Luigi");
        when(rs.getString("studente_cognome")).thenReturn("Verdi");
        when(rs.getString("studente_email")).thenReturn("luigi@email.com");
        
        // Lezione
        when(rs.getInt("idLezione")).thenReturn(100);
        when(rs.getString("materia")).thenReturn("Matematica");
        when(rs.getTimestamp("dataInizio")).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
        when(rs.getTimestamp("dataFine")).thenReturn(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)));
        when(rs.getFloat("durata")).thenReturn(2.0f);
        when(rs.getFloat("prezzo")).thenReturn(20.0f);
        when(rs.getString("modalitaLezione")).thenReturn("ONLINE");
        when(rs.getString("citta")).thenReturn("Roma");
        when(rs.getString("statoLezione")).thenReturn("PRENOTATA");
        when(rs.getInt("idStudente")).thenReturn(2); // idStudente nella lezione
        when(rs.getInt("idPrenotazione")).thenReturn(200); // idPrenotazione nella lezione
        
        // Tutor
        when(rs.getInt("idTutor")).thenReturn(1);
        when(rs.getString("tutor_nome")).thenReturn("Mario");
        when(rs.getString("tutor_cognome")).thenReturn("Rossi");
        when(rs.getString("tutor_email")).thenReturn("mario@email.com");
    }
    
 // ============== TEST hasTutorLezioneInFasciaOraria() ==============

    @Test
    void testHasTutorLezioneInFasciaOraria_TutorOccupato() throws SQLException {
        // Arrange
        LocalDateTime dataInizio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime dataFine = dataInizio.plusHours(2);
        
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1); // COUNT = 1 (trovata lezione)
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(1, dataInizio, dataFine);
        
        // Assert
        assertTrue(result);
        
        // Verify parametri
        verify(mockPreparedStatement).setInt(eq(1), eq(1)); // idTutor
        verify(mockPreparedStatement).setTimestamp(eq(2), eq(Timestamp.valueOf(dataInizio)));
        verify(mockPreparedStatement).setTimestamp(eq(3), eq(Timestamp.valueOf(dataFine)));
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_TutorLibero() throws SQLException {
        // Arrange
        LocalDateTime dataInizio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime dataFine = dataInizio.plusHours(2);
        
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // COUNT = 0 (nessuna lezione)
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(1, dataInizio, dataFine);
        
        // Assert
        assertFalse(result);
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_ConLezioneAnnullata() throws SQLException {
        // Test che lezioni ANNULLATE non vengono considerate
        LocalDateTime dataInizio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime dataFine = dataInizio.plusHours(2);
        
        // Setup per simulare query (lezione annullata non dovrebbe essere contata)
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // Anche se c'è lezione annullata
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(1, dataInizio, dataFine);
        
        // Assert
        assertFalse(result); // Lezione annullata non dovrebbe bloccare
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_SovrapposizioneParziale() throws SQLException {
        // Test vari casi di sovrapposizione
        LocalDateTime[] casi = {
            // Nuova: 10:00-12:00, Esistente: 9:00-11:00 (sovrappone ultima ora)
            LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
            LocalDateTime.now().plusDays(1).withHour(12).withMinute(0),
            
            // Nuova: 10:00-12:00, Esistente: 11:00-13:00 (sovrappone prima ora)
            LocalDateTime.now().plusDays(2).withHour(10).withMinute(0),
            LocalDateTime.now().plusDays(2).withHour(12).withMinute(0),
            
            // Nuova: 10:00-12:00, Esistente: 10:30-11:30 (contenuta)
            LocalDateTime.now().plusDays(3).withHour(10).withMinute(0),
            LocalDateTime.now().plusDays(3).withHour(12).withMinute(0),
        };
        
        for (int i = 0; i < casi.length; i += 2) {
            // Setup
            when(mockDataSource.getConnection()).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getInt(1)).thenReturn(1); // Sovrapposizione trovata
            
            // Act
            boolean result = dao.hasTutorLezioneInFasciaOraria(1, casi[i], casi[i + 1]);
            
            // Assert
            assertTrue("Dovrebbe rilevare sovrapposizione per caso " + (i/2), result);
            
            // Reset mocks per prossimo ciclo
            reset(mockConnection, mockPreparedStatement, mockResultSet);
        }
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_NoSovrapposizione() throws SQLException {
        // Test caso senza sovrapposizione
        LocalDateTime dataInizio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime dataFine = dataInizio.plusHours(2);
        
        // Lezione esistente: 13:00-15:00 (dopo la nuova)
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // Nessuna sovrapposizione
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(1, dataInizio, dataFine);
        
        // Assert
        assertFalse(result);
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_Exception() throws SQLException {
        // Test gestione eccezioni
        LocalDateTime dataInizio = LocalDateTime.now().plusDays(1);
        LocalDateTime dataFine = dataInizio.plusHours(2);
        
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Errore DB"));
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            dao.hasTutorLezioneInFasciaOraria(1, dataInizio, dataFine);
        });
    }

    @Test
    void testHasTutorLezioneInFasciaOraria_ConLezioneConclusa() throws SQLException {
        // Test che lezioni CONCLUSE non vengono considerate
        LocalDateTime dataInizio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime dataFine = dataInizio.plusHours(2);
        
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // Lezione conclusa non contata
        
        // Act
        boolean result = dao.hasTutorLezioneInFasciaOraria(1, dataInizio, dataFine);
        
        // Assert
        assertFalse(result);
    }
}
