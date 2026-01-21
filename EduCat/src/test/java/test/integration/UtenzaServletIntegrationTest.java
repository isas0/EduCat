package test.integration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import test.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import it.unisa.educat.controller.gestioneutenza.*;
import it.unisa.educat.dao.GestioneUtenzaDAO;
import it.unisa.educat.model.UtenteDTO;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unisa.educat.dao.GestioneUtenzaDAO;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

class UtenzaServletIntegrationTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock HttpSession session;
    @Mock RequestDispatcher dispatcher;
    @Mock GestioneUtenzaDAO utenzaDAO;

    // Hash SHA-512 della stringa "password"
    private final String HASH_PASSWORD = "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Configurazione base della sessione per evitare NullPointerException
        lenient().when(request.getSession()).thenReturn(session);
        lenient().when(request.getSession(anyBoolean())).thenReturn(session);
        lenient().when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    
 // ============== TEST LOGIN (TC_LEZ_05) ==============

    @Test
    @DisplayName("TC_LEZ_05_01: Login successo (Happy Path)")
    void TC_LEZ_05_01_LoginSuccesso() throws Exception {
        LoginServlet servlet = new LoginServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        UtenteDTO user = new UtenteDTO();
        user.setEmail("student@test.it");
        user.setPassword(HASH_PASSWORD);
        user.setTipo(TipoUtente.STUDENTE);
        user.setUID(1);

        when(request.getParameter("email")).thenReturn("student@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(utenzaDAO.doRetrieveByEmail("student@test.it")).thenReturn(user);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("utente"), eq(user));
        verify(response).sendRedirect(contains("homePageStudenteGenitore.jsp"));
    }

    @Test
    @DisplayName("TC_LEZ_05_02: Mail non presente nel DB")
    void TC_LEZ_05_02_LoginMailNonPresente() throws Exception {
        LoginServlet servlet = new LoginServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        when(request.getParameter("email")).thenReturn("inesistente@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(utenzaDAO.doRetrieveByEmail("inesistente@test.it")).thenReturn(null);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Email o password errati"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("TC_LEZ_05_03: Mail vuota")
    void TC_LEZ_05_03_LoginMailVuota() throws Exception {
        LoginServlet servlet = new LoginServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        when(request.getParameter("email")).thenReturn(""); // VUOTA
        when(request.getParameter("password")).thenReturn("password");

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("TC_LEZ_05_04: Password non associata a mail")
    void TC_LEZ_05_04_LoginPasswordSbagliata() throws Exception {
        LoginServlet servlet = new LoginServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        UtenteDTO user = new UtenteDTO();
        user.setEmail("student@test.it");
        user.setPassword(HASH_PASSWORD); // Password corretta hash
        user.setTipo(TipoUtente.STUDENTE);

        when(request.getParameter("email")).thenReturn("student@test.it");
        when(request.getParameter("password")).thenReturn("passwordSbagliata"); // Password errata
        when(utenzaDAO.doRetrieveByEmail("student@test.it")).thenReturn(user);

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Email o password errati"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("TC_LEZ_05_05: Password vuota")
    void TC_LEZ_05_05_LoginPasswordVuota() throws Exception {
        LoginServlet servlet = new LoginServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        when(request.getParameter("email")).thenReturn("student@test.it");
        when(request.getParameter("password")).thenReturn(""); // VUOTA

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), anyString());
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("TC_LEZ_05_06: Login Tutor successo")
    void TC_LEZ_05_06_LoginTutorSuccesso() throws Exception {
        LoginServlet servlet = new LoginServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        UtenteDTO tutor = new UtenteDTO();
        tutor.setEmail("tutor@test.it");
        tutor.setPassword(HASH_PASSWORD);
        tutor.setTipo(TipoUtente.TUTOR);
        tutor.setUID(2);

        when(request.getParameter("email")).thenReturn("tutor@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(utenzaDAO.doRetrieveByEmail("tutor@test.it")).thenReturn(tutor);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("utente"), eq(tutor));
        verify(response).sendRedirect(contains("storico-lezioni"));
    }

    @Test
    @DisplayName("TC_LEZ_05_07: Login Genitore successo")
    void TC_LEZ_05_07_LoginGenitoreSuccesso() throws Exception {
        LoginServlet servlet = new LoginServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        UtenteDTO genitore = new UtenteDTO();
        genitore.setEmail("genitore@test.it");
        genitore.setPassword(HASH_PASSWORD);
        genitore.setTipo(TipoUtente.GENITORE);
        genitore.setUID(3);

        when(request.getParameter("email")).thenReturn("genitore@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(utenzaDAO.doRetrieveByEmail("genitore@test.it")).thenReturn(genitore);

        servlet.doPost(request, response);

        verify(session).setAttribute(eq("utente"), eq(genitore));
        verify(response).sendRedirect(contains("homePageStudenteGenitore.jsp"));
    }
    
    
 // ============== TEST REGISTRAZIONE (TC_LEZ_06) ==============

    @Test
    @DisplayName("TC_LEZ_06_01: Registrazione Studente successo")
    void TC_LEZ_06_01_RegistrazioneStudenteSuccesso() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        // Mock parametri validi STUDENTE
        when(request.getParameter("tipoUtente")).thenReturn("STUDENTE");
        when(request.getParameter("email")).thenReturn("nuovo.studente@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("nome")).thenReturn("Mario");
        when(request.getParameter("cognome")).thenReturn("Rossi");
        when(request.getParameter("dataNascita")).thenReturn("2000-01-01");
        when(request.getParameter("via")).thenReturn("Via Roma");
        when(request.getParameter("civico")).thenReturn("1");
        when(request.getParameter("città")).thenReturn("Roma");
        when(request.getParameter("CAP")).thenReturn("00100");

        when(utenzaDAO.doRetrieveByEmail("nuovo.studente@test.it")).thenReturn(null);
        when(utenzaDAO.doSave(any(UtenteDTO.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(utenzaDAO).doSave(any(UtenteDTO.class));
        verify(dispatcher).forward(request, response);
        // Verifica redirect a homePageStudenteGenitore.jsp
    }

    @Test
    @DisplayName("TC_LEZ_06_02: Registrazione Genitore successo con figlio valido")
    void TC_LEZ_06_02_RegistrazioneGenitoreSuccesso() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        // Mock parametri validi GENITORE con figlio
        when(request.getParameter("tipoUtente")).thenReturn("GENITORE");
        when(request.getParameter("email")).thenReturn("genitore@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("nome")).thenReturn("Paolo");
        when(request.getParameter("cognome")).thenReturn("Bianchi");
        when(request.getParameter("dataNascita")).thenReturn("1980-01-01");
        when(request.getParameter("via")).thenReturn("Via Milano");
        when(request.getParameter("civico")).thenReturn("2");
        when(request.getParameter("città")).thenReturn("Milano");
        when(request.getParameter("CAP")).thenReturn("20100");
        when(request.getParameter("nomeFiglio")).thenReturn("Luca");
        when(request.getParameter("cognomeFiglio")).thenReturn("Bianchi");
        when(request.getParameter("dataNascitaFiglio")).thenReturn("2010-01-01");

        when(utenzaDAO.doRetrieveByEmail("genitore@test.it")).thenReturn(null);
        when(utenzaDAO.doSave(any(UtenteDTO.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(utenzaDAO).doSave(any(UtenteDTO.class));
        verify(dispatcher).forward(request, response);
        // Verifica redirect a homePageStudenteGenitore.jsp
    }

    @Test
    @DisplayName("TC_LEZ_06_03: Registrazione Tutor successo")
    void TC_LEZ_06_03_RegistrazioneTutorSuccesso() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        // Mock parametri validi TUTOR
        when(request.getParameter("tipoUtente")).thenReturn("TUTOR");
        when(request.getParameter("email")).thenReturn("nuovo.tutor@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("nome")).thenReturn("Anna");
        when(request.getParameter("cognome")).thenReturn("Verdi");
        when(request.getParameter("dataNascita")).thenReturn("1990-01-01");
        when(request.getParameter("via")).thenReturn("Via Napoli");
        when(request.getParameter("civico")).thenReturn("3");
        when(request.getParameter("città")).thenReturn("Napoli");
        when(request.getParameter("CAP")).thenReturn("80100");

        when(utenzaDAO.doRetrieveByEmail("nuovo.tutor@test.it")).thenReturn(null);
        when(utenzaDAO.doSave(any(UtenteDTO.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(utenzaDAO).doSave(any(UtenteDTO.class));
        verify(dispatcher).forward(request, response);
        // Verifica redirect a nuovaLezione.jsp
    }

    @Test
    @DisplayName("TC_LEZ_06_04: Email già registrata")
    void TC_LEZ_06_04_RegistrazioneEmailEsistente() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        when(request.getParameter("email")).thenReturn("esiste@test.it");
        when(request.getParameter("tipoUtente")).thenReturn("STUDENTE");
        when(utenzaDAO.doRetrieveByEmail("esiste@test.it")).thenReturn(new UtenteDTO()); // Email esiste

        servlet.doPost(request, response);

        verify(request).setAttribute(eq("errorMessage"), contains("Email già registrata"));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("TC_LEZ_06_05: Nome/Cognome formato non valido (contiene numeri)")
    void TC_LEZ_06_05_RegistrazioneNomeCognomeNonValido() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        // NOTA: La validazione del nome/cognome probabilmente è fatta lato client (JavaScript)
        // Questo test verifica che il servlet non faccia validazione server-side
        when(request.getParameter("tipoUtente")).thenReturn("STUDENTE");
        when(request.getParameter("email")).thenReturn("test@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("nome")).thenReturn("M4r10"); // Contiene numeri
        when(request.getParameter("cognome")).thenReturn("R0ss1"); // Contiene numeri
        when(request.getParameter("dataNascita")).thenReturn("2000-01-01");

        when(utenzaDAO.doRetrieveByEmail("test@test.it")).thenReturn(null);

        servlet.doPost(request, response);

        // Il servlet dovrebbe accettarlo (validazione lato client)
        verify(utenzaDAO).doSave(argThat(u -> 
            u.getNome().equals("M4r10") && u.getCognome().equals("R0ss1")
        ));
    }

    @Test
    @DisplayName("TC_LEZ_06_06: Email formato non valido")
    void TC_LEZ_06_06_RegistrazioneEmailNonValida() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        // Email senza @
        when(request.getParameter("tipoUtente")).thenReturn("STUDENTE");
        when(request.getParameter("email")).thenReturn("emailnonvalida");
        when(request.getParameter("password")).thenReturn("password");

        servlet.doPost(request, response);

        verify(utenzaDAO, never()).doSave(any()); // NON dovrebbe salvare
        verify(request).setAttribute("errorMessage", contains("Email"));
    }

    @Test
    @DisplayName("TC_LEZ_06_07: Studente minorenne (data nascita recente)")
    void TC_LEZ_06_07_RegistrazioneStudenteMinorenne() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        // Studente nato nel 2010 (minorenne nel 2024)
        when(request.getParameter("tipoUtente")).thenReturn("STUDENTE");
        when(request.getParameter("email")).thenReturn("minorenne@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("nome")).thenReturn("Luca");
        when(request.getParameter("cognome")).thenReturn("Rossi");
        when(request.getParameter("dataNascita")).thenReturn("2010-01-01"); // 14 anni

        when(utenzaDAO.doRetrieveByEmail("minorenne@test.it")).thenReturn(null);

        servlet.doPost(request, response);

        verify(utenzaDAO, never()).doSave(any()); //invece salva!
        verify(request).setAttribute("errorMessage", contains("maggiorenne"));    }

    @Test
    @DisplayName("TC_LEZ_06_08: Genitore con figlio più grande")
    void TC_LEZ_06_08_RegistrazioneGenitoreFiglioPiuGrande() throws Exception {
        RegistrazioneServlet servlet = new RegistrazioneServlet();
        TestUtils.injectPrivateField(servlet, "utenzaDAO", utenzaDAO);

        // Genitore nato nel 1990, figlio nato nel 1980 (IMPOSSIBILE!)
        when(request.getParameter("tipoUtente")).thenReturn("GENITORE");
        when(request.getParameter("email")).thenReturn("genitore@test.it");
        when(request.getParameter("password")).thenReturn("password");
        when(request.getParameter("nome")).thenReturn("Giovanni");
        when(request.getParameter("cognome")).thenReturn("Rossi");
        when(request.getParameter("dataNascita")).thenReturn("1990-01-01"); // Genitore 34 anni
        when(request.getParameter("nomeFiglio")).thenReturn("Marco");
        when(request.getParameter("cognomeFiglio")).thenReturn("Rossi");
        when(request.getParameter("dataNascitaFiglio")).thenReturn("1980-01-01"); // Figlio 44 anni!

        when(utenzaDAO.doRetrieveByEmail("genitore@test.it")).thenReturn(null);

        servlet.doPost(request, response);

        //(non c'è validazione server-side)
        verify(utenzaDAO, never()).doSave(any()); //invece salva!
        verify(request).setAttribute("errorMessage", contains("più grande"));
    }
    
}