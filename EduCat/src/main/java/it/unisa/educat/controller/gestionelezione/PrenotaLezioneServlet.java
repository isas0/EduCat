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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
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
            // Ottieni parametri dalla richiesta
            String idLezioneStr = request.getParameter("idLezione");
            if (idLezioneStr == null || idLezioneStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath()+"/cerca-lezione?error=" + 
                    URLEncoder.encode("ID lezione non specificato", "UTF-8"));
                return;
            }
            
            int idLezione = Integer.parseInt(idLezioneStr);
            
            // Verifica che l'utente sia effettivamente uno studente
            if (!"STUDENTE".equals(studente.getTipo().toString())) {
                response.sendRedirect("/cerca-lezione?error=" + 
                    URLEncoder.encode("Solo gli studenti possono prenotare lezioni", "UTF-8"));
                return;
            }
            
            // Recupera la lezione dal database
            LezioneDTO lezione = lezioneDAO.getLezioneById(idLezione);
            
            if (lezione == null) {
                response.sendRedirect("/cerca-lezione?error=" + 
                    URLEncoder.encode("Lezione non trovata", "UTF-8"));
                return;
            }
            
            // Verifica che la lezione sia disponibile
            if (lezione.getStato() != StatoLezione.PIANIFICATA) {
                response.sendRedirect("/info-lezione?idLezione=" + idLezione + "&error=" + 
                    URLEncoder.encode("Questa lezione non è più disponibile", "UTF-8"));
                return;
            }
            
            // Verifica che la lezione non sia già passata
            if (lezione.getDataInizio().isBefore(LocalDateTime.now())) {
                response.sendRedirect("/info-lezione?idLezione=" + idLezione + "&error=" + 
                    URLEncoder.encode("Impossibile prenotare una lezione già passata", "UTF-8"));
                return;
            }
            
            // Crea l'oggetto PrenotazioneDTO
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
                // Gestione errore CVV
            }
            
            prenotazione.setIntestatario(request.getParameter("intestatario"));
            prenotazione.setIdTutor(lezione.getTutor().getUID());
            
            // Effettua la prenotazione
            boolean success = lezioneDAO.prenotaLezione(prenotazione);
            
            if (success) {
                // Successo
                response.sendRedirect(request.getContextPath()+"/storico-lezioni?success=" + 
                    URLEncoder.encode("Lezione prenotata con successo!", "UTF-8"));
            } else {
                // Fallimento
                response.sendRedirect(request.getContextPath()+"/info-lezione?idLezione=" + idLezione + "&error=" + 
                    URLEncoder.encode("Impossibile prenotare la lezione", "UTF-8"));
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
}