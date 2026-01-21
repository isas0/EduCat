package test.integration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import test.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.unisa.educat.controller.gestionelezione.AnnullaPrenotazioneServlet;
import it.unisa.educat.controller.gestionelezione.CercaLezioneServlet;
import it.unisa.educat.controller.gestionelezione.PrenotaLezioneServlet;
import it.unisa.educat.controller.gestionelezione.PubblicaAnnuncioServlet;
import it.unisa.educat.controller.gestionelezione.StoricoLezioniServlet;
import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;
import it.unisa.educat.model.LezioneDTO.StatoLezione;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

class LezioneServletIntegrationTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock HttpSession session;
    @Mock RequestDispatcher dispatcher;
    @Mock GestioneLezioneDAO lezioneDAO;

    UtenteDTO tutor;
    UtenteDTO studente;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(request.getSession()).thenReturn(session);
        lenient().when(request.getSession(anyBoolean())).thenReturn(session);
        lenient().when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);

        tutor = new UtenteDTO();
        tutor.setUID(1);
        tutor.setTipo(TipoUtente.TUTOR);
        tutor.setCittà("Roma");

        studente = new UtenteDTO();
        studente.setUID(2);
        studente.setTipo(TipoUtente.STUDENTE);
    }

    @Test
    @DisplayName("TC_LEZ_01_01: Tutto corretto")
    void TC_LEZ_01_01_PrenotazioneCompletata() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        // Mock lezione valida
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5)); // Futura
        lezione.setDataFine(LocalDateTime.now().plusDays(5).plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        when(lezioneDAO.prenotaLezione(any(PrenotazioneDTO.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(lezioneDAO).prenotaLezione(any(PrenotazioneDTO.class));
        verify(response).sendRedirect(contains("success="));
    }
    
    @Test
    @DisplayName("TC_LEZ_01_02: ID lezione vuoto")
    void TC_LEZ_01_02_PrenotazioneIDEmpty() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn(""); // EMPTY!
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("ID+lezione+non+specificato"));
    }

    @Test
    @DisplayName("TC_LEZ_01_03: ID lezione non numerico")  
    void TC_LEZ_01_03_PrenotazioneIDInvalid() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("abc"); // NON NUMERICO
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("ID+lezione+non+valido"));
    }

    @Test
    @DisplayName("TC_LEZ_01_04: Meno di 24h alla lezione")
    void TC_LEZ_01_04_PrenotazioneLessThan24h() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusHours(23)); // SOLO 23h!
        lezione.setDataFine(LocalDateTime.now().plusHours(24));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("Impossibile+prenotare+la+lezione"));
    }

    @Test
    @DisplayName("TC_LEZ_01_05: Data lezione già passata")
    void TC_LEZ_01_05_PrenotazioneLezionePassata() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        // LEZIONE NEL PASSATO
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().minusDays(1)); // IERI!
        lezione.setDataFine(LocalDateTime.now().minusDays(1).plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        
        // Dati pagamento validi (ma non dovrebbero essere usati)
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn("12/25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        // ✅ Questo DOVREBBE fallire (lezione passata)
        // Ma se il servlet non controlla, il test fallirà
        verify(lezioneDAO, never()).prenotaLezione(any());
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("Impossibile+prenotare+una+lezione+gi%C3%A0+passata"));
    }
    
    @Test
    @DisplayName("TC_LEZ_01_06: Slot temporale occupato")
    void TC_LEZ_01_06_PrenotazioneTimeConflict() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(LocalDateTime.now().plusDays(5).plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(true); // CONFLITTO!
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("prenotato+una+lezione+in+questa+fascia+oraria"));
    }
    
    
 // ============== TEST PAGAMENTO 07-16 ==============

    @Test
    @DisplayName("TC_LEZ_01_07: Numero carta vuoto")
    void TC_LEZ_01_07_PrenotazioneCartaVuota() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        
        //CARTA VUOTA - JavaScript bloccherebbe, servlet DOVREBBE bloccare
        when(request.getParameter("numeroCarta")).thenReturn("");
        when(request.getParameter("scadenza")).thenReturn("12/25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).prenotaLezione(any());
        verify(response).sendRedirect(contains("info-lezione"));
        verify(response).sendRedirect(contains("error="));
    }

    @Test
    @DisplayName("TC_LEZ_01_08: Numero carta non 16 cifre")
    void TC_LEZ_01_08_PrenotazioneCarta15Cifre() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        when(lezioneDAO.prenotaLezione(any(PrenotazioneDTO.class))).thenReturn(true);
        
        //CARTA 15 CIFRE - Dovrebbero essere 16
        when(request.getParameter("numeroCarta")).thenReturn("123456781234567"); // 15 cifre!
        when(request.getParameter("scadenza")).thenReturn("12/25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).prenotaLezione(any()); // ❌ Fallirà perché il DAO sarà chiamato
        verify(response).sendRedirect(contains("error=")); // ❌ Fallirà perché sarà success=
    }

    @Test
    @DisplayName("TC_LEZ_01_09: CVV vuoto")
    void TC_LEZ_01_09_PrenotazioneCVVVuoto() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        
        //CVV VUOTO
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn("12/25");
        when(request.getParameter("cvv")).thenReturn(""); // VUOTO!
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).prenotaLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_01_10: CVV non 3 cifre")
    void TC_LEZ_01_10_PrenotazioneCVV4Cifre() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        when(lezioneDAO.prenotaLezione(any(PrenotazioneDTO.class))).thenReturn(true);
        
        //CVV 4 CIFRE - Dovrebbero essere 3
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn("12/25");
        when(request.getParameter("cvv")).thenReturn("1234"); // 4 cifre!
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO).prenotaLezione(argThat(p -> {
            System.out.println("BUG: CVV accettato = " + p.getCvv());
            return p.getCvv() == 1234;
        }));
    }

    @Test
    @DisplayName("TC_LEZ_01_11: Scadenza carta vuota")
    void TC_LEZ_01_11_PrenotazioneScadenzaVuota() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        
        //SCADENZA VUOTA
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn(""); // VUOTO!
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).prenotaLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_01_12: Carta scaduta")
    void TC_LEZ_01_12_PrenotazioneCartaScaduta() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        when(lezioneDAO.prenotaLezione(any(PrenotazioneDTO.class))).thenReturn(true);
        
        //CARTA SCADUTA - Gennaio 2020
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn("01/20"); // SCADUTA!
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO).prenotaLezione(argThat(p -> 
            p.getDataScadenza().equals("01/20")));
    }

    @Test
    @DisplayName("TC_LEZ_01_13: Intestatario vuoto")
    void TC_LEZ_01_13_PrenotazioneIntestatarioVuoto() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        
        //INTESTATARIO VUOTO
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn("12/25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn(""); // VUOTO!
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).prenotaLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_01_14: Intestatario con caratteri speciali")
    void TC_LEZ_01_14_PrenotazioneIntestatarioCaratteriSpeciali() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        when(lezioneDAO.prenotaLezione(any(PrenotazioneDTO.class))).thenReturn(true);
        
        //INTESTATARIO CON NUMERI E CARATTERI SPECIALI
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn("12/25");
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn("M4R10_R0SS1#@!"); // INVALIDO!
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO).prenotaLezione(argThat(p -> 
            p.getIntestatario().contains("#@!")));
    }

    @Test
    @DisplayName("TC_LEZ_01_15: Dati pagamento vuoti")
    void TC_LEZ_01_15_PrenotazioneDatiPagamentoVuoti() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        
        //TUTTI I DATI PAGAMENTO VUOTI
        when(request.getParameter("numeroCarta")).thenReturn("");
        when(request.getParameter("scadenza")).thenReturn("");
        when(request.getParameter("cvv")).thenReturn("");
        when(request.getParameter("intestatario")).thenReturn("");
        when(request.getParameter("indirizzo")).thenReturn("");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).prenotaLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_01_16: Formato scadenza errato")
    void TC_LEZ_01_16_PrenotazioneFormatoScadenzaErrato() throws Exception {
        PrenotaLezioneServlet servlet = new PrenotaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idLezione")).thenReturn("100");
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setIdLezione(100);
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setDataInizio(LocalDateTime.now().plusDays(5));
        lezione.setDataFine(lezione.getDataInizio().plusHours(1));
        lezione.setPrezzo(15);
        lezione.setDurata(1);
        lezione.setTutor(tutor);

        when(lezioneDAO.getLezioneById(100)).thenReturn(lezione);
        when(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        when(lezioneDAO.prenotaLezione(any(PrenotazioneDTO.class))).thenReturn(true);
        
        //FORMATO SCADENZA SBAGLIATO: MM-AA invece di MM/AA
        when(request.getParameter("numeroCarta")).thenReturn("1234567812345678");
        when(request.getParameter("scadenza")).thenReturn("12-25"); // FORMATO SBAGLIATO!
        when(request.getParameter("cvv")).thenReturn("123");
        when(request.getParameter("intestatario")).thenReturn("MARIO ROSSI");
        when(request.getParameter("indirizzo")).thenReturn("Via Roma 1");
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO).prenotaLezione(argThat(p -> 
            p.getDataScadenza().equals("12-25")));
    }
    
    
    
    // ============== TEST TC_LEZ_02 COMPLETI ==============
    
    @Test
    @DisplayName("TC_LEZ_02_01: Happy Path - Lezione pubblicata con successo")
    void TC_LEZ_02_01_PubblicaAnnuncioSuccess() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // Parametri validi
        when(request.getParameter("materia")).thenReturn("Fisica");
        // Data futura per evitare errori
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");

        // Mock DAO: nessuna sovrapposizione e salvataggio OK
        when(lezioneDAO.hasTutorLezioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        when(lezioneDAO.doSaveLezione(any(LezioneDTO.class))).thenReturn(true);

        servlet.doPost(request, response);

        verify(lezioneDAO).doSaveLezione(any(LezioneDTO.class));
        verify(response).sendRedirect(contains("success="));
    }



    @Test
    @DisplayName("TC_LEZ_02_02: Materia vuota")
    void TC_LEZ_02_02_PubblicaAnnuncioMateriaVuota() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ MATERIA VUOTA
        when(request.getParameter("materia")).thenReturn(""); // VUOTO!
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        // Verifica errore "La materia è obbligatoria"
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("La+materia+%C3%A8+obbligatoria"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_03: Data vuota")
    void TC_LEZ_02_03_PubblicaAnnuncioDataVuota() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ DATA VUOTA
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn(""); // VUOTO!
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("La+data+%C3%A8+obbligatoria"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_04: Data nel passato")
    void TC_LEZ_02_04_PubblicaAnnuncioDataPassata() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ DATA NEL PASSATO
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2020-01-01"); // PASSATO!
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("Non+puoi+creare+lezioni+nel+passato"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_05: Orario vuoto")
    void TC_LEZ_02_05_PubblicaAnnuncioOrarioVuoto() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ ORARIO VUOTO
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn(""); // VUOTO!
        when(request.getParameter("oraFine")).thenReturn(""); // VUOTO!
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("di+inizio+%C3%A8+obbligatoria"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_06: Solo ora inizio (manca ora fine)")
    void TC_LEZ_02_06_PubblicaAnnuncioSoloOraInizio() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ SOLO ORA INIZIO
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn(""); // MANCA ORA FINE!
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("ora+di+fine+%C3%A8+obbligatoria"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_07: Solo ora fine (manca ora inizio)")
    void TC_LEZ_02_07_PubblicaAnnuncioSoloOraFine() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ SOLO ORA FINE
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn(""); // MANCA ORA INIZIO!
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("ora+di+inizio+%C3%A8+obbligatoria"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_08: Ora fine < Ora inizio")
    void TC_LEZ_02_08_PubblicaAnnuncioOraFineMinoreInizio() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ ORA FINE PRIMA DI ORA INIZIO
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("12:00"); // 12:00
        when(request.getParameter("oraFine")).thenReturn("10:00");   // 10:00 (PRIMA!)
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("L%27ora+di+fine+deve+essere+successiva+all%27ora+di+inizio"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_09: Prezzo vuoto")
    void TC_LEZ_02_09_PubblicaAnnuncioPrezzoVuoto() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ PREZZO VUOTO
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn(""); // VUOTO!
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("Il+prezzo+%C3%A8+obbligatorio"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_10: Prezzo zero o negativo")
    void TC_LEZ_02_10_PubblicaAnnuncioPrezzoZero() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ PREZZO ZERO
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("0"); // ZERO!
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("Il+prezzo+deve+essere+maggiore+di+0"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_11: Modalità non selezionata")
    void TC_LEZ_02_11_PubblicaAnnuncioModalitaVuota() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ MODALITÀ VUOTA
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn(""); // VUOTO!
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("non+valida"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_12: Conflitto fascia oraria")
    void TC_LEZ_02_12_PubblicaAnnuncioConflittoFascia() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // Parametri validi
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        // ❌ CONFLITTO FASCIA ORARIA
        when(lezioneDAO.hasTutorLezioneInFasciaOraria(anyInt(), any(), any())).thenReturn(true);
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("una+lezione+attiva+nella+stessa+fascia+oraria"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_13: Durata minima 30 minuti")
    void TC_LEZ_02_13_PubblicaAnnuncioDurataMinima() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ DURATA 29 MINUTI (meno di 30 min)
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("10:29"); // Solo 29 minuti!
        when(request.getParameter("prezzo")).thenReturn("25");
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        when(lezioneDAO.hasTutorLezioneInFasciaOraria(anyInt(), any(), any())).thenReturn(false);
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("La+lezione+deve+durare+almeno+30+minuti"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }

    @Test
    @DisplayName("TC_LEZ_02_14: Prezzo formato non valido")
    void TC_LEZ_02_14_PubblicaAnnuncioPrezzoNonNumerico() throws Exception {
        PubblicaAnnuncioServlet servlet = new PubblicaAnnuncioServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(session.getAttribute("utente")).thenReturn(tutor);
        
        // ❌ PREZZO NON NUMERICO
        when(request.getParameter("materia")).thenReturn("Fisica");
        when(request.getParameter("data")).thenReturn("2030-01-01");
        when(request.getParameter("oraInizio")).thenReturn("10:00");
        when(request.getParameter("oraFine")).thenReturn("12:00");
        when(request.getParameter("prezzo")).thenReturn("venticinque"); // NON NUMERICO!
        when(request.getParameter("modalita")).thenReturn("ONLINE");
        
        servlet.doPost(request, response);
        
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("Formato+prezzo+non+valido"));
        verify(lezioneDAO, never()).doSaveLezione(any());
    }
 // ============== TEST TC_LEZ_03: Annullamento Tutor ==============
    
    @Test
    @DisplayName("TC_LEZ_03_01: Tutor annulla prenotazione successo (>24h)")
    void TC_LEZ_03_01_TutorAnnullaPrenotazioneSuccess() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(tutor);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione valida (>24h rimanenti)
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusDays(2)); // +48h
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        when(lezioneDAO.annullaPrenotazione(100)).thenReturn(true);
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO).annullaPrenotazione(100);
        verify(response).sendRedirect(contains("success="));
        verify(response).sendRedirect(contains("Prenotazione+annullata+con+successo"));
    }

    @Test
    @DisplayName("TC_LEZ_03_02: ID prenotazione vuoto")
    void TC_LEZ_03_02_TutorAnnullaPrenotazioneIDVuoto() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(tutor);
        when(request.getParameter("idPrenotazione")).thenReturn(""); // VUOTO!
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).annullaPrenotazione(anyInt());
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("ID+prenotazione+non+specificato"));
    }
    
    @Test
    @DisplayName("TC_LEZ_03_03: Meno di 24h alla lezione")
    void TC_LEZ_03_03_TutorAnnullaPrenotazioneMeno24h() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(tutor);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione con <24h rimanenti
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusHours(23)); // +23h (<24h)
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).annullaPrenotazione(anyInt());
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("manca+meno+di+un+giorno+alla+lezione"));
    }
    
    @Test
    @DisplayName("TC_LEZ_03_04: Boundary test - 23h 59min")
    void TC_LEZ_03_04_TutorAnnullaPrenotazione23h59min() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(tutor);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione con 23h 59min rimanenti
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusHours(23).plusMinutes(59)); // 23h59min
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        
        servlet.doPost(request, response);
        
        // 23h59min è MENO di 24h, quindi deve fallire
        verify(lezioneDAO, never()).annullaPrenotazione(anyInt());
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("manca+meno+di+un+giorno"));
    }
    
    @Test
    @DisplayName("TC_LEZ_03_05: Boundary test - 24h 1min")
    void TC_LEZ_03_05_TutorAnnullaPrenotazione24h1min() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(tutor);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione con 24h 1min rimanenti
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusHours(24).plusMinutes(1)); // 24h1min
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        when(lezioneDAO.annullaPrenotazione(100)).thenReturn(true);
        
        servlet.doPost(request, response);
        
        // 24h1min è PIÙ di 24h, quindi deve avere successo
        verify(lezioneDAO).annullaPrenotazione(100);
        verify(response).sendRedirect(contains("success="));
    }
    
    
 // ============== TEST TC_LEZ_04: Annullamento Studente ==============
  
    @Test
    @DisplayName("TC_LEZ_04_01: Studente annulla prenotazione successo (>24h)")
    void TC_LEZ_04_01_StudenteAnnullaPrenotazioneSuccess() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione valida (>24h rimanenti)
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusDays(2)); // +48h
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        when(lezioneDAO.annullaPrenotazione(100)).thenReturn(true);
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO).annullaPrenotazione(100);
        verify(response).sendRedirect(contains("success="));
        verify(response).sendRedirect(contains("Prenotazione+annullata+con+successo"));
    }

    @Test
    @DisplayName("TC_LEZ_04_02: ID prenotazione vuoto")
    void TC_LEZ_04_02_StudenteAnnullaPrenotazioneIDVuoto() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idPrenotazione")).thenReturn(""); // VUOTO!
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).annullaPrenotazione(anyInt());
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("ID+prenotazione+non+specificato"));
    }
    
    @Test
    @DisplayName("TC_LEZ_04_03: Meno di 24h alla lezione")
    void TC_LEZ_04_03_StudenteAnnullaPrenotazioneMeno24h() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione con <24h rimanenti
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusHours(23)); // +23h (<24h)
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        
        servlet.doPost(request, response);
        
        verify(lezioneDAO, never()).annullaPrenotazione(anyInt());
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("manca+meno+di+un+giorno+alla+lezione"));
    }
    
    @Test
    @DisplayName("TC_LEZ_04_04: Boundary test - 23h 59min")
    void TC_LEZ_04_04_StudenteAnnullaPrenotazione23h59min() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione con 23h 59min rimanenti
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusHours(23).plusMinutes(59)); // 23h59min
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        
        servlet.doPost(request, response);
        
        // 23h59min è MENO di 24h, quindi deve fallire
        verify(lezioneDAO, never()).annullaPrenotazione(anyInt());
        verify(response).sendRedirect(contains("error="));
        verify(response).sendRedirect(contains("manca+meno+di+un+giorno"));
    }
    
    @Test
    @DisplayName("TC_LEZ_04_05: Boundary test - 24h 1min")
    void TC_LEZ_04_05_StudenteAnnullaPrenotazione24h1min() throws Exception {
        AnnullaPrenotazioneServlet servlet = new AnnullaPrenotazioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);
        
        when(session.getAttribute("utente")).thenReturn(studente);
        when(request.getParameter("idPrenotazione")).thenReturn("100");
        
        // Mock prenotazione con 24h 1min rimanenti
        PrenotazioneDTO prenotazione = new PrenotazioneDTO();
        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
        
        LezioneDTO lezione = new LezioneDTO();
        lezione.setDataInizio(LocalDateTime.now().plusHours(24).plusMinutes(1)); // 24h1min
        prenotazione.setLezione(lezione);
        
        when(lezioneDAO.getPrenotazioneById(100)).thenReturn(prenotazione);
        when(lezioneDAO.annullaPrenotazione(100)).thenReturn(true);
        
        servlet.doPost(request, response);
        
        // 24h1min è PIÙ di 24h, quindi deve avere successo
        verify(lezioneDAO).annullaPrenotazione(100);
        verify(response).sendRedirect(contains("success="));
    }
    
 // ============== TEST RICERCA LEZIONE (TC_LEZ_07) ==============

    @Test
    @DisplayName("TC_LEZ_07_01: Ricerca senza filtri (tutte le lezioni)")
    void TC_LEZ_07_01_RicercaSenzaFiltri() throws Exception {
        CercaLezioneServlet servlet = new CercaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        // Tutti i parametri vuoti
        when(request.getParameter("materia")).thenReturn("");
        when(request.getParameter("modalita")).thenReturn("");
        when(request.getParameter("citta")).thenReturn("");
        when(request.getParameter("prezzoMax")).thenReturn("");

        List<LezioneDTO> mockList = new ArrayList<>();
        // Aggiungi 2 lezioni
        LezioneDTO lezione1 = new LezioneDTO();
        lezione1.setStato(StatoLezione.PIANIFICATA);
        lezione1.setMateria("Matematica");
        mockList.add(lezione1);
        
        LezioneDTO lezione2 = new LezioneDTO();
        lezione2.setStato(StatoLezione.PIANIFICATA);
        lezione2.setMateria("Fisica");
        mockList.add(lezione2);

        when(lezioneDAO.doRetrieveByCriteria(any())).thenReturn(mockList);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("lezioni"), argThat(list -> 
            ((List<?>)list).size() == 2
        ));
    }

    @Test
    @DisplayName("TC_LEZ_07_02: Ricerca solo per materia")
    void TC_LEZ_07_02_RicercaSoloMateria() throws Exception {
        CercaLezioneServlet servlet = new CercaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(request.getParameter("materia")).thenReturn("Fisica");
        // Altri filtri vuoti
        when(request.getParameter("modalita")).thenReturn("");
        when(request.getParameter("citta")).thenReturn("");
        when(request.getParameter("prezzoMax")).thenReturn("");

        List<LezioneDTO> mockList = new ArrayList<>();
        LezioneDTO lezione = new LezioneDTO();
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setMateria("Fisica");
        mockList.add(lezione);

        when(lezioneDAO.doRetrieveByCriteria(any())).thenReturn(mockList);

        servlet.doGet(request, response);

        verify(request).setAttribute("materiaParam", "Fisica");
        verify(request).setAttribute(eq("lezioni"), anyList());
    }

    @Test
    @DisplayName("TC_LEZ_07_03: Ricerca solo per modalità")
    void TC_LEZ_07_03_RicercaSoloModalita() throws Exception {
        CercaLezioneServlet servlet = new CercaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(request.getParameter("modalita")).thenReturn("PRESENZA");
        when(request.getParameter("materia")).thenReturn("");
        when(request.getParameter("citta")).thenReturn("");

        List<LezioneDTO> mockList = new ArrayList<>();
        LezioneDTO lezione = new LezioneDTO();
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.PRESENZA);
        mockList.add(lezione);

        when(lezioneDAO.doRetrieveByCriteria(any())).thenReturn(mockList);

        servlet.doGet(request, response);

        verify(request).setAttribute("modalitaParam", "PRESENZA");
        verify(request).setAttribute(eq("lezioni"), anyList());
    }

    @Test
    @DisplayName("TC_LEZ_07_04: Ricerca solo per città")
    void TC_LEZ_07_04_RicercaSoloCitta() throws Exception {
        CercaLezioneServlet servlet = new CercaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(request.getParameter("citta")).thenReturn("Napoli");
        when(request.getParameter("materia")).thenReturn("");
        when(request.getParameter("modalita")).thenReturn("");

        List<LezioneDTO> mockList = new ArrayList<>();
        LezioneDTO lezione = new LezioneDTO();
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setCitta("Napoli");
        mockList.add(lezione);

        when(lezioneDAO.doRetrieveByCriteria(any())).thenReturn(mockList);

        servlet.doGet(request, response);

        verify(request).setAttribute("cittaParam", "Napoli");
        verify(request).setAttribute(eq("lezioni"), anyList());
    }

    @Test
    @DisplayName("TC_LEZ_07_05: Ricerca solo per prezzo massimo")
    void TC_LEZ_07_05_RicercaSoloPrezzoMax() throws Exception {
        CercaLezioneServlet servlet = new CercaLezioneServlet();
        TestUtils.injectPrivateField(servlet, "lezioneDAO", lezioneDAO);

        when(request.getParameter("prezzoMax")).thenReturn("30");
        when(request.getParameter("materia")).thenReturn("");
        when(request.getParameter("citta")).thenReturn("");
        when(request.getParameter("modalita")).thenReturn("");

        List<LezioneDTO> mockList = new ArrayList<>();
        LezioneDTO lezione = new LezioneDTO();
        lezione.setStato(StatoLezione.PIANIFICATA);
        lezione.setPrezzo(25.0f); // < 30
        mockList.add(lezione);

        when(lezioneDAO.doRetrieveByCriteria(any())).thenReturn(mockList);

        servlet.doGet(request, response);

        verify(request).setAttribute("prezzoMaxParam", "30");
        verify(request).setAttribute(eq("lezioni"), anyList());
    }

    
   
}
