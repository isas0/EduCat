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
import java.util.List;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;

/**
 * Servlet implementation class AnnullaPrenotazioneServlet
 */
@WebServlet("/annulla-prenotazione")
public class AnnullaPrenotazioneServlet extends HttpServlet {
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
        
        UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
        if (utente == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        try {
            // Ottieni parametri dalla richiesta
            String idPrenotazioneStr = request.getParameter("idPrenotazione");
            if (idPrenotazioneStr == null || idPrenotazioneStr.trim().isEmpty()) {
                throw new IllegalArgumentException("ID prenotazione non specificato");
            }
            
            int idPrenotazione = Integer.parseInt(idPrenotazioneStr);
            
            // Verifica che l'utente sia uno studente o un tutor
            if (!"STUDENTE".equals(utente.getTipo()) && !"TUTOR".equals(utente.getTipo())) {
                session.setAttribute("errorMessage", "Solo studenti o tutor possono annullare prenotazioni");
                response.sendRedirect("accessoNegato.jsp");
                return;
            }
            
            // Recupera la prenotazione per verificare che appartenga allo studente
            // Per fare questo, prima recuperiamo tutte le prenotazioni dello studente
            // e verifichiamo che quella richiesta appartenga a lui
         // Verifica autorizzazione e recupera la prenotazione
            PrenotazioneDTO prenotazione = null;
            boolean autorizzato = false;

            if ("STUDENTE".equals(utente.getTipo())) {
                // Per lo studente: recupera le sue prenotazioni
                List<PrenotazioneDTO> prenotazioniStudente = lezioneDAO.getPrenotazioniByStudente(utente.getUID());
                
                for (PrenotazioneDTO p : prenotazioniStudente) {
                    if (p.getIdPrenotazione() == idPrenotazione) {
                        prenotazione = p;
                        autorizzato = true;
                        break;
                    }
                }
            } else if ("TUTOR".equals(utente.getTipo())) {
                // Per il tutor: recupera tutte le prenotazioni delle sue lezioni
                prenotazione = lezioneDAO.getPrenotazioneById(idPrenotazione);
                
                if (prenotazione != null) {
                    // Verifica che il tutor sia il tutor della lezione
                    if (prenotazione.getLezione().getTutor().getUID() == utente.getUID()) {
                        autorizzato = true;
                    }
                }
            }

            if (!autorizzato || prenotazione == null) {
                session.setAttribute("errorMessage", "Prenotazione non trovata o non sei autorizzato");
                response.sendRedirect("storicoPrenotazioni.jsp?error=prenotazione_non_trovata");
                return;
            }

            // Verifica che la prenotazione sia ancora attiva
            if (prenotazione.getStato() != PrenotazioneDTO.StatoPrenotazione.ATTIVA) {
                session.setAttribute("errorMessage", "Impossibile annullare una prenotazione che non è attiva");
                response.sendRedirect("storicoPrenotazioni.jsp?error=stato_non_valido");
                return;
            }

            // Verifica che lezione sia almeno un giorno prima
            if (prenotazione.getLezione().getData().minusDays(1).isBefore(LocalDateTime.now())) {
                session.setAttribute("errorMessage", "Impossibile annullare: manca meno di un giorno alla lezione");
                response.sendRedirect("storicoPrenotazioni.jsp?error=troppo_tardi");
                return;
            }
            
            // Effettua l'annullamento
            boolean success = lezioneDAO.doUpdateStatoPrenotazione(idPrenotazione, "ANNULLATA");
            
            if (success) {
                // Successo
                session.setAttribute("successMessage", "Prenotazione annullata con successo!");
                response.sendRedirect("storicoPrenotazioni.jsp?success=true");
            } else {
                // Fallimento
                session.setAttribute("errorMessage", "Impossibile annullare la prenotazione");
                response.sendRedirect("storicoPrenotazioni.jsp?error=annullamento_fallito");
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID prenotazione non valido");
            response.sendRedirect("storicoPrenotazioni.jsp?error=id_invalido");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect("storicoPrenotazioni.jsp?error=parametri_mancanti");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database durante l'annullamento");
            response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore durante l'annullamento: " + e.getMessage());
            response.sendRedirect("error.jsp");
        }
    }
    
    /**
     * GET per mostrare la pagina di conferma annullamento
     */
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
        
        // Verifica che l'utente sia uno studente
        if (!"STUDENTE".equals(utente.getTipo())) {
            session.setAttribute("errorMessage", "Solo gli studenti possono annullare prenotazioni");
            request.getRequestDispatcher("/accessoNegato.jsp").forward(request, response);
            return;
        }
        
        String idPrenotazioneStr = request.getParameter("idPrenotazione");
        if (idPrenotazioneStr == null || idPrenotazioneStr.trim().isEmpty()) {
            response.sendRedirect("storicoPrenotazioni.jsp?error=id_mancante");
            return;
        }
        
        try {
            int idPrenotazione = Integer.parseInt(idPrenotazioneStr);
            
            // Recupera tutte le prenotazioni dello studente
            List<PrenotazioneDTO> prenotazioniStudente = lezioneDAO.getPrenotazioniByStudente(utente.getUID());
            
            PrenotazioneDTO prenotazioneDaAnnullare = null;
            
            for (PrenotazioneDTO prenotazione : prenotazioniStudente) {
                if (prenotazione.getIdPrenotazione() == idPrenotazione) {
                    prenotazioneDaAnnullare = prenotazione;
                    break;
                }
            }
            
            if (prenotazioneDaAnnullare == null) {
                session.setAttribute("errorMessage", "Prenotazione non trovata o non appartiene all'utente");
                response.sendRedirect("storicoPrenotazioni.jsp?error=prenotazione_non_trovata");
                return;
            }
            
            // Verifica che la prenotazione sia ancora attiva
            if (prenotazioneDaAnnullare.getStato() != PrenotazioneDTO.StatoPrenotazione.ATTIVA) {
                session.setAttribute("errorMessage", "Impossibile annullare una prenotazione che non è attiva");
                response.sendRedirect("storicoPrenotazioni.jsp?error=stato_non_valido");
                return;
            }
            
            // Verifica che la lezione non sia già iniziata
            if (prenotazioneDaAnnullare.getLezione().getData().isBefore(LocalDateTime.now())) {
                session.setAttribute("errorMessage", "Impossibile annullare una lezione già iniziata");
                response.sendRedirect("storicoPrenotazioni.jsp?error=lezione_già_iniziata");
                return;
            }
            
            // Mostra pagina di conferma annullamento
            request.setAttribute("prenotazione", prenotazioneDaAnnullare);
            request.getRequestDispatcher("/confermaAnnullamento.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "ID prenotazione non valido");
            response.sendRedirect("storicoPrenotazioni.jsp?error=id_invalido");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database");
            response.sendRedirect("error.jsp");
        }
    }
}
