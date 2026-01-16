package it.unisa.educat.controller.gestionesegnalazione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

import it.unisa.educat.dao.GestioneSegnalazioneDAO;
import it.unisa.educat.model.UtenteDTO;

/**
 * Servlet implementation class RisolviSegnalazioneServlet
 */
@WebServlet("/risolvi-segnalazione")
public class RisolviSegnalazioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private GestioneSegnalazioneDAO segnalazioneDAO;
    
    @Override
    public void init() throws ServletException {
        segnalazioneDAO = new GestioneSegnalazioneDAO();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
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
        
        // Verifica che l'utente sia admin o abbia permessi
         if (!"ADMIN".equals(utente.getTipo().toString())) {
             session.setAttribute("errorMessage", "Accesso negato: solo gli amministratori possono risolvere segnalazioni");
             response.sendRedirect("accessoNegato.jsp");
             return;
         }
        
        try {
            String idSegnalazioneStr = request.getParameter("idSegnalazione");
            String azione = request.getParameter("azione"); // "elimina" o "segna-risolta"
            
            if (idSegnalazioneStr == null || idSegnalazioneStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "ID segnalazione non specificato");
                response.sendRedirect("lista-segnalazioni");
                return;
            }
            
            int idSegnalazione = Integer.parseInt(idSegnalazioneStr);
            
            boolean success = false;
            String message = "";
            
            if ("elimina".equals(azione)) {
                // Elimina la segnalazione
                success = risolviSegnalazioneElimina(idSegnalazione);
                message = success ? "Segnalazione eliminata con successo" : "Errore durante l'eliminazione";
            } else {
                // Di default, elimina (se non hai uno stato "risolta" nel database)
                success = risolviSegnalazioneElimina(idSegnalazione);
                message = success ? "Segnalazione risolta con successo" : "Errore durante la risoluzione";
            }
            
            if (success) {
                session.setAttribute("successMessage", message);
            } else {
                session.setAttribute("errorMessage", message);
            }
            
            response.sendRedirect("lista-segnalazioni");
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID segnalazione non valido");
            response.sendRedirect("lista-segnalazioni");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database durante la risoluzione");
            response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore: " + e.getMessage());
            response.sendRedirect("error.jsp");
        }
    }
    
    /**
     * Risolve la segnalazione eliminandola
     */
    private boolean risolviSegnalazioneElimina(int idSegnalazione) throws SQLException {
        // Utilizza il metodo doDelete del DAO
        return segnalazioneDAO.doDelete(idSegnalazione);
    }
    
    /*
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
        
        // Verifica che l'utente sia admin o abbia permessi
        // if (!"ADMIN".equals(utente.getTipo())) {
        //     session.setAttribute("errorMessage", "Accesso negato");
        //     response.sendRedirect("accessoNegato.jsp");
        //     return;
        // }
        
        String idSegnalazioneStr = request.getParameter("idSegnalazione");
        if (idSegnalazioneStr == null || idSegnalazioneStr.trim().isEmpty()) {
            session.setAttribute("errorMessage", "ID segnalazione non specificato");
            response.sendRedirect("lista-segnalazioni");
            return;
        }
        
        try {
            int idSegnalazione = Integer.parseInt(idSegnalazioneStr);
            
            // Mostra pagina di conferma
            request.setAttribute("idSegnalazione", idSegnalazione);
            request.getRequestDispatcher("/confermaRisoluzione.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID segnalazione non valido");
            response.sendRedirect("lista-segnalazioni");
        }
    }*/
}
