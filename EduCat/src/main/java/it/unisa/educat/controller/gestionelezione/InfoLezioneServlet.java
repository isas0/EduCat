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

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.model.LezioneDTO;

/**
 * Servlet implementation class InfoLezioneServlet
 */
@WebServlet("/info-lezione")
public class InfoLezioneServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    GestioneLezioneDAO lezioneDao = null;
    
    public void init() {
        lezioneDao = new GestioneLezioneDAO();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			String idLezioneStr = request.getParameter("idLezione");
			if (idLezioneStr == null || idLezioneStr.trim().isEmpty()) {
				response.sendRedirect(request.getContextPath() + "/cerca-lezione?error=" + 
					URLEncoder.encode("ID lezione non specificato", "UTF-8"));
				return;
			}
			
			int idLezione = Integer.parseInt(idLezioneStr);
			LezioneDTO lezione = lezioneDao.getLezioneById(idLezione);
			
			if (lezione == null) {
				response.sendRedirect(request.getContextPath() + "/cerca-lezione?error=" + 
					URLEncoder.encode("Lezione non trovata", "UTF-8"));
				return;
			}
			
			request.setAttribute("lezione", lezione);
			
			HttpSession session = request.getSession();
			session.setAttribute("lezioneCheckout", lezione);
			
			request.getRequestDispatcher("/singolaLezione.jsp").forward(request, response);
		} catch (NumberFormatException e) {
			response.sendRedirect(request.getContextPath() + "/cerca-lezione?error=" + 
				URLEncoder.encode("ID lezione non valido", "UTF-8"));
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/cerca-lezione?error=" + 
				URLEncoder.encode("Errore di database: " + e.getMessage(), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
			response.sendRedirect(request.getContextPath() + "/cerca-lezione?error=" + 
				URLEncoder.encode("Errore: " + e.getMessage(), "UTF-8"));
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}

