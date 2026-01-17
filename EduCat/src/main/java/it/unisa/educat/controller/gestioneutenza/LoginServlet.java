package it.unisa.educat.controller.gestioneutenza;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
            
            if (utente != null && utente.getPassword().equals(toHash(password))) {
                // Login riuscito
                HttpSession session = request.getSession();
                session.setAttribute("utente", utente);
                session.setAttribute("userId", utente.getUID());
                
                // Redirect alla dashboard in base al ruolo
                
                if(utente.getTipo().toString().equals("STUDENTE") || utente.getTipo().toString().equals("GENITORE")) {
                	response.sendRedirect("homePageStudenteGenitore.jsp");
                } else if(utente.getTipo().toString().equals("TUTOR")) {
                	response.sendRedirect("homeTutor.jsp");
                } else if(utente.getTipo().toString().equals("AMMINISTRATORE_UTENTI")) {
                	response.sendRedirect("homeAdmin.jsp");
                }
                
            } else {
                // Login fallito
                request.setAttribute("errorMessage", "Email o password errati");
                request.getRequestDispatcher("/login.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Errore durante il login");
            e.printStackTrace();
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
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
