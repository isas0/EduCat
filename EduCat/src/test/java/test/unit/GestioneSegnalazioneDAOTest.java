package test.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
        // Re-inizializza i mock
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockGeneratedKeys = mock(ResultSet.class);
        
        // Oppure usa MockitoAnnotations
        // MockitoAnnotations.openMocks(this);
        
        dao = new GestioneSegnalazioneDAO(mockDataSource);
        
        segnalazione = new SegnalazioneDTO();
        segnalazione.setDescrizione("Comportamento inappropriato");
        segnalazione.setIdSegnalante(1);
        segnalazione.setIdSegnalato(2);
        segnalazione.setStato(StatoSegnalazione.ATTIVA);
    }
    
    
    
    // ============== TEST doSave() ==============
    
    @Test
    void testDoSave_Successo() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(
            eq("INSERT INTO Segnalazione (descrizione, idSegnalante, idSegnalato) VALUES (?, ?, ?)"),
            anyInt()
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

        when(mockConnection.prepareStatement(anyString(), anyInt()))
        .thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.doSave(segnalazione);
        
        // Assert
        assertFalse(result);
        verify(mockPreparedStatement, never()).getGeneratedKeys();
    }
    
    
    // ============== TEST doRetrieveAll() ==============
    
    @Test
    void testDoRetrieveAll_ConSegnalazioni() throws SQLException {
        // Arrange
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // SOLO stubbing ESSENZIALI
        when(mockResultSet.next()).thenReturn(true, false); // Solo UNA segnalazione
        
        // MINIMO indispensabile per fare passare il test
        when(mockResultSet.getInt("idSegnalazione")).thenReturn(1);
        when(mockResultSet.getString("descrizione")).thenReturn("Test");
        when(mockResultSet.getString("stato")).thenReturn("ATTIVA");
        when(mockResultSet.getInt("idSegnalante")).thenReturn(1);
        when(mockResultSet.getString("segnalante_nome")).thenReturn("Mario");
        when(mockResultSet.getString("segnalante_cognome")).thenReturn("Rossi");
        when(mockResultSet.getInt("idSegnalato")).thenReturn(2);
        when(mockResultSet.getString("segnalato_nome")).thenReturn("Anna");
        when(mockResultSet.getString("segnalato_cognome")).thenReturn("Bianchi");
        
        // Act
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
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
    void testDoRetrieveAll_ConNullValues() throws SQLException {
        // 1. Crea un ResultSet NUOVO solo per questo test
        ResultSet localResultSet = Mockito.mock(ResultSet.class);
        
        // 2. Setup minimale
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(localResultSet); // <-- USA IL NUOVO
        
        // 3. Setup del ResultSet LOCALE
        when(localResultSet.next()).thenReturn(true, false); // IMPORTANTE: false alla fine!
        
        // 4. Setup dei valori - MINIMO indispensabile
        when(localResultSet.getInt("idSegnalazione")).thenReturn(1);
        when(localResultSet.getString("descrizione")).thenReturn("Test");
        when(localResultSet.getString("stato")).thenReturn("ATTIVA");
        when(localResultSet.getInt("idSegnalante")).thenReturn(1);
        when(localResultSet.getInt("idSegnalato")).thenReturn(2);
        
        // 5. Valori null (questo è il punto del test)
        when(localResultSet.getString("segnalante_nome")).thenReturn(null);
        when(localResultSet.getString("segnalante_cognome")).thenReturn(null);
        when(localResultSet.getString("segnalato_nome")).thenReturn(null);
        when(localResultSet.getString("segnalato_cognome")).thenReturn(null);
        
        // 6. Esegui
        List<SegnalazioneDTO> result = dao.doRetrieveAll();
        
        // 7. Verifica
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getSegnalante());
        assertNotNull(result.get(0).getSegnalato());
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
}
