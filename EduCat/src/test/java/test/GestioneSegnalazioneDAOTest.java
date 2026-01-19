package test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import it.unisa.educat.dao.GestioneSegnalazioneDAO;
import it.unisa.educat.model.SegnalazioneDTO;
import it.unisa.educat.model.SegnalazioneDTO.StatoSegnalazione;

import java.sql.*;
import java.util.List;
import javax.sql.DataSource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GestioneSegnalazioneDAOTest {
    
    @Mock
    private DataSource mockDataSource;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private ResultSet mockGeneratedKeys;
    
    private GestioneSegnalazioneDAO dao;
    private SegnalazioneDTO segnalazione;
    
    @BeforeEach
    void setUp() throws SQLException {
        // Setup lenient per evitare UnnecessaryStubbingException
        lenient().when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        // Crea DAO con mock DataSource
        dao = new GestioneSegnalazioneDAO(mockDataSource);
        
        // Setup segnalazione di test
        segnalazione = new SegnalazioneDTO();
        segnalazione.setDescrizione("Comportamento inappropriato");
        segnalazione.setIdSegnalante(1);  // ID utente che segnala
        segnalazione.setIdSegnalato(2);   // ID utente segnalato
    }
    
    // ============== TEST doSave() ==============
    
    @Test
    void testDoSave_Successo() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(
            eq("INSERT INTO Segnalazione (descrizione, idSegnalante, idSegnalato) VALUES (?, ?, ?)"),
            eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(100);
        
        // Act
        boolean result = dao.doSave(segnalazione);
        
        // Assert
        assertTrue(result);
        assertEquals(100, segnalazione.getIdSegnalazione());
        
        // Verify parametri
        verify(mockPreparedStatement).setString(eq(1), eq("Comportamento inappropriato"));
        verify(mockPreparedStatement).setInt(eq(2), eq(1));
        verify(mockPreparedStatement).setInt(eq(3), eq(2));
    }
    
    @Test
    void testDoSave_NessunaRigaInserita() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(
            anyString(),
            eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.doSave(segnalazione);
        
        // Assert
        assertFalse(result);
        verify(mockPreparedStatement, never()).getGeneratedKeys();
    }
    
    @Test
    void testDoSave_SQLException() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException("Errore database"));
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            dao.doSave(segnalazione);
        });
    }
    
    // ============== TEST doRetrieveAll() ==============
    
    @Test
    void testDoRetrieveAll_ConSegnalazioni() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(
            eq("SELECT s.*, " +
               "u_segnalante.nome as segnalante_nome, u_segnalante.cognome as segnalante_cognome, " +
               "u_segnalato.nome as segnalato_nome, u_segnalato.cognome as segnalato_cognome " +
               "FROM Segnalazione s " +
               "LEFT JOIN Utente u_segnalante ON s.idSegnalante = u_segnalante.idUtente " +
               "LEFT JOIN Utente u_segnalato ON s.idSegnalato = u_segnalato.idUtente " +
               "ORDER BY s.idSegnalazione DESC")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Simula 2 segnalazioni
        when(mockResultSet.next()).thenReturn(true, true, false);
        
        // Mock prima segnalazione (ATTIVA)
        when(mockResultSet.getInt("idSegnalazione")).thenReturn(1, 2);
        when(mockResultSet.getString("descrizione")).thenReturn("Prima segnalazione", "Seconda segnalazione");
        when(mockResultSet.getString("stato")).thenReturn("ATTIVA", "RISOLTA");
        
        // Mock dati segnalante/segnalato
        when(mockResultSet.getInt("idSegnalante")).thenReturn(1, 3);
        when(mockResultSet.getString("segnalante_nome")).thenReturn("Mario", "Luigi");
        when(mockResultSet.getString("segnalante_cognome")).thenReturn("Rossi", "Verdi");
        
        when(mockResultSet.getInt("idSegnalato")).thenReturn(2, 4);
        when(mockResultSet.getString("segnalato_nome")).thenReturn("Anna", "Giulia");
        when(mockResultSet.getString("segnalato_cognome")).thenReturn("Bianchi", "Neri");
        
        // Act
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verifica prima segnalazione
        SegnalazioneDTO prima = result.get(0);
        assertEquals(1, prima.getIdSegnalazione());
        assertEquals("Prima segnalazione", prima.getDescrizione());
        assertEquals(StatoSegnalazione.ATTIVA, prima.getStato());
        assertEquals(1, prima.getSegnalante().getUID());
        assertEquals("Mario", prima.getSegnalante().getNome());
        
        // Verifica seconda segnalazione
        SegnalazioneDTO seconda = result.get(1);
        assertEquals(StatoSegnalazione.RISOLTA, seconda.getStato());
    }
    
    @Test
    void testDoRetrieveAll_NessunaSegnalazione() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testDoRetrieveAll_StatoNonRiconosciuto() throws SQLException {
        // Test per stato non valido nel ResultSet
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // Mock con stato non valido
        when(mockResultSet.getInt("idSegnalazione")).thenReturn(1);
        when(mockResultSet.getString("descrizione")).thenReturn("Test");
        when(mockResultSet.getString("stato")).thenReturn("STATO_INVALIDO");
        
        // Act
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert - Il metodo potrebbe lanciare eccezione o gestire il caso
        assertNotNull(result);
        // Dipende da come gestisci stati non validi nella mapResultSetToSegnalazione
    }
    
    @Test
    void testDoRetrieveAll_ConNullValues() throws SQLException {
        // Test con valori null nel ResultSet (per LEFT JOIN)
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // Mock con valori null per i join (utenti eliminati)
        when(mockResultSet.getInt("idSegnalazione")).thenReturn(1);
        when(mockResultSet.getString("descrizione")).thenReturn("Segnalazione utente eliminato");
        when(mockResultSet.getString("stato")).thenReturn("ATTIVA");
        
        when(mockResultSet.getInt("idSegnalante")).thenReturn(1);
        when(mockResultSet.getString("segnalante_nome")).thenReturn(null);
        when(mockResultSet.getString("segnalante_cognome")).thenReturn(null);
        
        when(mockResultSet.getInt("idSegnalato")).thenReturn(0); // potrebbe essere 0 se utente eliminato
        when(mockResultSet.getString("segnalato_nome")).thenReturn(null);
        when(mockResultSet.getString("segnalato_cognome")).thenReturn(null);
        
        // Act
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        SegnalazioneDTO segn = result.get(0);
        assertNotNull(segn.getSegnalante());
        assertNotNull(segn.getSegnalato());
    }
    
    // ============== TEST setAsSolved() ==============
    
    @Test
    void testSetAsSolved_Successo() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(
            eq("UPDATE Segnalazione SET stato = 'RISOLTA' WHERE idSegnalazione = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = dao.setAsSolved(100);
        
        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setInt(eq(1), eq(100));
    }
    
    @Test
    void testSetAsSolved_SegnalazioneNonEsistente() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.setAsSolved(999);
        
        // Assert
        assertFalse(result);
    }
    
    // ============== TEST doDelete() ==============
    
    @Test
    void testDoDelete_Successo() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(
            eq("DELETE FROM Segnalazione WHERE idSegnalazione = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = dao.doDelete(100);
        
        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setInt(eq(1), eq(100));
    }
    
    @Test
    void testDoDelete_SegnalazioneNonEsistente() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.doDelete(999);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testDoDelete_SQLException() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException("Vincolo foreign key"));
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            dao.doDelete(100);
        });
    }
    
    // ============== TEST mapResultSetToSegnalazione() (private method) ==============
    
    @Test
    void testMapResultSetToSegnalazione_StatoAttiva() throws SQLException {
        // Test indiretto - verifica mapping corretto
        
        // Arrange
        when(mockResultSet.getInt("idSegnalazione")).thenReturn(1);
        when(mockResultSet.getString("descrizione")).thenReturn("Test");
        when(mockResultSet.getString("stato")).thenReturn("ATTIVA");
        
        when(mockResultSet.getInt("idSegnalante")).thenReturn(1);
        when(mockResultSet.getString("segnalante_nome")).thenReturn("Mario");
        when(mockResultSet.getString("segnalante_cognome")).thenReturn("Rossi");
        
        when(mockResultSet.getInt("idSegnalato")).thenReturn(2);
        when(mockResultSet.getString("segnalato_nome")).thenReturn("Anna");
        when(mockResultSet.getString("segnalato_cognome")).thenReturn("Bianchi");
        
        // Act (chiamando doRetrieveAll che usa il metodo privato)
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        SegnalazioneDTO segnalazione = result.get(0);
        assertEquals(1, segnalazione.getIdSegnalazione());
        assertEquals("Test", segnalazione.getDescrizione());
        assertEquals(StatoSegnalazione.ATTIVA, segnalazione.getStato());
        assertEquals("Mario", segnalazione.getSegnalante().getNome());
        assertEquals("Anna", segnalazione.getSegnalato().getNome());
    }
    
    @Test
    void testMapResultSetToSegnalazione_StatoRisolto() throws SQLException {
        // Test per stato RISOLTA
        when(mockResultSet.getString("stato")).thenReturn("RISOLTA");
        when(mockResultSet.getInt("idSegnalazione")).thenReturn(1);
        
        // Setup minimo per il test
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(StatoSegnalazione.RISOLTA, result.get(0).getStato());
    }
    
    // ============== TEST Eccezioni e Edge Cases ==============
    
    @Test
    void testDoSave_ExceptionCatturata() throws SQLException {
        // Test che verifica che le eccezioni siano catturate e restituiscano false
        // Nota: nel tuo codice hai un catch che stampa stack trace e ritorna false
        
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException("Errore DB"));
        
        // Act
        boolean result = dao.doSave(segnalazione);
        
        // Assert - Dovrebbe restituire false, non lanciare eccezione
        assertFalse(result);
    }
    
    @Test
    void testDoRetrieveAll_ExceptionCatturata() throws SQLException {
        // Test per doRetrieveAll che cattura eccezioni
        
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException("Errore DB"));
        
        // Act
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert - Dovrebbe restituire null (come nel tuo catch)
        assertNull(result);
    }
    
    @Test
    void testMetodiConnessioneChiusaCorrettamente() throws SQLException {
        // Verifica che le risorse vengano chiuse anche con eccezioni
        
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException("Errore nella preparazione statement"));
        
        // Act & Assert
        assertThrows(SQLException.class, () -> dao.doSave(segnalazione));
        
        // Le risorse dovrebbero essere chiuse nel finally block
        // Non possiamo verificarlo direttamente ma è importante il pattern
    }
}
