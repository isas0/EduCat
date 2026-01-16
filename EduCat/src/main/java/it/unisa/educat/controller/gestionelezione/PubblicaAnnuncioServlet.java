package it.unisa.educat.controller.gestionelezione;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        UtenteDTO tutor = (UtenteDTO) session.getAttribute("utente");
        if (tutor == null || !"TUTOR".equals(tutor.getTipo().toString())) {
            session.setAttribute("errorMessage", "Solo i tutor possono pubblicare annunci");
            response.sendRedirect("accessoNegato.jsp");
            return;
        }
        
        try {
            // Recupera parametri dal form
            String materia = request.getParameter("materia");
            String modalitaStr = request.getParameter("modalita");
            String citta = request.getParameter("citta");
            String durataStr = request.getParameter("durata");
            String prezzoStr = request.getParameter("prezzo");
            
            // Validazioni di base
            if (materia == null || materia.trim().isEmpty()) {
                throw new IllegalArgumentException("Materia obbligatoria");
            }
            
            if (modalitaStr == null || (!"ONLINE".equals(modalitaStr) && !"PRESENZA".equals(modalitaStr))) {
                throw new IllegalArgumentException("Modalità non valida");
            }
            
            if ("PRESENZA".equals(modalitaStr) && (citta == null || citta.trim().isEmpty())) {
                throw new IllegalArgumentException("Città obbligatoria per lezioni in presenza");
            }
            
            // Conversione numeri
            float durata = 60; // default 60 minuti
            if (durataStr != null && !durataStr.trim().isEmpty()) {
                durata = Float.parseFloat(durataStr);
                if (durata <= 0) {
                    throw new IllegalArgumentException("Durata deve essere positiva");
                }
            }
            
            float prezzo = 0;
            if (prezzoStr != null && !prezzoStr.trim().isEmpty()) {
                prezzo = Float.parseFloat(prezzoStr);
                if (prezzo < 0) {
                    throw new IllegalArgumentException("Prezzo non può essere negativo");
                }
            }
            
            // Crea oggetto LezioneDTO
            LezioneDTO lezione = new LezioneDTO();
            lezione.setMateria(materia.trim());
            lezione.setModalitaLezione("ONLINE".equals(modalitaStr) ? 
                LezioneDTO.ModalitaLezione.ONLINE : LezioneDTO.ModalitaLezione.PRESENZA);
            lezione.setCitta(citta != null ? citta.trim() : null);
            lezione.setDurata(durata);
            lezione.setPrezzo(prezzo);
            lezione.setTutor(tutor);
            
            // La data non è più obbligatoria a livello di lezione
            // Verrà specificata negli slot
            
            // Salva la lezione nel database
            boolean success = lezioneDAO.doSaveLezione(lezione);
            
            if (success) {
                session.setAttribute("successMessage", "Annuncio pubblicato con successo!");
                
                // Reindirizza alla pagina di gestione slot
                response.sendRedirect("gestione-slot.jsp?idLezione=" + lezione.getIdLezione());
            } else {
                session.setAttribute("errorMessage", "Errore durante il salvataggio dell'annuncio");
                response.sendRedirect("pubblicaAnnuncio.jsp?error=salvataggio_fallito");
            }
            
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Formato numerico non valido per durata o prezzo");
            response.sendRedirect("pubblicaAnnuncio.jsp?error=formato_numerico");
        } catch (IllegalArgumentException e) {
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect("pubblicaAnnuncio.jsp?error=parametri_non_validi");
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore di database durante il salvataggio");
            response.sendRedirect("error.jsp");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Errore durante la pubblicazione: " + e.getMessage());
            response.sendRedirect("error.jsp");
        }
    }

}
