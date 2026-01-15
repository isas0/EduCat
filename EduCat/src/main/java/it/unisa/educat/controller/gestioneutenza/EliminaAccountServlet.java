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

@WebServlet("/eliminaAccount")
public class EliminaAccountServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Mostra pagina di conferma eliminazione
        request.getRequestDispatcher("/confermaEliminazione.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	GestioneUtenzaDAO utenzaDAO = new GestioneUtenzaDAO();
    	
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
        if (utente == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        
        // Verifica password per conferma
        String password = request.getParameter("password");
        try {
        	
            if (hashPassword(password).equals(utente.getPassword())) {
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
                    request.getRequestDispatcher("/confermaEliminazione.jsp").forward(request, response);
                }
            } else {
                request.setAttribute("errorMessage", "Password errata");
                request.getRequestDispatcher("/confermaEliminazione.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Errore: " + e.getMessage());
            request.getRequestDispatcher("/confermaEliminazione.jsp").forward(request, response);
        }
    }
    
    private String hashPassword(String password) {
        // Implementa hashing (es: BCrypt)
        return password; // Sostituire con hashing reale
    }
}
