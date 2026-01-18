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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
		HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        UtenteDTO tutor = (UtenteDTO) session.getAttribute("utente");
        if (tutor == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        // Verifica che l'utente sia TUTOR
        if (!"TUTOR".equals(tutor.getTipo().toString())) {
            session.setAttribute("errorMessage", "Solo i tutor possono creare lezioni");
            response.sendRedirect("accessoNegato.jsp");
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
                session.setAttribute("errorMessage", "La materia è obbligatoria");
                response.sendRedirect("nuovaLezione.jsp?error=materia_obbligatoria");
                return;
            }
            
            if (dataStr == null || dataStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "La data è obbligatoria");
                response.sendRedirect("nuovaLezione.jsp?error=data_obbligatoria");
                return;
            }
            
            if (oraInizioStr == null || oraInizioStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "L'ora di inizio è obbligatoria");
                response.sendRedirect("nuovaLezione.jsp?error=ora_inizio_obbligatoria");
                return;
            }
            
            if (oraFineStr == null || oraFineStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "L'ora di fine è obbligatoria");
                response.sendRedirect("nuovaLezione.jsp?error=ora_fine_obbligatoria");
                return;
            }
            
            if (prezzoStr == null || prezzoStr.trim().isEmpty()) {
                session.setAttribute("errorMessage", "Il prezzo è obbligatorio");
                response.sendRedirect("nuovaLezione.jsp?error=prezzo_obbligatorio");
                return;
            }
            
            if (modalitaStr == null || (!"ONLINE".equals(modalitaStr) && !"PRESENZA".equals(modalitaStr))) {
                session.setAttribute("errorMessage", "Modalità non valida");
                response.sendRedirect("nuovaLezione.jsp?error=modalita_non_valida");
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
                session.setAttribute("errorMessage", "L'ora di fine deve essere successiva all'ora di inizio");
                response.sendRedirect("nuovaLezione.jsp?error=orario_non_valido");
                return;
            }
            
            // Verifica che la lezione non sia nel passato
            if (dataInizio.isBefore(LocalDateTime.now())) {
                session.setAttribute("errorMessage", "Non puoi creare lezioni nel passato");
                response.sendRedirect("nuovaLezione.jsp?error=lezione_passato");
                return;
            }
            
            // Prezzo
            float prezzo;
            try {
                prezzo = Float.parseFloat(prezzoStr);
                if (prezzo <= 0) {
                    session.setAttribute("errorMessage", "Il prezzo deve essere maggiore di 0");
                    response.sendRedirect("nuovaLezione.jsp?error=prezzo_non_valido");
                    return;
                }
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "Formato prezzo non valido");
                response.sendRedirect("nuovaLezione.jsp?error=formato_prezzo_non_valido");
                return;
            }
            
            // Calcolo durata in ore
            long minuti = java.time.Duration.between(dataInizio, dataFine).toMinutes();
            float durataOre = minuti / 60.0f;
            
            // Verifica durata minima (es. almeno 30 minuti)
            if (minuti < 30) {
                session.setAttribute("errorMessage", "La lezione deve durare almeno 30 minuti");
                response.sendRedirect("nuovaLezione.jsp?error=durata_minima");
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
                session.setAttribute("successMessage", 
                    "Lezione creata con successo!<br>" +
                    "Materia: " + lezione.getMateria() + "<br>" +
                    "Data: " + dataInizio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "<br>" +
                    "Durata: " + String.format("%.1f", durataOre) + " ore<br>" +
                    "Prezzo: €" + String.format("%.2f", prezzo)
                );
                
                // Reindirizza alla pagina delle lezioni del tutor
                response.sendRedirect("storico-lezioni");
            } else {
                session.setAttribute("errorMessage", "Errore durante il salvataggio della lezione");
                response.sendRedirect("nuovaLezione.jsp?error=salvataggio_fallito");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database: " + e.getMessage());
            response.sendRedirect("nuovaLezione.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore durante la creazione della lezione: " + e.getMessage());
            response.sendRedirect("nuovaLezione.jsp");
        }
    }
}
