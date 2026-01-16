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
            String idSlotStr = request.getParameter("idSlot");
            if (idSlotStr == null || idSlotStr.trim().isEmpty()) {
                throw new IllegalArgumentException("ID slot non specificato");
            }
            
            int idSlot = Integer.parseInt(idSlotStr);
            
            // Verifica che l'utente sia effettivamente uno studente
            if (!"STUDENTE".equals(studente.getTipo())) {
                session.setAttribute("errorMessage", "Solo gli studenti possono prenotare lezioni");
                response.sendRedirect("accessoNegato.jsp");
                return;
            }
            
            // Recupera lo slot dal database
            SlotDTO slot = lezioneDAO.getSlotById(idSlot);
            
            if (slot == null) {
                session.setAttribute("errorMessage", "Slot non trovato");
                response.sendRedirect("cercaLezione.jsp?error=slot_non_trovato");
                return;
            }
            
            // Verifica che lo slot sia disponibile
            if (slot.getStato() != SlotDTO.StatoSlot.DISPONIBILE) {
                session.setAttribute("errorMessage", "Questo slot non è più disponibile");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=slot_non_disponibile");
                return;
            }
            
            // Verifica che lo slot non sia già passato
            if (slot.getDataOraInizio().isBefore(LocalDateTime.now())) {
                session.setAttribute("errorMessage", "Impossibile prenotare uno slot già passato");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=slot_passato");
                return;
            }
            
            // Verifica se lo studente ha già prenotato questo slot
            boolean giaPrenotato = lezioneDAO.hasStudentePrenotatoSlot(studente.getUID(), idSlot);
            if (giaPrenotato) {
                session.setAttribute("errorMessage", "Hai già prenotato questo slot");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=gia_prenotato");
                return;
            }
            
            // Verifica se lo studente ha già prenotato un altro slot per la stessa lezione
            boolean giaPrenotatoLezione = lezioneDAO.hasStudentePrenotatoLezione(studente.getUID(), slot.getLezione().getIdLezione());
            if (giaPrenotatoLezione) {
                session.setAttribute("errorMessage", "Hai già prenotato un altro slot per questa lezione");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=gia_prenotato_lezione");
                return;
            }
            
            // Crea l'oggetto PrenotazioneDTO
            PrenotazioneDTO prenotazione = new PrenotazioneDTO();
            prenotazione.setStudente(studente);
            prenotazione.setLezione(slot.getLezione());
            prenotazione.setDataPrenotazione(LocalDate.now());
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
            prenotazione.setImportoPagato(slot.getPrezzo() != null ? slot.getPrezzo() : slot.getLezione().getPrezzo());
            
            // Effettua la prenotazione dello slot
            boolean success = lezioneDAO.prenotaSlot(idSlot, prenotazione);
            
            if (success) {
                // Successo: reindirizza con messaggio di successo
                session.setAttribute("successMessage", "Slot prenotato con successo!");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&success=true");
            } else {
                // Fallimento
                session.setAttribute("errorMessage", "Impossibile prenotare lo slot");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=prenotazione_fallita");
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID slot non valido");
            response.sendRedirect("cercaLezione.jsp?error=id_invalido");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect("cercaLezione.jsp?error=parametri_mancanti");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database durante la prenotazione");
            response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore durante la prenotazione: " + e.getMessage());
            response.sendRedirect("error.jsp");
        }    
    }
    
    /**
     * GET per mostrare la pagina di conferma prenotazione
     * FORSE NON NECESSARIO VALUTARE SE CANCELLARE
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
        
        String idSlotStr = request.getParameter("idSlot");
        if (idSlotStr == null || idSlotStr.trim().isEmpty()) {
            response.sendRedirect("cercaLezione.jsp?error=id_mancante");
            return;
        }
        
        try {
            int idSlot = Integer.parseInt(idSlotStr);
            
            // Recupera i dettagli dello slot
            SlotDTO slot = lezioneDAO.getSlotById(idSlot);
            if (slot == null) {
                session.setAttribute("errorMessage", "Slot non trovato");
                response.sendRedirect("cercaLezione.jsp?error=slot_non_trovato");
                return;
            }
            
            // Verifica che lo slot sia disponibile
            if (slot.getStato() != SlotDTO.StatoSlot.DISPONIBILE) {
                session.setAttribute("errorMessage", "Questo slot non è più disponibile");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=slot_non_disponibile");
                return;
            }
            
            // Verifica se lo studente ha già prenotato questo slot
            boolean giaPrenotato = lezioneDAO.hasStudentePrenotatoSlot(studente.getUID(), idSlot);
            if (giaPrenotato) {
                session.setAttribute("errorMessage", "Hai già prenotato questo slot");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=gia_prenotato");
                return;
            }
            
            // Verifica se lo studente ha già prenotato un altro slot per la stessa lezione
            boolean giaPrenotatoLezione = lezioneDAO.hasStudentePrenotatoLezione(studente.getUID(), slot.getLezione().getIdLezione());
            if (giaPrenotatoLezione) {
                session.setAttribute("errorMessage", "Hai già prenotato un altro slot per questa lezione");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=gia_prenotato_lezione");
                return;
            }
            
            // Mostra pagina di conferma prenotazione slot
            request.setAttribute("slot", slot);
            request.setAttribute("lezione", slot.getLezione());
            request.getRequestDispatcher("/confermaPrenotazioneSlot.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID slot non valido");
            response.sendRedirect("cercaLezione.jsp?error=id_invalido");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database");
            response.sendRedirect("error.jsp");
        }
    }
    
}
