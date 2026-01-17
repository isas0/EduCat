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
import java.util.Comparator;
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
            List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
            
            if ("STUDENTE".equals(tipoUtente) || "GENITORE".equals(tipoUtente)) {
                // Per studente: tutte le sue prenotazioni CON SLOT
                prenotazioni = lezioneDAO.getPrenotazioniByStudente(utente.getUID());
            } else if ("TUTOR".equals(tipoUtente)) {
                // Per tutor: tutte le prenotazioni delle sue lezioni CON SLOT
                prenotazioni = lezioneDAO.getPrenotazioniByTutor(utente.getUID());
            } else {
                session.setAttribute("errorMessage", "Utente non autorizzato a visualizzare lo storico");
                response.sendRedirect("accessoNegato.jsp");
                return;
            }
            
            
            // Imposta attributi per la JSP
            request.setAttribute("prenotazioni", prenotazioni);
            //request.setAttribute("lezioniFuture", lezioniFuture);
            //request.setAttribute("lezioniAnnullate", lezioniAnnullate);
            request.setAttribute("tipoUtente", tipoUtente);
            request.setAttribute("utente", utente);
            
            // Inoltra alla pagina JSP
            
            
            if ("STUDENTE".equals(tipoUtente)) {
            	request.getRequestDispatcher("/prenotazioni.jsp").forward(request, response);
            } else if ("TUTOR".equals(tipoUtente)) {
            	request.getRequestDispatcher("/homeTutor.jsp").forward(request, response);
            }
            
            
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database nel recupero dello storico");
            response.sendRedirect("homePageStudenteGenitore.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore nel recupero dello storico: " + e.getMessage());
            response.sendRedirect("homePageStudenteGenitore.jsp");
        }
    }
}


