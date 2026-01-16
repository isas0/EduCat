package it.unisa.educat.controller.gestionesegnalazione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import it.unisa.educat.dao.GestioneSegnalazioneDAO;
import it.unisa.educat.model.SegnalazioneDTO;
import it.unisa.educat.model.UtenteDTO;

/**
 * Servlet implementation class ListaSegnalazioniServlet
 */
@WebServlet("/lista-segnalazioni")
public class ListaSegnalazioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private GestioneSegnalazioneDAO segnalazioneDAO;
    
    @Override
    public void init() throws ServletException {
        segnalazioneDAO = new GestioneSegnalazioneDAO();
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
        
        // Verifica che l'utente sia admin o abbia permessi per vedere le segnalazioni
        if (!"ADMIN".equals(utente.getTipo().toString())) {
             session.setAttribute("errorMessage", "Accesso negato: solo gli amministratori possono visualizzare le segnalazioni");
             response.sendRedirect("accessoNegato.jsp");
             return;
         }
        
        try {
            // Recupera tutte le segnalazioni
            List<SegnalazioneDTO> segnalazioni = segnalazioneDAO.doRetrieveAll();
            
            // Imposta attributi per la JSP
            request.setAttribute("segnalazioni", segnalazioni);
            request.setAttribute("totaleSegnalazioni", segnalazioni.size());
            
            // Forward alla pagina JSP
            request.getRequestDispatcher("/listaSegnalazioni.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database nel recupero delle segnalazioni");
            response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore: " + e.getMessage());
            response.sendRedirect("error.jsp");
        }
    }
    
    // Solo GET, niente POST
}
