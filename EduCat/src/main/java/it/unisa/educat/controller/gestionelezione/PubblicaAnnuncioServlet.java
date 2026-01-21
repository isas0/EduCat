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
import java.time.format.DateTimeFormatter;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.LezioneDTO;
import it.unisa.educat.model.UtenteDTO;

/**
 * Servlet implementation class PubblicaAnnuncioServlet
 */
@WebServlet("/pubblica-annuncio")
public class PubblicaAnnuncioServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private GestioneLezioneDAO lezioneDAO;
    
    @Override
    public void init() throws ServletException {
        lezioneDAO = new GestioneLezioneDAO();
    }
    
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
		HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                URLEncoder.encode("Sessione scaduta", "UTF-8"));
            return;
        }
        
        UtenteDTO tutor = (UtenteDTO) session.getAttribute("utente");
        if (tutor == null) {
            response.sendRedirect(request.getContextPath()+"/login.jsp?error=" + 
                URLEncoder.encode("Accesso richiesto", "UTF-8"));
            return;
        }
        
        // Verifica che l'utente sia TUTOR
        if (!"TUTOR".equals(tutor.getTipo().toString())) {
            response.sendRedirect(request.getContextPath() + "/storico-lezioni?error=" + 
                URLEncoder.encode("Solo i tutor possono creare lezioni", "UTF-8"));
            return;
        }
        
        try {
            // 1. Recupera e valida i parametri dal form
            String materia = request.getParameter("materia");
            String dataStr = request.getParameter("data");
            String oraInizioStr = request.getParameter("oraInizio");
            String oraFineStr = request.getParameter("oraFine");
            String prezzoStr = request.getParameter("prezzo");
            String modalitaStr = request.getParameter("modalita");
            
            // Validazioni base
            if (materia == null || materia.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("La materia è obbligatoria", "UTF-8"));
                return;
            }
            
            if (dataStr == null || dataStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("La data è obbligatoria", "UTF-8"));
                return;
            }
            
            if (oraInizioStr == null || oraInizioStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("L'ora di inizio è obbligatoria", "UTF-8"));
                return;
            }
            
            if (oraFineStr == null || oraFineStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("L'ora di fine è obbligatoria", "UTF-8"));
                return;
            }
            
            if (prezzoStr == null || prezzoStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("Il prezzo è obbligatorio", "UTF-8"));
                return;
            }
            
            if (modalitaStr == null || (!"ONLINE".equals(modalitaStr) && !"PRESENZA".equals(modalitaStr))) {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("Modalità non valida", "UTF-8"));
                return;
            }
            
            // 2. Parsing dei dati
            // Data e ora di inizio
            LocalDateTime dataInizio = LocalDateTime.parse(
                dataStr + "T" + oraInizioStr + ":00"
            );
            
            // Data e ora di fine
            LocalDateTime dataFine = LocalDateTime.parse(
                dataStr + "T" + oraFineStr + ":00"
            );
            
            // Verifica che l'ora fine sia dopo l'ora inizio
            if (!dataFine.isAfter(dataInizio)) {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("L'ora di fine deve essere successiva all'ora di inizio", "UTF-8"));
                return;
            }
            
            // Verifica che la lezione non sia nel passato
            if (dataInizio.isBefore(LocalDateTime.now())) {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("Non puoi creare lezioni nel passato", "UTF-8"));
                return;
            }
            
            // Verifica che tutor non ha altra lezione attiva nella stessa fascia oraria
            if (lezioneDAO.hasTutorLezioneInFasciaOraria(tutor.getUID(), dataInizio, dataFine)) {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("Hai già una lezione attiva nella stessa fascia oraria", "UTF-8"));
                return;
            }
            
            // Prezzo
            float prezzo;
            try {
                prezzo = Float.parseFloat(prezzoStr);
                if (prezzo <= 0) {
                    response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                        URLEncoder.encode("Il prezzo deve essere maggiore di 0", "UTF-8"));
                    return;
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("Formato prezzo non valido", "UTF-8"));
                return;
            }
            
            // Calcolo durata in ore
            long minuti = java.time.Duration.between(dataInizio, dataFine).toMinutes();
            float durataOre = minuti / 60.0f;
            
            // Verifica durata minima (es. almeno 30 minuti)
            if (minuti < 30) {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("La lezione deve durare almeno 30 minuti", "UTF-8"));
                return;
            }
            
            // 3. Crea oggetto LezioneDTO (nuovo sistema senza slot)
            LezioneDTO lezione = new LezioneDTO();
            lezione.setMateria(materia.trim());
            lezione.setDataInizio(dataInizio);
            lezione.setDataFine(dataFine);
            lezione.setDurata(durataOre);
            lezione.setPrezzo(prezzo);
            lezione.setCitta(tutor.getCittà());
            
            // Modalità
            if ("ONLINE".equals(modalitaStr)) {
                lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.ONLINE);
            } else {
                lezione.setModalitaLezione(LezioneDTO.ModalitaLezione.PRESENZA);
            }
            
            lezione.setTutor(tutor);
            lezione.setCitta("PRESENZA".equals(modalitaStr) ? 
                (request.getParameter("citta") != null ? request.getParameter("citta").trim() : "") : 
                "Online");
            
            // Stato iniziale: PIANIFICATA
            lezione.setStato(LezioneDTO.StatoLezione.PIANIFICATA);
            
            // 4. Salva la lezione nel database
            boolean success = lezioneDAO.doSaveLezione(lezione);
            
            if (success) {
                response.sendRedirect(request.getContextPath()+ "/storico-lezioni?success=" + 
                    URLEncoder.encode("Lezione creata con successo!", "UTF-8"));
            } else {
                response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                    URLEncoder.encode("Errore durante il salvataggio della lezione", "UTF-8"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                URLEncoder.encode("Errore di database: " + e.getMessage(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath()+ "/nuovaLezione.jsp?error=" + 
                URLEncoder.encode("Errore durante la creazione della lezione: " + e.getMessage(), "UTF-8"));
        }
    }
}