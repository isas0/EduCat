package test.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import it.unisa.educat.dao.GestioneUtenzaDAO;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GestioneUtenzaDAOTest {
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockPreparedStatement;
    
    @Mock
    private PreparedStatement mockPreparedStatementGenitore;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private ResultSet mockGeneratedKeys;
    
    @Mock
    private DataSource mockDataSource;
    
    private GestioneUtenzaDAO dao;
    private UtenteDTO utenteStudente;
    private UtenteDTO utenteGenitore;
    
    @BeforeEach
    void setUp() throws SQLException {
        // Setup base
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        // Crea DAO con mock DataSource
        dao = new GestioneUtenzaDAO(mockDataSource);
        
        // Setup utente studente
        utenteStudente = new UtenteDTO();
        utenteStudente.setNome("Mario");
        utenteStudente.setCognome("Rossi");
        utenteStudente.setEmail("mario.rossi@email.com");
        utenteStudente.setPassword("hashed123");
        utenteStudente.setDataNascita(LocalDate.of(2000, 1, 1).toString());
        utenteStudente.setVia("Via Roma");
        utenteStudente.setCivico("1");
        utenteStudente.setCittà("Napoli");
        utenteStudente.setCAP("80100");
        utenteStudente.setTipo(TipoUtente.STUDENTE);
        
        // Setup utente genitore
        utenteGenitore = new UtenteDTO();
        utenteGenitore.setNome("Paolo");
        utenteGenitore.setCognome("Bianchi");
        utenteGenitore.setEmail("paolo.bianchi@email.com");
        utenteGenitore.setPassword("hashed456");
        utenteGenitore.setDataNascita(LocalDate.of(1980, 1, 1).toString());
        utenteGenitore.setVia("Via Milano");
        utenteGenitore.setCivico("2");
        utenteGenitore.setCittà("Roma");
        utenteGenitore.setCAP("00100");
        utenteGenitore.setTipo(TipoUtente.GENITORE);
        utenteGenitore.setNomeFiglio("Luca");
        utenteGenitore.setCognomeFiglio("Bianchi");
        utenteGenitore.setDataNascitaFiglio(LocalDate.of(2015, 1, 1).toString());
    }
    
    
    
    // ============== TEST doSave() ==============
    
    @Test
    void WB_TC_07_01DoSave_Studente_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("INSERT INTO Utente (nome, cognome, email, password, dataNascita, via, civico, citta, cap, tipoUtente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
            eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(123);
        
        // Act
        boolean result = dao.doSave(utenteStudente);
        
        // Assert
        assertTrue(result);
        assertEquals(123, utenteStudente.getUID());
        
        // Verify parametri STUDENTE
        verify(mockPreparedStatement).setString(1, "Mario");
        verify(mockPreparedStatement).setString(2, "Rossi");
        verify(mockPreparedStatement).setString(3, "mario.rossi@email.com");
        verify(mockPreparedStatement).setString(4, "hashed123");
        verify(mockPreparedStatement).setString(5, "2000-01-01");
        verify(mockPreparedStatement).setString(6, "Via Roma");
        verify(mockPreparedStatement).setString(7, "1");
        verify(mockPreparedStatement).setString(8, "Napoli");
        verify(mockPreparedStatement).setString(9, "80100");
        verify(mockPreparedStatement).setString(10, "STUDENTE");
        
        // Verify che NON siano stati settati parametri per GENITORE
        verify(mockPreparedStatement, never()).setString(eq(11), anyString());
        verify(mockPreparedStatement, never()).setString(eq(12), anyString());
        verify(mockPreparedStatement, never()).setString(eq(13), anyString());
    }
    
    @Test
    void WB_TC_07_02DoSave_Genitore_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("INSERT INTO Utente (nome, cognome, email, password, dataNascita, via, civico, citta, cap, tipoUtente, nomeFiglio, cognomeFiglio, dataNascitaFiglio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
            eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(mockPreparedStatementGenitore);
        
        when(mockPreparedStatementGenitore.executeUpdate()).thenReturn(1);
        when(mockPreparedStatementGenitore.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(456);
        
        // Act
        boolean result = dao.doSave(utenteGenitore);
        
        // Assert
        assertTrue(result);
        assertEquals(456, utenteGenitore.getUID());
        
        // Verify parametri GENITORE
        verify(mockPreparedStatementGenitore).setString(1, "Paolo");
        verify(mockPreparedStatementGenitore).setString(2, "Bianchi");
        verify(mockPreparedStatementGenitore).setString(3, "paolo.bianchi@email.com");
        verify(mockPreparedStatementGenitore).setString(4, "hashed456");
        verify(mockPreparedStatementGenitore).setString(5, "1980-01-01");
        verify(mockPreparedStatementGenitore).setString(6, "Via Milano");
        verify(mockPreparedStatementGenitore).setString(7, "2");
        verify(mockPreparedStatementGenitore).setString(8, "Roma");
        verify(mockPreparedStatementGenitore).setString(9, "00100");
        verify(mockPreparedStatementGenitore).setString(10, "GENITORE");
        verify(mockPreparedStatementGenitore).setString(11, "Luca");
        verify(mockPreparedStatementGenitore).setString(12, "Bianchi");
        verify(mockPreparedStatementGenitore).setString(13, "2015-01-01");
    }
    
    @Test
    void WB_TC_07_03DoSave_Tutor_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("INSERT INTO Utente (nome, cognome, email, password, dataNascita, via, civico, citta, cap, tipoUtente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"),
            eq(Statement.RETURN_GENERATED_KEYS)
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(123);
        
        // Act
        utenteStudente.setTipo(TipoUtente.TUTOR);
        boolean result = dao.doSave(utenteStudente);
        
        // Assert
        assertTrue(result);
        assertEquals(123, utenteStudente.getUID());
        
        // Verify parametri STUDENTE
        verify(mockPreparedStatement).setString(1, "Mario");
        verify(mockPreparedStatement).setString(2, "Rossi");
        verify(mockPreparedStatement).setString(3, "mario.rossi@email.com");
        verify(mockPreparedStatement).setString(4, "hashed123");
        verify(mockPreparedStatement).setString(5, "2000-01-01");
        verify(mockPreparedStatement).setString(6, "Via Roma");
        verify(mockPreparedStatement).setString(7, "1");
        verify(mockPreparedStatement).setString(8, "Napoli");
        verify(mockPreparedStatement).setString(9, "80100");
        verify(mockPreparedStatement).setString(10, "TUTOR");
        
        // Verify che NON siano stati settati parametri per GENITORE
        verify(mockPreparedStatement, never()).setString(eq(11), anyString());
        verify(mockPreparedStatement, never()).setString(eq(12), anyString());
        verify(mockPreparedStatement, never()).setString(eq(13), anyString());
    }
    
    @Test
    void WB_TC_07_04DoSave_NessunaRigaInserita() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.doSave(utenteStudente);
        
        // Assert
        assertFalse(result);
        verify(mockPreparedStatement, never()).getGeneratedKeys();
    }
    
    @Test
    void WB_TC_07_05DoSave_SQLException() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString(), anyInt()))
            .thenThrow(new SQLException("Errore database"));
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            dao.doSave(utenteStudente);
        });
    }
    
    // ============== TEST doRetrieveByEmail() ==============
    
    @Test
    void WB_TC_08_01DoRetrieveByEmail_Trovato() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("SELECT * FROM Utente WHERE email = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        
        // Mock ResultSet per STUDENTE
        mockResultSetForUtente(mockResultSet, TipoUtente.STUDENTE);
        
        // Act
        UtenteDTO result = dao.doRetrieveByEmail("test@email.com");
        
        // Assert
        assertNotNull(result);
        assertEquals("Mario", result.getNome());
        assertEquals("Rossi", result.getCognome());
        assertEquals(TipoUtente.STUDENTE, result.getTipo());
        
        verify(mockPreparedStatement).setString(1, "test@email.com");
    }
    
    @Test
    void WB_TC_08_02DoRetrieveByEmail_NonTrovato() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        UtenteDTO result = dao.doRetrieveByEmail("inesistente@email.com");
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void WB_TC_08_03DoRetrieveByEmail_ConErrore() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException("Connection failed"));
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            dao.doRetrieveByEmail("test@email.com");
        });
    }
    
    // ============== TEST doRetrieveAll() ==============
    
    @Test
    void WB_TC_10_01DoRetrieveAll_ConDati() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("SELECT * FROM Utente")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Simula 2 righe nel resultset
        when(mockResultSet.next()).thenReturn(true, true, false);
        
        // Mock per due utenti diversi
        when(mockResultSet.getString("tipoUtente"))
            .thenReturn("STUDENTE", "TUTOR");
        when(mockResultSet.getString("nome"))
            .thenReturn("Mario", "Luigi");
        
        // Act
        List<UtenteDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertEquals(2, result.size());
        verify(mockPreparedStatement, times(1)).executeQuery();
    }
    
    @Test
    void WB_TC_10_02DoRetrieveAll_Vuoto() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        List<UtenteDTO> result = dao.doRetrieveAll();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void WB_TC_10_03DoRetrieveAll_ConErrore() throws SQLException {
    	// Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException("Connection failed"));
        
        // Act & Assert
        assertThrows(SQLException.class, () -> {
            dao.doRetrieveAll();
        });
    }
    
    // ============== TEST doRetrieveById() ==============
    
    @Test
    void WB_TC_09_01DoRetrieveById_Trovato() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("SELECT * FROM Utente WHERE idUtente = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        mockResultSetForUtente(mockResultSet, TipoUtente.TUTOR);
        
        // Act
        UtenteDTO result = dao.doRetrieveById(123);
        
        // Assert
        assertNotNull(result);
        verify(mockPreparedStatement).setInt(1, 123);
    }
    
    @Test
    void WB_TC_09_02DoRetrieveById_NonTrovato() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);
        
        // Act
        UtenteDTO result = dao.doRetrieveById(999);
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void WB_TC_09_03DoRetrieveById_ConErrore() throws SQLException {
    	// Arrange
    	when(mockConnection.prepareStatement(anyString()))
    	.thenThrow(new SQLException("Connection failed"));

    	// Act & Assert
    	assertThrows(SQLException.class, () -> {
    		dao.doRetrieveById(999);
    	});
    }
    
    // ============== TEST doUpdate() ==============
    
    @Test
    void WB_TC_11_01DoUpdate_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("UPDATE Utente SET nome = ?, cognome = ?, email = ?, password = ?, dataNascita = ?, via = ?, civico = ?, citta = ?, cap = ?, tipoUtente = ? WHERE idUtente = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        utenteStudente.setUID(123);
        
        // Act
        boolean result = dao.doUpdate(utenteStudente);
        
        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setInt(11, 123);
    }
    
    @Test
    void WB_TC_11_02DoUpdate_NessunaRigaModificata() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        utenteStudente.setUID(999); // ID non esistente
        
        // Act
        boolean result = dao.doUpdate(utenteStudente);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void WB_TC_11_03DoUpdate_ConErrore() throws SQLException {
    	// Arrange
    	when(mockConnection.prepareStatement(anyString()))
    	.thenThrow(new SQLException("Connection failed"));

    	// Act & Assert
    	assertThrows(SQLException.class, () -> {
    		dao.doUpdate(utenteStudente);
    	});
    }
    
    // ============== TEST doDelete() ==============
    
    @Test
    void WB_TC_12_01DoDelete_Successo() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(
            eq("DELETE FROM Utente WHERE idUtente = ?")
        )).thenReturn(mockPreparedStatement);
        
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = dao.doDelete(123);
        
        // Assert
        assertTrue(result);
        verify(mockPreparedStatement).setInt(1, 123);
    }
    
    @Test
    void WB_TC_12_02DoDelete_NonEsistente() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
            .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);
        
        // Act
        boolean result = dao.doDelete(999);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void WB_TC_12_03DoDelete_ConErrore() throws SQLException {
    	// Arrange
    	when(mockConnection.prepareStatement(anyString()))
    	.thenThrow(new SQLException("Connection failed"));

    	// Act & Assert
    	assertThrows(SQLException.class, () -> {
    		dao.doDelete(utenteStudente.getUID());
    	});
    }
    
    // ============== TEST mapResultSetToUtente() (private method) ==============
    
    @Test
    void WB_TC_13_01MapResultSetToUtente_TuttiTipi() throws SQLException {
    
        // Questo è un test che usa reflection per testare il metodo privato
        try {
            // Setup per STUDENTE
            when(mockResultSet.getString("tipoUtente")).thenReturn("STUDENTE");
            when(mockResultSet.getInt("idUtente")).thenReturn(1);
            when(mockResultSet.getString("nome")).thenReturn("Test");
            
            // Usa reflection per chiamare il metodo privato
            java.lang.reflect.Method method = GestioneUtenzaDAO.class
                .getDeclaredMethod("mapResultSetToUtente", ResultSet.class);
            method.setAccessible(true);
            
            UtenteDTO result = (UtenteDTO) method.invoke(dao, mockResultSet);
            
            assertNotNull(result);
            assertEquals(TipoUtente.STUDENTE, result.getTipo());
            
        } catch (Exception e) {
            fail("Errore reflection: " + e.getMessage());
        }
    }
    
    @Test
    void WB_TC_13_02MapResultSetToUtente_TipoNonRiconosciuto() throws SQLException {
        // Test per tipo utente non valido
        when(mockResultSet.getString("tipoUtente")).thenReturn("TIPO_SCONOSCIUTO");
        when(mockResultSet.getInt("idUtente")).thenReturn(1);
        
        // Dovrebbe gestire il caso default o lanciare eccezione?
        // Dipende dalla tua implementazione
    }
    
    // ============== METODI DI SUPPORTO ==============
    
    private void mockResultSetForUtente(ResultSet rs, TipoUtente tipo) throws SQLException {
        when(rs.getInt("idUtente")).thenReturn(1);
        when(rs.getString("nome")).thenReturn("Mario");
        when(rs.getString("cognome")).thenReturn("Rossi");
        when(rs.getString("email")).thenReturn("test@email.com");
        when(rs.getString("password")).thenReturn("hashed");
        when(rs.getString("dataNascita")).thenReturn("2000-01-01");
        when(rs.getString("citta")).thenReturn("Napoli");
        when(rs.getString("cap")).thenReturn("80100");
        when(rs.getString("via")).thenReturn("Via Roma");
        when(rs.getString("civico")).thenReturn("1");
        
        switch(tipo) {
            case STUDENTE:
                when(rs.getString("tipoUtente")).thenReturn("STUDENTE");
                break;
            case TUTOR:
                when(rs.getString("tipoUtente")).thenReturn("TUTOR");
                break;
            case GENITORE:
                when(rs.getString("tipoUtente")).thenReturn("GENITORE");
                break;
            case AMMINISTRATORE_UTENTI:
                when(rs.getString("tipoUtente")).thenReturn("AMMINISTRATORE_UTENTI");
                break;
        }
    }
}