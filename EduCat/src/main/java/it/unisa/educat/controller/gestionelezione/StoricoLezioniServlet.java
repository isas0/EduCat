package it.unisa.educat.controller.gestionelezione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;

@WebServlet("/storico-lezioni")
public class StoricoLezioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private GestioneLezioneDAO lezioneDAO;
    
    @Override
    public void init() throws ServletException {
        lezioneDAO = new GestioneLezioneDAO();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
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
            // Gestisci messaggi da URL
            String error = request.getParameter("error");
            String success = request.getParameter("success");
            
            if (error != null && !error.trim().isEmpty()) {
                request.setAttribute("errorMessage", URLDecoder.decode(error, "UTF-8"));
            }
            
            if (success != null && !success.trim().isEmpty()) {
                request.setAttribute("successMessage", URLDecoder.decode(success, "UTF-8"));
            }
            
            String tipoUtente = utente.getTipo().toString();
            List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
            List<LezioneDTO> lezioni = new ArrayList<>();
            
            if ("STUDENTE".equals(tipoUtente) || "GENITORE".equals(tipoUtente)) {
                prenotazioni = lezioneDAO.getPrenotazioniByStudente(utente.getUID());
                
                segnaLezioniPassateComeConcluse(prenotazioni);
            } else if ("TUTOR".equals(tipoUtente)) {
                prenotazioni = lezioneDAO.getPrenotazioniByTutor(utente.getUID());
                lezioni = lezioneDAO.getLezioniByTutor(utente.getUID());
                request.setAttribute("lezioni", lezioni);
                segnaLezioniPassateComeConcluse(prenotazioni);
            } else {
                response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                    URLEncoder.encode("Utente non autorizzato a visualizzare lo storico", "UTF-8"));
                return;
            }
            
            // Imposta attributi per la JSP
            request.setAttribute("prenotazioni", prenotazioni);
            request.setAttribute("tipoUtente", tipoUtente);
            request.setAttribute("utente", utente);
            
            // Inoltra alla pagina JSP
            if ("STUDENTE".equals(tipoUtente) || "GENITORE".equals(tipoUtente)) {
                request.getRequestDispatcher("/prenotazioni.jsp").forward(request, response);
            } else if ("TUTOR".equals(tipoUtente)) {
                request.getRequestDispatcher("/homeTutor.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                URLEncoder.encode("Errore di database nel recupero dello storico", "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                URLEncoder.encode("Errore nel recupero dello storico: " + e.getMessage(), "UTF-8"));
        }
    }
    
    
    
    
    private void segnaLezioniPassateComeConcluse(List<PrenotazioneDTO> prenotazioni) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        
        for (PrenotazioneDTO prenotazione : prenotazioni) {
            if (prenotazione.getLezione() == null) continue;
            
            LezioneDTO lezione = prenotazione.getLezione();
            
            // Condizioni per segnare come conclusa:
            // 1. La lezione è PRENOTATA
            // 2. La data di fine è passata
            // 3. La prenotazione è ATTIVA
            
            if (lezione.getStato() == LezioneDTO.StatoLezione.PRENOTATA &&
                lezione.getDataFine().isBefore(now) &&
                prenotazione.getStato() == PrenotazioneDTO.StatoPrenotazione.ATTIVA) {
                
                try {
                    // Aggiorna stato della lezione a CONCLUSA
                    if (lezioneDAO.setLezioneAsConclusa(lezione.getIdLezione())) {
                        lezione.setStato(LezioneDTO.StatoLezione.CONCLUSA);
                    }
                    
                    // Aggiorna stato della prenotazione a CONCLUSA
                    if (lezioneDAO.doUpdateStatoPrenotazione(prenotazione.getIdPrenotazione(), "CONCLUSA")) {
                        prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.CONCLUSA);
                    }
                    
                } catch (SQLException e) {
                    System.err.println("Errore nell'aggiornamento automatico lezione ID: " + 
                                     lezione.getIdLezione() + ": " + e.getMessage());
                    // Continua con le altre prenotazioni
                }
            }
        }
    }
    
    
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}


