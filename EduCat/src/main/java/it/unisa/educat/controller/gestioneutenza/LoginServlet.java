package it.unisa.educat.controller.gestioneutenza;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import it.unisa.educat.dao.GestioneUtenzaDAO;
import it.unisa.educat.model.UtenteDTO;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    
	private static final long serialVersionUID = 1L;
	private GestioneUtenzaDAO utenzaDAO = new GestioneUtenzaDAO();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Mostra la form di login
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        try {
            // Contratto OCL: self.utenti → exists(u| u.email = email and u.password = password and result = u)
            UtenteDTO utente = utenzaDAO.doRetrieveByEmail(email);
            
            if (utente != null && utente.getPassword().equals(hashPassword(password))) {
                // Login riuscito
                HttpSession session = request.getSession();
                session.setAttribute("utente", utente);
                session.setAttribute("userId", utente.getUID());
                
                // Redirect alla dashboard in base al ruolo
                response.sendRedirect("dashboard.jsp");
            } else {
                // Login fallito
                request.setAttribute("errorMessage", "Email o password errati");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Errore durante il login");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
    
    private String hashPassword(String password) {
        // Implementa hashing (es: BCrypt)
        return password; // Sostituire con hashing reale
    }
}
