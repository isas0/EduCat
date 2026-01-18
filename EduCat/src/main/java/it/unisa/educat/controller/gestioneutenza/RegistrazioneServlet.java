package it.unisa.educat.controller.gestioneutenza;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		request.getRequestDispatcher("/registrazione.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		// Contratto OCL pre: !self.utenti → includes(nuovoUtente)
		// Verifica se email già esiste
		String email = request.getParameter("email");

		String tipoUtente = request.getParameter("tipoUtente");
		try {
			UtenteDTO esistente = utenzaDAO.doRetrieveByEmail(email);
			if (esistente != null) {
				
				request.setAttribute("errorMessage", "Email già registrata");
				if(tipoUtente.equals("STUDENTE")) {
					
					request.getRequestDispatcher("/registrazioneStudente.jsp").forward(request, response);
					
				} else if(tipoUtente.equals("GENITORE")) {
					
					request.getRequestDispatcher("/registrazioneGenitore.jsp").forward(request, response);
					
				} else if(tipoUtente.equals("TUTOR")) {
					
					request.getRequestDispatcher("/registrazioneTutor.jsp").forward(request, response);
					
				}
				return;
			}

			// Crea nuovo utente
			UtenteDTO nuovoUtente = new UtenteDTO();
			nuovoUtente.setNome(request.getParameter("nome"));
			nuovoUtente.setCognome(request.getParameter("cognome"));
			nuovoUtente.setEmail(email);
			nuovoUtente.setPassword(toHash(request.getParameter("password")));
			nuovoUtente.setDataNascita(request.getParameter("dataNascita").toString());
			
			
			if(tipoUtente.equals("STUDENTE")) {
				
				nuovoUtente.setTipo(TipoUtente.STUDENTE);
				
			} else if(tipoUtente.equals("GENITORE")) {
				
				nuovoUtente.setTipo(TipoUtente.GENITORE);
				nuovoUtente.setNomeFiglio(request.getParameter("nomeFiglio"));
				nuovoUtente.setCognomeFiglio(request.getParameter("cognomeFiglio"));
				nuovoUtente.setDataNascitaFiglio(request.getParameter("dataNascitaFiglio"));
				
			} else if(tipoUtente.equals("TUTOR")) {
				
					nuovoUtente.setTipo(TipoUtente.TUTOR);
					request.setAttribute("successMessage", "Registrazione completata!.");
					request.getRequestDispatcher("/nuovaLezione.jsp").forward(request, response);
			}
			
			//nuovoUtente.setDataNascita(LocalDate.parse(request.getParameter("dataNascita")));
			//nuovoUtente.setIndirizzo(request.getParameter("indirizzo"));

			// Contratto OCL post: self.utenti → includes(nuovoUtente) and nuovoUtente.stato = CONFIRMED
			boolean success = utenzaDAO.doSave(nuovoUtente);

			if (success) {
				// Redirect al login con messaggio di successo
				request.setAttribute("successMessage", "Registrazione completata! Effettua il login.");
				request.getRequestDispatcher("/login.jsp").forward(request, response);
			} else {
				request.setAttribute("errorMessage", "Errore durante la registrazione");
				request.getRequestDispatcher("/registrazione.jsp").forward(request, response);
			}

		} catch (Exception e) {
			request.setAttribute("errorMessage", "Errore durante la registrazione");
			e.printStackTrace()	;		//request.getRequestDispatcher("/error.jsp").forward(request, response);
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

}
