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
import java.time.LocalDateTime;
import java.util.List;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;

@WebServlet("/annulla-prenotazione")
public class AnnullaPrenotazioneServlet extends HttpServlet {
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
            response.sendRedirect(request.getContextPath()+"/login.jsp");
            return;
        }
        
        UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
        if (utente == null) {
            response.sendRedirect(request.getContextPath()+"/login.jsp");
            return;
        }
        
        try {
            // Ottieni parametri dalla richiesta
            String idPrenotazioneStr = request.getParameter("idPrenotazione");
            if (idPrenotazioneStr == null || idPrenotazioneStr.trim().isEmpty()) {
                throw new IllegalArgumentException("ID prenotazione non specificato");
            }
            
            int idPrenotazione = Integer.parseInt(idPrenotazioneStr);
            
            // Verifica che l'utente sia uno studente o un tutor
            if (!"STUDENTE".equals(utente.getTipo().toString()) && !"TUTOR".equals(utente.getTipo().toString()) && !"GENITORE".equals(utente.getTipo().toString())) {
                response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                    URLEncoder.encode("Solo studenti o tutor possono annullare prenotazioni", "UTF-8"));
                return;
            }
            
            // Recupera la prenotazione con la lezione associata
            PrenotazioneDTO prenotazione = lezioneDAO.getPrenotazioneById(idPrenotazione);
            LezioneDTO lezione = prenotazione.getLezione();
            
            // Verifica che la prenotazione sia ancora attiva
            if (prenotazione.getStato() != PrenotazioneDTO.StatoPrenotazione.ATTIVA) {
                response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                    URLEncoder.encode("Impossibile annullare una prenotazione che non è attiva", "UTF-8"));
                return;
            }
            
            // Verifica che lo slot sia almeno un giorno prima
            if (lezione.getDataInizio().minusDays(1).isBefore(LocalDateTime.now())) {
                response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                    URLEncoder.encode("Impossibile annullare: manca meno di un giorno alla lezione", "UTF-8"));
                return;
            }
            
            // Effettua l'annullamento (transazione: annulla prenotazione e libera slot)
            boolean success = lezioneDAO.annullaPrenotazione(idPrenotazione);
            
            if (success) {
                // Successo
                response.sendRedirect(request.getContextPath() + "/storico-lezioni?success=" + 
                    URLEncoder.encode("Prenotazione annullata con successo!", "UTF-8"));
            } else {
                // Fallimento
                response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                    URLEncoder.encode("Impossibile annullare la prenotazione", "UTF-8"));
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                URLEncoder.encode("ID prenotazione non valido", "UTF-8"));
        } catch (IllegalArgumentException e) {
            response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                URLEncoder.encode(e.getMessage(), "UTF-8"));
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                URLEncoder.encode("Errore di database durante l'annullamento", "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                URLEncoder.encode("Errore durante l'annullamento: " + e.getMessage(), "UTF-8"));
        }
    }
}
