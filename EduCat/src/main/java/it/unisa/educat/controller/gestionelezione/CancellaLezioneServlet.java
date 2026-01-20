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
public class CancellaLezioneServlet extends HttpServlet {
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
            String idLezioneStr = request.getParameter("idLezione");
            if (idLezioneStr == null || idLezioneStr.trim().isEmpty()) {
                throw new IllegalArgumentException("ID prenotazione non specificato");
            }
            
            int idLezione = Integer.parseInt(idLezioneStr);
            
            // Verifica che l'utente sia uno studente o un tutor
            if (!"TUTOR".equals(utente.getTipo().toString())) {
                response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                    URLEncoder.encode("Solo tutor possono annullare lezioni", "UTF-8"));
                return;
            }
            
            // Recupera la prenotazione con la lezione associata
            lezioneDAO.doDeleteLezione(idLezione);
            
                response.sendRedirect(request.getContextPath() + "/storico-lezioni?success=" + 
                    URLEncoder.encode("Prenotazione annullata con successo!", "UTF-8"));
           
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
