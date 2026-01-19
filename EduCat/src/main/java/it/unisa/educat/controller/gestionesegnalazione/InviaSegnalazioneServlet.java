package it.unisa.educat.controller.gestionesegnalazione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.SQLException;

import it.unisa.educat.dao.GestioneSegnalazioneDAO;
import it.unisa.educat.model.SegnalazioneDTO;
import it.unisa.educat.model.UtenteDTO;

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
        
        // ★ IMPOSTA SUBITO JSON ★
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                URLEncoder.encode("Sessione scaduta", "UTF-8"));
            return;
        }
        
        UtenteDTO segnalante = (UtenteDTO) session.getAttribute("utente");
        if (segnalante == null) {
        	response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                    URLEncoder.encode("Accesso richiesto", "UTF-8"));
            return;
        }
        
        try {
            // DEBUG
            System.out.println("=== INVIO SEGNALAZIONE ===");
            System.out.println("Segnalante: " + segnalante.getUID() + " - " + segnalante.getNome());
            
            String idSegnalatoStr = request.getParameter("idSegnalato");
            String descrizione = request.getParameter("descrizione");
            
            System.out.println("Parametri ricevuti:");
            System.out.println("idSegnalato: " + idSegnalatoStr);
            System.out.println("descrizione: " + descrizione);
            
            if (idSegnalatoStr == null || idSegnalatoStr.trim().isEmpty()) {
                System.out.println("ERRORE: idSegnalato mancante");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"ID segnalato mancante\"}");
                return;
            }
            
            if (descrizione == null || descrizione.trim().isEmpty()) {
                System.out.println("ERRORE: descrizione mancante");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"Descrizione mancante\"}");
                return;
            }
            
            int idSegnalato;
            try {
                idSegnalato = Integer.parseInt(idSegnalatoStr.trim());
                System.out.println("ID segnalato parsato: " + idSegnalato);
            } catch (NumberFormatException e) {
                System.out.println("ERRORE: idSegnalato non numerico: " + idSegnalatoStr);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"ID utente non valido\"}");
                return;
            }
            
            // Verifica che non stia segnalando se stesso
            if (idSegnalato == segnalante.getUID()) {
                System.out.println("ERRORE: sta segnalando se stesso");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"success\": false, \"message\": \"Non puoi segnalare te stesso\"}");
                return;
            }
            
            // Crea segnalazione
            SegnalazioneDTO segnalazione = new SegnalazioneDTO();
            segnalazione.setDescrizione(descrizione.trim());
            segnalazione.setIdSegnalante(segnalante.getUID());
            segnalazione.setIdSegnalato(idSegnalato);
            
            System.out.println("Creazione segnalazione DTO:");
            System.out.println("- Descrizione: " + segnalazione.getDescrizione());
            System.out.println("- ID Segnalante: " + segnalazione.getIdSegnalante());
            System.out.println("- ID Segnalato: " + segnalazione.getIdSegnalato());
            
            // Salva nel database
            boolean success = segnalazioneDAO.doSave(segnalazione);
            
            System.out.println("Risultato doSave: " + success);
            
            if (success) {
                System.out.println("SUCCESSO! ID segnalazione: " + segnalazione.getIdSegnalazione());
                out.write("{\"success\": true, \"message\": \"Segnalazione inviata\", \"idSegnalazione\": " + segnalazione.getIdSegnalazione() + "}");
                // ★ NON FARE FORWARD/REDIRECT DOPO JSON ★
            } else {
                System.out.println("ERRORE: doSave ha restituito false");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"success\": false, \"message\": \"Errore durante il salvataggio\"}");
            }
            
        } catch (SQLException e) {
            System.err.println("ERRORE SQL:");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\": false, \"message\": \"Errore di database: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("ERRORE GENERICO:");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\": false, \"message\": \"Errore: " + e.getMessage() + "\"}");
        }
    }
}