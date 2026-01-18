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
import java.util.List;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;
import it.unisa.educat.model.LezioneDTO.StatoLezione;

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
                throw new IllegalArgumentException("ID slot non specificato");
            }
            
            int idLezione = Integer.parseInt(idLezioneStr);
            
            // Verifica che l'utente sia effettivamente uno studente
            if (!"STUDENTE".equals(studente.getTipo().toString())) {
                session.setAttribute("errorMessage", "Solo gli studenti possono prenotare lezioni");
                response.sendRedirect("accessoNegato.jsp");
                return;
            }
            
            // Recupera la lezione dal database
            LezioneDTO lezione = lezioneDAO.getLezioneById(idLezione);
            
            if (lezione == null) {
                session.setAttribute("errorMessage", "Slot non trovato");
                response.sendRedirect("cercaLezione.jsp?error=slot_non_trovato");
                return;
            }
            
            // Verifica che la lezione sia disponibile
            if (lezione.getStato() != StatoLezione.PIANIFICATA) {
                session.setAttribute("errorMessage", "Questo slot non è più disponibile");
                response.sendRedirect("prenotazioni.jsp?id=" + lezione.getIdLezione() + "&error=slot_non_disponibile");
                return;
            }
            
            // Verifica che lo slot non sia già passato
            if (lezione.getDataInizio().isBefore(LocalDateTime.now())) {
                session.setAttribute("errorMessage", "Impossibile prenotare uno slot già passato");
                response.sendRedirect("prenotazioni.jsp?id=" + lezione.getIdLezione() + "&error=slot_passato");
                return;
            }
            
            // Verifica se lo studente ha già prenotato questo slot
            
            /*boolean giaPrenotato = lezioneDAO.hasStudentePrenotatoSlot(studente.getUID(), idSlot);
            if (giaPrenotato) {
                session.setAttribute("errorMessage", "Hai già prenotato questo slot");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=gia_prenotato");
                return;
            }*/
            
            // Verifica se lo studente ha già prenotato un altro slot per la stessa lezione
            /*boolean giaPrenotatoLezione = lezioneDAO.hasStudentePrenotatoLezione(studente.getUID(), slot.getLezione().getIdLezione());
            if (giaPrenotatoLezione) {
                session.setAttribute("errorMessage", "Hai già prenotato un altro slot per questa lezione");
                response.sendRedirect("dettaglioLezione.jsp?id=" + slot.getLezione().getIdLezione() + "&error=gia_prenotato_lezione");
                return;
            }*/
            
            // Crea l'oggetto PrenotazioneDTO
            PrenotazioneDTO prenotazione = new PrenotazioneDTO();
            prenotazione.setStudente(studente);
            prenotazione.setLezione(lezione);
            prenotazione.setDataPrenotazione(LocalDate.now());
            prenotazione.setStato(PrenotazioneDTO.StatoPrenotazione.ATTIVA);
            prenotazione.setImportoPagato(lezione.getPrezzo());
            prenotazione.setIndirizzoFatturazione(request.getParameter("indirizzo"));
            prenotazione.setNumeroCarta(request.getParameter("numeroCarta"));
            prenotazione.setDataScadenza(request.getParameter("scadenza"));
            prenotazione.setCvv(Integer.parseInt(request.getParameter("cvv")));
            prenotazione.setIntestatario(request.getParameter("intestatario"));
            prenotazione.setIdTutor(lezione.getTutor().getUID());
            
            // Effettua la prenotazione dello slot
            boolean success = lezioneDAO.prenotaLezione(prenotazione);
            List<PrenotazioneDTO> prenotazioni =  lezioneDAO.getPrenotazioniByStudente(studente.getUID());
            request.setAttribute("prenotazioni", prenotazioni);
            
            
            if (success) {
                // Successo: reindirizza con messaggio di successo
                session.setAttribute("successMessage", "Slot prenotato con successo!");
                request.getRequestDispatcher("prenotazioni.jsp?id=" + lezione.getIdLezione() + "&success=true").forward(request, response);
            } else {
                // Fallimento
                session.setAttribute("errorMessage", "Impossibile prenotare lo slot");
                request.getRequestDispatcher("prenotazioni.jsp?id=" + lezione.getIdLezione() + "&error=prenotazione_fallita").forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID slot non valido");
            response.sendRedirect("listaLezioni.jsp?error=id_invalido");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect("listaLezioni.jsp?error=parametri_mancanti");
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
    
    
}
