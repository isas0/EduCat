package it.unisa.educat.controller.gestioneutenza;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import it.unisa.educat.dao.GestioneUtenzaDAO;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.UtenteDTO.TipoUtente;

@WebServlet("/registrazione")
public class RegistrazioneServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private GestioneUtenzaDAO utenzaDAO = new GestioneUtenzaDAO();

	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		request.getRequestDispatcher("/registrazione.jsp").forward(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
	        throws ServletException, IOException {

	    String tipoUtente = request.getParameter("tipoUtente");
	    String email = request.getParameter("email");
	    
	    try {
	        String erroreValidazione = validaDatiUtente(request, tipoUtente);
	        if (erroreValidazione != null) {
	            forwardToRegistrationPage(request, response, tipoUtente, erroreValidazione);
	            return;
	        }
	        
	        String dataNascita = request.getParameter("dataNascita");
	        String erroreEtaStudente = validaEtaStudente(dataNascita, tipoUtente);
	        if (erroreEtaStudente != null) {
	            forwardToRegistrationPage(request, response, tipoUtente, erroreEtaStudente);
	            return;
	        }
	        
	        if ("GENITORE".equals(tipoUtente)) {
	            String dataNascitaFiglio = request.getParameter("dataNascitaFiglio");
	            String erroreGenitoreFiglio = validaEtaGenitoreFiglio(
	                dataNascita, dataNascitaFiglio, tipoUtente);
	            if (erroreGenitoreFiglio != null) {
	                forwardToRegistrationPage(request, response, tipoUtente, erroreGenitoreFiglio);
	                return;
	            }
	        }
	        UtenteDTO esistente = utenzaDAO.doRetrieveByEmail(email);
	        if (esistente != null) {
	            forwardToRegistrationPage(request, response, tipoUtente, "Email già registrata");
	            return;
	        }

	        UtenteDTO nuovoUtente = new UtenteDTO();
	        nuovoUtente.setNome(request.getParameter("nome"));
	        nuovoUtente.setCognome(request.getParameter("cognome"));
	        nuovoUtente.setEmail(email);
	        nuovoUtente.setPassword(toHash(request.getParameter("password")));
	        nuovoUtente.setDataNascita(dataNascita);
	        nuovoUtente.setCittà(request.getParameter("città"));
	        nuovoUtente.setCivico(request.getParameter("civico"));
	        nuovoUtente.setCAP(request.getParameter("CAP"));
	        nuovoUtente.setVia(request.getParameter("via"));
	        
	        HttpSession session = request.getSession();
	        session.setAttribute("userId", nuovoUtente.getUID());
	        
	        if(tipoUtente.equals("STUDENTE")) {
	            nuovoUtente.setTipo(TipoUtente.STUDENTE);
	        } else if(tipoUtente.equals("GENITORE")) {
	            nuovoUtente.setTipo(TipoUtente.GENITORE);
	            nuovoUtente.setNomeFiglio(request.getParameter("nomeFiglio"));
	            nuovoUtente.setCognomeFiglio(request.getParameter("cognomeFiglio"));
	            nuovoUtente.setDataNascitaFiglio(request.getParameter("dataNascitaFiglio"));
	        } else if(tipoUtente.equals("TUTOR")) {
	            nuovoUtente.setTipo(TipoUtente.TUTOR);
	        }
	        
	        session.setAttribute("utente", nuovoUtente);
	        
	        boolean success = utenzaDAO.doSave(nuovoUtente);
	        
	        if (!success) {
	            forwardToRegistrationPage(request, response, tipoUtente, 
	                "Errore durante la registrazione");
	            return;
	        }
	        
	        if(tipoUtente.equals("STUDENTE") || tipoUtente.equals("GENITORE")) {
	            request.getRequestDispatcher("/homePageStudenteGenitore.jsp").forward(request, response);
	        } else if(tipoUtente.equals("TUTOR")) {
	            request.getRequestDispatcher("/nuovaLezione.jsp").forward(request, response);
	        }
	        
	    } catch (Exception e) {
	        forwardToRegistrationPage(request, response, tipoUtente, 
	            "Errore durante la registrazione: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	private void forwardToRegistrationPage(HttpServletRequest request, 
	                                      HttpServletResponse response,
	                                      String tipoUtente,
	                                      String errorMessage) 
	                                      throws ServletException, IOException {
	    request.setAttribute("errorMessage", errorMessage);
	    
	    if ("STUDENTE".equals(tipoUtente)) {
	        request.getRequestDispatcher("/registrazioneStudente.jsp").forward(request, response);
	    } else if ("GENITORE".equals(tipoUtente)) {
	        request.getRequestDispatcher("/registrazioneGenitore.jsp").forward(request, response);
	    } else if ("TUTOR".equals(tipoUtente)) {
	        request.getRequestDispatcher("/registrazioneTutor.jsp").forward(request, response);
	    } else {
	        request.getRequestDispatcher("/registrazione.jsp").forward(request, response);
	    }
	}

	//hashing password
	private String toHash(String password) {
		String hashString = null;
		try {
			java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-512");
			byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			hashString = "";
			for (int i = 0; i < hash.length; i++) {
				hashString += Integer.toHexString((hash[i] & 0xFF) | 0x100).substring(1, 3);
			}
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println(e);
		}
		return hashString;
	}
	
	private String validaEtaStudente(String dataNascitaStr, String tipoUtente) {
	    if (!"STUDENTE".equals(tipoUtente)) {
	        return null; 
	    }
	    
	    if (dataNascitaStr == null || dataNascitaStr.trim().isEmpty()) {
	        return "Data di nascita obbligatoria per gli studenti";
	    }
	    
	    try {
	        LocalDate dataNascita = LocalDate.parse(dataNascitaStr);
	        LocalDate oggi = LocalDate.now();
	        
	        int eta = oggi.getYear() - dataNascita.getYear();
	        if (dataNascita.plusYears(eta).isAfter(oggi)) {
	            eta--;
	        }
	        
	        if (eta < 18) {
	            return "Lo studente deve essere maggiorenne (almeno 18 anni)";
	        }
	        
	    } catch (Exception e) {
	        return "Formato data di nascita non valido (usa YYYY-MM-DD)";
	    }
	    
	    return null; // Valido
	}

	/**
	 * Valida che il genitore sia più vecchio del figlio
	 */
	private String validaEtaGenitoreFiglio(String dataNascitaGenitoreStr, 
	                                      String dataNascitaFiglioStr, 
	                                      String tipoUtente) {
	    if (!"GENITORE".equals(tipoUtente)) {
	        return null; 
	    }
	    
	    if (dataNascitaGenitoreStr == null || dataNascitaGenitoreStr.trim().isEmpty() ||
	        dataNascitaFiglioStr == null || dataNascitaFiglioStr.trim().isEmpty()) {
	        return "Date di nascita genitore e figlio obbligatorie";
	    }
	    
	    try {
	        LocalDate dataNascitaGenitore = LocalDate.parse(dataNascitaGenitoreStr);
	        LocalDate dataNascitaFiglio = LocalDate.parse(dataNascitaFiglioStr);
	        
	        if (!dataNascitaGenitore.isBefore(dataNascitaFiglio)) {
	            return "Il genitore deve essere nato prima del figlio";
	        }
	        
	        LocalDate oggi = LocalDate.now();
	        long etaFiglio = java.time.temporal.ChronoUnit.YEARS.between(
	            dataNascitaFiglio, oggi);
	        
	        if (etaFiglio >= 18) {
	            return "Il figlio deve essere minorenne (meno di 18 anni)";
	        }
	        
	    } catch (Exception e) {
	        return "Formato date di nascita non valido (usa YYYY-MM-DD)";
	    }
	    
	    return null; // Valido
	}

	/**
	 * Validazione generale dati utente
	 */
	private String validaDatiUtente(HttpServletRequest request, String tipoUtente) {
	    String email = request.getParameter("email");
	    if (email == null || email.trim().isEmpty()) {
	        return "Email obbligatoria";
	    }
	    
	    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
	        return "Formato email non valido";
	    }
	    
	    String password = request.getParameter("password");
	    if (password == null || password.trim().isEmpty()) {
	        return "Password obbligatoria";
	    }
	    
	    if (password.length() < 8) {
	        return "La password deve contenere almeno 8 caratteri";
	    }
	    
	    String nome = request.getParameter("nome");
	    String cognome = request.getParameter("cognome");
	    
	    if (nome == null || nome.trim().isEmpty() || 
	        cognome == null || cognome.trim().isEmpty()) {
	        return "Nome e cognome obbligatori";
	    }
	    
	    if (!nome.matches("^[A-Za-zÀ-ÿ\\s'-]+$") || 
	        !cognome.matches("^[A-Za-zÀ-ÿ\\s'-]+$")) {
	        return "Nome e cognome possono contenere solo lettere, spazi, apostrofi e trattini";
	    }
	    
	    String via = request.getParameter("via");
	    String citta = request.getParameter("città");
	    String cap = request.getParameter("CAP");
	    
	    if (via == null || via.trim().isEmpty() ||
	        citta == null || citta.trim().isEmpty() ||
	        cap == null || cap.trim().isEmpty()) {
	        return "Indirizzo completo obbligatorio (via, città, CAP)";
	    }
	    
	    if (!cap.matches("\\d{5}")) {
	        return "CAP non valido (5 cifre)";
	    }
	    
	    if ("GENITORE".equals(tipoUtente)) {
	        String nomeFiglio = request.getParameter("nomeFiglio");
	        String cognomeFiglio = request.getParameter("cognomeFiglio");
	        
	        if (nomeFiglio == null || nomeFiglio.trim().isEmpty() ||
	            cognomeFiglio == null || cognomeFiglio.trim().isEmpty()) {
	            return "Nome e cognome figlio obbligatori per genitori";
	        }
	        
	        if (!nomeFiglio.matches("^[A-Za-zÀ-ÿ\\s'-]+$") || 
	            !cognomeFiglio.matches("^[A-Za-zÀ-ÿ\\s'-]+$")) {
	            return "Nome e cognome figlio possono contenere solo lettere";
	        }
	    }
	    
	    return null; // Tutto valido
	}

}
