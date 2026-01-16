package it.unisa.educat.controller.gestionelezione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;

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
            response.sendRedirect("../login.jsp");
            return;
        }
        
        UtenteDTO studente = (UtenteDTO) session.getAttribute("utente");
        if (studente == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        try {
            // Ottieni parametri dalla richiesta
            String idLezioneStr = request.getParameter("idLezione");
            if (idLezioneStr == null || idLezioneStr.trim().isEmpty()) {
                throw new IllegalArgumentException("ID lezione non specificato");
            }
            
            int idLezione = Integer.parseInt(idLezioneStr);
            
            // Verifica che l'utente sia effettivamente uno studente
            // (Assumendo che UtenteDTO abbia un campo 'tipo' con valori 'STUDENTE', 'TUTOR', ecc.)
            if (!"STUDENTE".equals(studente.getTipo())) {
                session.setAttribute("errorMessage", "Solo gli studenti possono prenotare lezioni");
                response.sendRedirect("accessoNegato.jsp");
                return;
            }
            
            // Recupera la lezione dal database per verificarne la disponibilità
            LezioneDTO lezione = lezioneDAO.getLezioneById(idLezione);
            
            if (lezione == null) {
                session.setAttribute("errorMessage", "Lezione non trovata");
                response.sendRedirect("cercaLezione.jsp?error=lezione_non_trovata");
                return;
            }
            
            // Verifica se la lezione è già passata
            if (lezione.getData().isBefore(LocalDateTime.now())) {
                session.setAttribute("errorMessage", "Impossibile prenotare una lezione passata");
                response.sendRedirect("dettaglioLezione.jsp?id=" + idLezione + "&error=lezione_passata");
                return;
            }
            
            // Verifica se lo studente ha già prenotato questa lezione
            // (Implementa il metodo nel DAO)
            boolean giaPrenotato = lezioneDAO.hasStudentePrenotatoLezione(studente.getUID(), idLezione);
            if (giaPrenotato) {
                session.setAttribute("errorMessage", "Hai già prenotato questa lezione");
                response.sendRedirect("dettaglioLezione.jsp?id=" + idLezione + "&error=gia_prenotata");
                return;
            }
            
            // Verifica posti disponibili (se applicabile)
            /*int postiDisponibili = lezioneDAO.getPostiDisponibili(idLezione);
            if (postiDisponibili <= 0) {
                session.setAttribute("errorMessage", "Nessun posto disponibile per questa lezione");
                response.sendRedirect("dettaglioLezione.jsp?id=" + idLezione + "&error=posti_esauriti");
                return;
            }*/
            
            // Crea l'oggetto PrenotazioneDTO
            PrenotazioneDTO prenotazione = new PrenotazioneDTO(
                0, // id verrà generato dal database
                LocalDate.now(), // dataPrenotazione
                PrenotazioneDTO.StatoPrenotazione.ATTIVA, // stato iniziale
                studente, // studente
                lezione // lezione
            );
            
            // Effettua la prenotazione
            boolean success = lezioneDAO.doSavePrenotazione(prenotazione);
            
            if (success) {
                // Successo: reindirizza con messaggio di successo
                session.setAttribute("successMessage", "Lezione prenotata con successo!");
                response.sendRedirect("dettaglioLezione.jsp?id=" + idLezione + "&success=true");
            } else {
                // Fallimento
                session.setAttribute("errorMessage", "Impossibile prenotare la lezione");
                response.sendRedirect("dettaglioLezione.jsp?id=" + idLezione + "&error=prenotazione_fallita");
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID lezione non valido");
            //response.sendRedirect("cercaLezione.jsp?error=id_invalido");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMessage", e.getMessage());
            //response.sendRedirect("cercaLezione.jsp?error=parametri_mancanti");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database durante la prenotazione");
            //response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore durante la prenotazione: " + e.getMessage());
            //response.sendRedirect("error.jsp");
        }
    }
    
    /**
     * GET per mostrare la pagina di conferma prenotazione
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        UtenteDTO studente = (UtenteDTO) session.getAttribute("utente");
        if (studente == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        // Verifica che l'utente sia uno studente
        if (!"STUDENTE".equals(studente.getTipo())) {
            session.setAttribute("errorMessage", "Solo gli studenti possono prenotare lezioni");
            request.getRequestDispatcher("/accessoNegato.jsp").forward(request, response);
            return;
        }
        
        String idLezioneStr = request.getParameter("idLezione");
        if (idLezioneStr == null || idLezioneStr.trim().isEmpty()) {
            response.sendRedirect("cercaLezione.jsp?error=id_mancante");
            return;
        }
        
        try {
            int idLezione = Integer.parseInt(idLezioneStr);
            
            // Recupera i dettagli della lezione
            LezioneDTO lezione = lezioneDAO.getLezioneById(idLezione);
            if (lezione == null) {
                session.setAttribute("errorMessage", "Lezione non trovata");
                response.sendRedirect("cercaLezione.jsp?error=lezione_non_trovata");
                return;
            }
            
            // Verifica se lo studente ha già prenotato
            boolean giaPrenotato = lezioneDAO.hasStudentePrenotatoLezione(studente.getUID(), idLezione);
            if (giaPrenotato) {
                session.setAttribute("errorMessage", "Hai già prenotato questa lezione");
                response.sendRedirect("dettaglioLezione.jsp?id=" + idLezione + "&error=gia_prenotata");
                return;
            }
            
            // Verifica posti disponibili
            //int postiDisponibili = lezioneDAO.getPostiDisponibili(idLezione);
            //request.setAttribute("postiDisponibili", postiDisponibili);
            
            // Mostra pagina di conferma prenotazione
            request.setAttribute("lezione", lezione);
            request.setAttribute("idLezione", idLezione);
            request.getRequestDispatcher("/lezioni.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID lezione non valido");
            //response.sendRedirect("cercaLezione.jsp?error=id_invalido");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database");
            //response.sendRedirect("error.jsp");
        }
    }
}