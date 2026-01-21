package it.unisa.educat.controller.gestioneutenza;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.dao.GestioneUtenzaDAO;
import it.unisa.educat.model.UtenteDTO;

@WebServlet("/elimina-account")
public class EliminaAccountServlet extends HttpServlet {

	private GestioneUtenzaDAO utenzaDAO;
	
	@Override
    public void init() throws ServletException {
		utenzaDAO = new GestioneUtenzaDAO();
    }
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		doPost(request,response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		if (session == null) {
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}

		UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
		if (utente == null) {
			response.sendRedirect(request.getContextPath()+"/login.jsp");
			return;
		}

		if ("AMMINISTRATORE_UTENTI".equals(utente.getTipo().toString())) {
			//Id utente da eliminare
			int id = Integer.parseInt(request.getParameter("idUtente"));
			try {
				utenzaDAO.doDelete(id);
				request.setAttribute("successMessage", "Account eliminato con successo");
				request.getRequestDispatcher("/lista-segnalazioni").forward(request, response);
			} catch (SQLException e) {
				e.printStackTrace();
				request.setAttribute("errorMessage", "Errore durante l'eliminazione dell'account");
				request.getRequestDispatcher("/lista-segnalazioni").forward(request, response);
			}

		}

		else {

			try {
				// Elimina l'account
				boolean eliminato = utenzaDAO.doDelete(utente.getUID());

				if (eliminato) {
					// Logout e invalidazione sessione
					session.invalidate();

					// Redirect con messaggio di successo
					request.setAttribute("successMessage", "Account eliminato con successo");
					request.getRequestDispatcher("/login.jsp").forward(request, response);
					System.out.println("Account eliminato");
				} else {
					request.setAttribute("errorMessage", "Errore durante l'eliminazione dell'account");
					request.getRequestDispatcher("/account.jsp").forward(request, response);
				}


			} catch (Exception e) {
				e.printStackTrace();
				request.setAttribute("errorMessage", "Errore: " + e.getMessage());
				request.getRequestDispatcher("/account.jsp").forward(request, response);
			}
		}
	}

}
