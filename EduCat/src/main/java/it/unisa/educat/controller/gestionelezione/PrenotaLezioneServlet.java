package it.unisa.educat.controller.gestionelezione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;
import it.unisa.educat.model.LezioneDTO.StatoLezione;

/**
 * Servlet implementation class PrenotaLezione
 */
@WebServlet("/prenota-lezione")
public class PrenotaLezioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private GestioneLezioneDAO lezioneDAO;
    
    @Override
    public void init() throws ServletException {
        lezioneDAO = new GestioneLezioneDAO();
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                URLEncoder.encode("Sessione scaduta", "UTF-8"));
            return;
        }
        
        UtenteDTO studente = (UtenteDTO) session.getAttribute("utente");
        if (studente == null) {
            response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                URLEncoder.encode("Accesso richiesto", "UTF-8"));
            return;
        }
        
        try {
            String idLezioneStr = request.getParameter("idLezione");
            if (idLezioneStr == null || idLezioneStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath()+"/cerca-lezione?error=" + 
                    URLEncoder.encode("ID lezione non specificato", "UTF-8"));
                return;
            }
            
            int idLezione = Integer.parseInt(idLezioneStr);
            
            if (!"STUDENTE".equals(studente.getTipo().toString())) {
                response.sendRedirect(request.getContextPath()+"/cerca-lezione?error=" + 
                    URLEncoder.encode("Solo gli studenti possono prenotare lezioni", "UTF-8"));
                return;
            }
            
            LezioneDTO lezione = lezioneDAO.getLezioneById(idLezione);
            
            if (lezione == null) {
                response.sendRedirect(request.getContextPath()+"/cerca-lezione?error=" + 
                    URLEncoder.encode("Lezione non trovata", "UTF-8"));
                return;
            }
            
            // Validazioni
            
            String errorePagamento = validaDatiPagamento(request);
            if (errorePagamento != null) {
                response.sendRedirect(request.getContextPath()+"/info-lezione?idLezione=" + 
                    idLezione + "&error=" + URLEncoder.encode(errorePagamento, "UTF-8"));
                return;
            }
            
            String erroreAnticipo = validaAnticipoLezione(lezione);
            if (erroreAnticipo != null) {
                response.sendRedirect(request.getContextPath()+"/info-lezione?idLezione=" + 
                    idLezione + "&error=" + URLEncoder.encode(erroreAnticipo, "UTF-8"));
                return;
            }
            
            if(lezioneDAO.hasStudentePrenotazioneInFasciaOraria(studente.getUID(), 
               lezione.getDataInizio(), lezione.getDataFine())) {
                response.sendRedirect(request.getContextPath()+"/info-lezione?idLezione=" + 
                    idLezione + "&error=" + URLEncoder.encode(
                    "Hai già prenotato una lezione in questa fascia oraria", "UTF-8"));
                return;
            }
            
            if (lezione.getStato() != StatoLezione.PIANIFICATA) {
                response.sendRedirect(request.getContextPath()+"/info-lezione?idLezione=" + 
                    idLezione + "&error=" + URLEncoder.encode(
                    "Questa lezione non è più disponibile", "UTF-8"));
                return;
            }
            
            if (lezione.getDataInizio().isBefore(LocalDateTime.now())) {
                response.sendRedirect(request.getContextPath()+"/info-lezione?idLezione=" + 
                    idLezione + "&error=" + URLEncoder.encode(
                    "Impossibile prenotare una lezione già passata", "UTF-8"));
                return;
            }
            
            PrenotazioneDTO prenotazione = new PrenotazioneDTO();
            prenotazione.setStudente(studente);
            prenotazione.setLezione(lezione);
            prenotazione.setDataPrenotazione(LocalDate.now());
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
            prenotazione.setImportoPagato(lezione.getPrezzo() * lezione.getDurata());
            prenotazione.setIndirizzoFatturazione(request.getParameter("indirizzo"));
            prenotazione.setNumeroCarta(request.getParameter("numeroCarta"));
            prenotazione.setDataScadenza(request.getParameter("scadenza"));
            
            try {
                if (request.getParameter("cvv") != null) {
                    prenotazione.setCvv(Integer.parseInt(request.getParameter("cvv")));
                }
            } catch (NumberFormatException e) {
            }
            
            prenotazione.setIntestatario(request.getParameter("intestatario"));
            prenotazione.setIdTutor(lezione.getTutor().getUID());
            
            boolean success = lezioneDAO.prenotaLezione(prenotazione);
            
            if (success) {
                response.sendRedirect(request.getContextPath()+"/storico-lezioni?success=" + 
                    URLEncoder.encode("Lezione prenotata con successo!", "UTF-8"));
            } else {
                response.sendRedirect(request.getContextPath()+"/info-lezione?idLezione=" + 
                    idLezione + "&error=" + URLEncoder.encode(
                    "Impossibile prenotare la lezione", "UTF-8"));
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()+"/cerca-lezione?error=" + 
                URLEncoder.encode("ID lezione non valido", "UTF-8"));
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/cerca-lezione?error=" + 
                URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()+"/cerca-lezione?error=" + 
                URLEncoder.encode("Errore di database durante la prenotazione", "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()+"/cerca-lezione?error=" + 
                URLEncoder.encode("Errore durante la prenotazione: " + e.getMessage(), "UTF-8"));
        }    
    }


    /**
     * Valida i dati della carta di credito
     */
    private String validaDatiPagamento(HttpServletRequest request) {
        String numeroCarta = request.getParameter("numeroCarta");
        String scadenza = request.getParameter("scadenza");
        String cvv = request.getParameter("cvv");
        String intestatario = request.getParameter("intestatario");
        
        if (numeroCarta == null || numeroCarta.trim().isEmpty()) {
            return "Il numero di carta è obbligatorio";
        }
        
        String numeroCartaPulito = numeroCarta.replaceAll("\\s+", "");
        if (!numeroCartaPulito.matches("\\d{16}")) {
            return "Il numero di carta deve contenere 16 cifre";
        }
        
        if (cvv == null || cvv.trim().isEmpty()) {
            return "Il codice CVV è obbligatorio";
        }
        
        if (!cvv.matches("\\d{3}")) {
            return "Il CVV deve contenere 3 cifre";
        }
        
        if (scadenza == null || scadenza.trim().isEmpty()) {
            return "La data di scadenza è obbligatoria";
        }
        
        if (!scadenza.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return "Formato scadenza non valido (usa MM/YY)";
        }
        
        try {
            String[] parti = scadenza.split("/");
            int mese = Integer.parseInt(parti[0]);
            int anno = Integer.parseInt(parti[1]) + 2000; // Converti YY in YYYY
            
            LocalDate oggi = LocalDate.now();
            LocalDate scadenzaCarta = LocalDate.of(anno, mese, 1).withDayOfMonth(
                LocalDate.of(anno, mese, 1).lengthOfMonth()
            );
            
            if (scadenzaCarta.isBefore(oggi)) {
                return "La carta di credito è scaduta";
            }
        } catch (Exception e) {
            return "Data di scadenza non valida";
        }
        
        if (intestatario == null || intestatario.trim().isEmpty()) {
            return "L'intestatario della carta è obbligatorio";
        }
        
        if (!intestatario.matches("^[A-Za-zÀ-ÿ\\s']{3,}(\\s+[A-Za-zÀ-ÿ\\s']{3,})+$")) {
            return "Inserisci nome e cognome dell'intestatario";
        }
        
        String indirizzo = request.getParameter("indirizzo");
        if (indirizzo == null || indirizzo.trim().isEmpty()) {
            return "L'indirizzo di fatturazione è obbligatorio";
        }
        
        if (indirizzo.trim().length() < 5) {
            return "Indirizzo di fatturazione troppo breve";
        }
        
        return null; // Tutto valido
    }

    /**
     * Valida che la prenotazione sia fatta con almeno 24 ore di anticipo
     */
    private String validaAnticipoLezione(LezioneDTO lezione) {
        if (lezione.getDataInizio() == null) {
            return "Data della lezione non specificata";
        }
        
        LocalDateTime oraPrenotazione = LocalDateTime.now();
        LocalDateTime inizioLezione = lezione.getDataInizio();
        
        if (inizioLezione.minusHours(24).isBefore(oraPrenotazione)) {
            return "Impossibile prenotare la lezione: mancano meno di 24 ore all'inizio";
        }
        
        return null; // Valido
    }
}