package it.unisa.educat.controller.gestionelezione;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.dao.GestioneLezioneDAO.CriteriRicerca;
import it.unisa.educat.model.LezioneDTO;
import it.unisa.educat.model.UtenteDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/cerca-lezione")
public class CercaLezioneServlet extends HttpServlet {
    
    private GestioneLezioneDAO lezioneDAO;
    
    @Override
    public void init() throws ServletException {
        lezioneDAO = new GestioneLezioneDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Mostra form di ricerca
        request.getRequestDispatcher("/cercaLezione.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Crea criteri di ricerca dai parametri della richiesta
            CriteriRicerca criteri = new CriteriRicerca();
            
            // Filtri di base
            String materia = request.getParameter("materia");
            String citta = request.getParameter("citta");
            String modalita = request.getParameter("modalita");
            
            if (materia != null && !materia.trim().isEmpty()) {
                criteri.setMateria(materia.trim());
            }
            
            if (citta != null && !citta.trim().isEmpty()) {
                criteri.setCitta(citta.trim());
            }
            
            if (modalita != null && !modalita.trim().isEmpty()) {
                criteri.setModalita(modalita.trim());
            }
            
            // Filtri per data
            String dataDaStr = request.getParameter("dataDa");
            String dataAStr = request.getParameter("dataA");
            
            if (dataDaStr != null && !dataDaStr.trim().isEmpty()) {
                try {
                    LocalDateTime dataDa = LocalDateTime.parse(dataDaStr + "T00:00:00");
                    criteri.setDataDa(dataDa);
                } catch (DateTimeParseException e) {
                    // Ignora se formato non valido
                }
            }
            
            if (dataAStr != null && !dataAStr.trim().isEmpty()) {
                try {
                    LocalDateTime dataA = LocalDateTime.parse(dataAStr + "T23:59:59");
                    criteri.setDataA(dataA);
                } catch (DateTimeParseException e) {
                    // Ignora se formato non valido
                }
            }
            
            // Filtro per prezzo massimo
            String prezzoMaxStr = request.getParameter("prezzoMax");
            if (prezzoMaxStr != null && !prezzoMaxStr.trim().isEmpty()) {
                try {
                    float prezzoMax = Float.parseFloat(prezzoMaxStr);
                    if (prezzoMax > 0) {
                        criteri.setPrezzoMax(prezzoMax);
                    }
                } catch (NumberFormatException e) {
                    // Ignora se non è un numero valido
                }
            }
            
            // Paginazione
            String paginaStr = request.getParameter("pagina");
            int pagina = 1;
            int lezioniPerPagina = 10;
            
            try {
                pagina = Integer.parseInt(paginaStr);
                if (pagina < 1) pagina = 1;
            } catch (NumberFormatException e) {
                pagina = 1;
            }
            
            int offset = (pagina - 1) * lezioniPerPagina;
            criteri.setLimit(lezioniPerPagina);
            criteri.setOffset(offset);
            
            // Esegui ricerca
            List<LezioneDTO> lezioni = lezioneDAO.doRetrieveByCriteria(criteri);
            
            // Prepara dati per la vista
            request.setAttribute("lezioni", lezioni);
            request.setAttribute("materia", materia);
            request.setAttribute("citta", citta);
            request.setAttribute("modalita", modalita);
            request.setAttribute("dataDa", dataDaStr);
            request.setAttribute("dataA", dataAStr);
            request.setAttribute("prezzoMax", prezzoMaxStr);
            request.setAttribute("paginaCorrente", pagina);
            
            // Forward al risultato
            request.getRequestDispatcher("/risultatiRicerca.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Errore durante la ricerca: " + e.getMessage());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }
    }
}
