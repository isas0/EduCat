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
		
		int idLezione = Integer.parseInt(request.getParameter("idLezione"));
		
		try {
			LezioneDTO lezione = lezioneDao.getLezioneById(idLezione);
			request.setAttribute("lezione", lezione);
			
			HttpSession session = request.getSession();
			session.setAttribute("lezioneCheckout", lezione);
			
			request.getRequestDispatcher("/singolaLezione.jsp").forward(request, response);
		} catch (SQLException e) {
			 e.printStackTrace();
	         request.setAttribute("errorMessage", "Errore di database durante la ricerca: " + e.getMessage());
	         
		}
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
