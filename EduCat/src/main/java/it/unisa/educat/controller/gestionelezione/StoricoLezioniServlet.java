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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.*;

/**
 * Servlet implementation class StoricoLezioniServlet
 */
@WebServlet("/storico-lezioni")
public class StoricoLezioniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private GestioneLezioneDAO lezioneDAO;
    
    @Override
    public void init() throws ServletException {
        lezioneDAO = new GestioneLezioneDAO();
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
        
        try {
            String tipoUtente = utente.getTipo().toString();
            List<PrenotazioneDTO> prenotazioni = new ArrayList<>();
            
            if ("STUDENTE".equals(tipoUtente)) {
                // Per studente: tutte le sue prenotazioni CON SLOT
                prenotazioni = lezioneDAO.getPrenotazioniByStudente(utente.getUID());
            } else if ("TUTOR".equals(tipoUtente)) {
                // Per tutor: tutte le prenotazioni delle sue lezioni CON SLOT
                prenotazioni = lezioneDAO.getPrenotazioniByTutor(utente.getUID());
            } else {
                session.setAttribute("errorMessage", "Utente non autorizzato a visualizzare lo storico");
                response.sendRedirect("accessoNegato.jsp");
                return;
            }
            
            /*
            // Per ogni prenotazione, recupera lo slot se non è già presente
            for (PrenotazioneDTO prenotazione : prenotazioni) {
                if (prenotazione.getSlot() == null) {
                    SlotDTO slot = lezioneDAO.getSlotByPrenotazioneId(prenotazione.getIdPrenotazione());
                    prenotazione.setSlot(slot);
                    
                    // Se c'è lo slot, aggiorna la data nella lezione per retrocompatibilità
                    if (slot != null && prenotazione.getLezione() != null) {
                        prenotazione.getLezione().setData(slot.getDataOraInizio());
                    }
                }
            }*/
            
            // Separa le prenotazioni in passate, future e annullate
            List<PrenotazioneDTO> lezioniPassate = new ArrayList<>();
            List<PrenotazioneDTO> lezioniFuture = new ArrayList<>();
            List<PrenotazioneDTO> lezioniAnnullate = new ArrayList<>();
            
            LocalDateTime now = LocalDateTime.now();
            
            for (PrenotazioneDTO prenotazione : prenotazioni) {
                LocalDateTime dataLezione = null;
                
                // Prima cerca la data nello slot
                if (prenotazione.getSlot() != null) {
                    dataLezione = prenotazione.getSlot().getDataOraInizio();
                } 
                // Fallback: data nella lezione (per retrocompatibilità)
                else if (prenotazione.getLezione() != null && prenotazione.getLezione().getData() != null) {
                    dataLezione = prenotazione.getLezione().getData();
                }
                
                if (prenotazione.getStato() == PrenotazioneDTO.StatoPrenotazione.ANNULLATA) {
                    lezioniAnnullate.add(prenotazione);
                } else if (dataLezione != null && dataLezione.isBefore(now)) {
                    lezioniPassate.add(prenotazione);
                } else if (dataLezione != null) {
                    lezioniFuture.add(prenotazione);
                } else {
                    // Se non c'è data, considerala passata (caso strano)
                    lezioniPassate.add(prenotazione);
                }
            }
            
            // Ordina per data
            Comparator<PrenotazioneDTO> dateComparator = (p1, p2) -> {
                LocalDateTime data1 = p1.getSlot() != null ? p1.getSlot().getDataOraInizio() : 
                                   (p1.getLezione() != null ? p1.getLezione().getData() : null);
                LocalDateTime data2 = p2.getSlot() != null ? p2.getSlot().getDataOraInizio() : 
                                   (p2.getLezione() != null ? p2.getLezione().getData() : null);
                
                if (data1 == null && data2 == null) return 0;
                if (data1 == null) return 1;
                if (data2 == null) return -1;
                
                return data1.compareTo(data2);
            };
            
            // Passate: più recenti prima
            lezioniPassate.sort(dateComparator.reversed());
            
            // Future: più vicine prima
            lezioniFuture.sort(dateComparator);
            
            // Annullate: più recenti prima
            lezioniAnnullate.sort(dateComparator.reversed());
            
            // Imposta attributi per la JSP
            request.setAttribute("lezioniPassate", lezioniPassate);
            request.setAttribute("lezioniFuture", lezioniFuture);
            request.setAttribute("lezioniAnnullate", lezioniAnnullate);
            request.setAttribute("tipoUtente", tipoUtente);
            request.setAttribute("utente", utente);
            
            // Inoltra alla pagina JSP
            request.getRequestDispatcher("/storicoLezioni.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database nel recupero dello storico");
            response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore nel recupero dello storico: " + e.getMessage());
            response.sendRedirect("error.jsp");
        }
    }
}


