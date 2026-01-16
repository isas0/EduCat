package it.unisa.educat.controller.gestionesegnalazione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import it.unisa.educat.dao.GestioneSegnalazioneDAO;
import it.unisa.educat.model.SegnalazioneDTO;
import it.unisa.educat.model.UtenteDTO;

/**
 * Servlet implementation class InviaSegnalazioneServlet
 */
@WebServlet("/invia-segnalazione")
public class InviaSegnalazioneServlet extends HttpServlet {
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
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\": false, \"message\": \"Utente non autenticato\"}");
            return;
        }
        
        UtenteDTO segnalante = (UtenteDTO) session.getAttribute("utente");
        if (segnalante == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\": false, \"message\": \"Utente non autenticato\"}");
            return;
        }
        
        try {
            // Leggi il corpo della richiesta JSON
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            // Se non vuoi usare JSON, usa i parametri normali:
            String idSegnalatoStr = request.getParameter("idSegnalato");
            String descrizione = request.getParameter("descrizione");
            
            if (idSegnalatoStr == null || descrizione == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"Parametri mancanti\"}");
                return;
            }
            
            int idSegnalato = Integer.parseInt(idSegnalatoStr);
            
            // Verifica che non stia segnalando se stesso
            if (idSegnalato == segnalante.getUID()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\": false, \"message\": \"Non puoi segnalare te stesso\"}");
                return;
            }
            
            // Crea e invia segnalazione
            SegnalazioneDTO segnalazione = new SegnalazioneDTO();
            segnalazione.setIdSegnalante(segnalante.getUID());
            segnalazione.setIdSegnalato(idSegnalato);
            segnalazione.setDescrizione(descrizione.trim());
            
            boolean success = segnalazioneDAO.doSave(segnalazione);
            
            if (success) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true, \"message\": \"Segnalazione inviata\", \"idSegnalazione\": " + segnalazione.getIdSegnalazione() + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"success\": false, \"message\": \"Errore durante il salvataggio\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\": false, \"message\": \"ID utente non valido\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Errore di database\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Errore: " + e.getMessage() + "\"}");
        }
    }
}