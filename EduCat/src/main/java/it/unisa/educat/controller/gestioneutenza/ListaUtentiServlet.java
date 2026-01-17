package it.unisa.educat.controller.gestioneutenza;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unisa.educat.model.UtenteDTO;

@WebServlet("/admin/lista-utenti")
public class ListaUtentiServlet extends HttpServlet {
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("../login.jsp");
            return;
        }
        
        // Verifica che l'utente sia amministratore
        UtenteDTO admin = (UtenteDTO) session.getAttribute("utente");
        if (admin == null || !"AMMINISTRATORE_UTENTI".equals(admin.getTipo())) {
            request.setAttribute("errorMessage", "Accesso negato. Solo amministratori possono visualizzare la lista utenti.");
            request.getRequestDispatcher("/accessoNegato.jsp").forward(request, response);
            return;
        }
        
        try {
            // Filtri di ricerca
            String tipoFiltro = request.getParameter("tipo");
            String emailFiltro = request.getParameter("email");
            String nomeFiltro = request.getParameter("nome");
            
            // In una implementazione reale, avresti un metodo nel DAO per filtrare
            List<UtenteDTO> utenti = getListaUtentiConFiltri(tipoFiltro, emailFiltro, nomeFiltro);
            
            request.setAttribute("utenti", utenti);
            request.setAttribute("tipoFiltro", tipoFiltro);
            request.setAttribute("emailFiltro", emailFiltro);
            request.setAttribute("nomeFiltro", nomeFiltro);
            
            request.getRequestDispatcher("/admin/gestioneUtenti.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Errore nel recupero della lista utenti: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
    
    private List<UtenteDTO> getListaUtentiConFiltri(String tipo, String email, String nome) throws Exception {
        // Implementazione semplificata
        // In realtà dovresti avere un metodo nel DAO tipo:
        // List<Utente> utenzaDAO.getUtentiFiltrati(Map<String, String> filtri)
        
        // Per ora restituisci tutti gli utenti
        List<UtenteDTO> tuttiUtenti = new ArrayList<>();
        // Qui dovresti chiamare il DAO per ottenere tutti gli utenti
        
        // Filtraggio manuale (semplificato)
        List<UtenteDTO> filtrati = new ArrayList<>();
        for (UtenteDTO u : tuttiUtenti) {
            boolean match = true;
            
            if (tipo != null && !tipo.isEmpty() && !tipo.equals(u.getTipo())) {
                match = false;
            }
            if (email != null && !email.isEmpty() && !u.getEmail().contains(email)) {
                match = false;
            }
            if (nome != null && !nome.isEmpty() && 
                !(u.getNome().contains(nome) || u.getCognome().contains(nome))) {
                match = false;
            }
            
            if (match) {
                filtrati.add(u);
            }
        }
        
        return filtrati;
    }
}
