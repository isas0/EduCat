package it.unisa.educat.controller.gestionelezione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;

/**
 * Servlet implementation class StoricoLezioniServlet
 */
@WebServlet("/storico-lezioni")
public class StoricoLezioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private GestioneLezioneDAO lezioneDAO;
    
    @Override
    public void init() throws ServletException {
        lezioneDAO = new GestioneLezioneDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
        if (utente == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        try {
            String tipoUtente = utente.getTipo().toString();
            List<PrenotazioneDTO> prenotazioni = new ArrayList<PrenotazioneDTO>();
            
            if ("STUDENTE".equals(tipoUtente)) {
                // Per studente: tutte le sue prenotazioni
                prenotazioni = lezioneDAO.getPrenotazioniByStudente(utente.getUID());
            } else if ("TUTOR".equals(tipoUtente)) {
                // Per tutor: tutte le prenotazioni delle sue lezioni
                prenotazioni = lezioneDAO.getPrenotazioniByTutor(utente.getUID());
            } else {
                // Altri tipi di utente
                session.setAttribute("errorMessage", "Utente non autorizzato a visualizzare lo storico");
                response.sendRedirect("accessoNegato.jsp");
                return;
            }
            
            // Separa le prenotazioni in passate e future
            List<PrenotazioneDTO> lezioniPassate = new ArrayList<>();
            List<PrenotazioneDTO> lezioniFuture = new ArrayList<>();
            List<PrenotazioneDTO> lezioniAnnullate = new ArrayList<>();
            
            LocalDateTime now = LocalDateTime.now();
            
            for (PrenotazioneDTO prenotazione : prenotazioni) {
                if (prenotazione.getStato() == PrenotazioneDTO.StatoPrenotazione.ANNULLATA) {
                    lezioniAnnullate.add(prenotazione);
                } else if (prenotazione.getLezione().getData().isBefore(now)) {
                    lezioniPassate.add(prenotazione);
                } else {
                    lezioniFuture.add(prenotazione);
                }
            }
            
            // Ordina per data (più recenti prima per passate, più vicine prima per future)
            lezioniPassate.sort((p1, p2) -> 
                p2.getLezione().getData().compareTo(p1.getLezione().getData()));
            
            lezioniFuture.sort((p1, p2) -> 
                p1.getLezione().getData().compareTo(p2.getLezione().getData()));
            
            lezioniAnnullate.sort((p1, p2) -> 
                p2.getLezione().getData().compareTo(p1.getLezione().getData()));
            
            // Imposta gli attributi per la JSP
            request.setAttribute("lezioniPassate", lezioniPassate);
            request.setAttribute("lezioniFuture", lezioniFuture);
            request.setAttribute("lezioniAnnullate", lezioniAnnullate);
            request.setAttribute("tipoUtente", tipoUtente);
            request.setAttribute("utente", utente);
            
            // Inoltra alla pagina JSP
            request.getRequestDispatcher("/storicoLezioni.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database nel recupero dello storico");
            response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore nel recupero dello storico: " + e.getMessage());
            response.sendRedirect("error.jsp");
        }
    }
}


