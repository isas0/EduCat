package it.unisa.educat.controller.gestionelezione;

import it.unisa.educat.dao.GestioneLezioneDAO;
import it.unisa.educat.dao.GestioneLezioneDAO.CriteriRicerca;
import it.unisa.educat.model.LezioneDTO;
import it.unisa.educat.model.UtenteDTO;
import it.unisa.educat.model.LezioneDTO.StatoLezione;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/cerca-lezione")
public class CercaLezioneServlet extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
            GestioneLezioneDAO.CriteriRicerca criteri = new GestioneLezioneDAO.CriteriRicerca();
            
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
            /*String dataDaStr = request.getParameter("dataDa");
            String dataAStr = request.getParameter("dataA");
            
            if (dataDaStr != null && !dataDaStr.trim().isEmpty()) {
                try {
                    // Se la stringa ha solo la data, aggiungi l'ora 00:00
                    if (!dataDaStr.contains("T")) {
                        dataDaStr += "T00:00:00";
                    }
                    LocalDateTime dataDa = LocalDateTime.parse(dataDaStr);
                    criteri.setDataDa(dataDa);
                } catch (DateTimeParseException e) {
                    // Log errore ma continua
                    System.err.println("Formato dataDa non valido: " + dataDaStr);
                }
            }
            
            if (dataAStr != null && !dataAStr.trim().isEmpty()) {
                try {
                    // Se la stringa ha solo la data, aggiungi l'ora 23:59
                    if (!dataAStr.contains("T")) {
                        dataAStr += "T23:59:59";
                    }
                    LocalDateTime dataA = LocalDateTime.parse(dataAStr);
                    criteri.setDataA(dataA);
                } catch (DateTimeParseException e) {
                    // Log errore ma continua
                    System.err.println("Formato dataA non valido: " + dataAStr);
                }
            }
            
            // Filtro per tutor (se serve)
            String idTutorStr = request.getParameter("idTutor");
            if (idTutorStr != null && !idTutorStr.trim().isEmpty()) {
                try {
                    int idTutor = Integer.parseInt(idTutorStr);
                    criteri.setIdTutor(idTutor);
                } catch (NumberFormatException e) {
                    // Ignora
                }
            }*/
            
            // Filtro per prezzo massimo
            String prezzoMaxStr = request.getParameter("prezzoMax");
            if (prezzoMaxStr != null && !prezzoMaxStr.trim().isEmpty()) {
                try {
                    float prezzoMax = Float.parseFloat(prezzoMaxStr);
                    if (prezzoMax > 0) {
                        criteri.setPrezzoMax(prezzoMax);
                    }
                } catch (NumberFormatException e) {
                    // Ignora
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
            
            // Esegui ricerca usando il metodo esistente del DAO
            List<LezioneDTO> lezioni = lezioneDAO.doRetrieveByCriteria(criteri);
            List<LezioneDTO> lezioniPianificate = new ArrayList<LezioneDTO>();
            
            // Per ogni lezione, carica quelle disponibili
            for (LezioneDTO lezione : lezioni) {
            	if(lezione.getStato().equals(StatoLezione.PIANIFICATA)) {
            		lezioniPianificate.add(lezione);
            	}
            }
            
            // Calcola statistiche
            int totaleLezioni = lezioniPianificate.size();
            
            // Prepara dati per la vista
            request.setAttribute("lezioni", lezioniPianificate);
            request.setAttribute("totaleLezioni", totaleLezioni);
            request.setAttribute("criteri", criteri); // Utile per mostrare filtri attivi
            request.setAttribute("paginaCorrente", pagina);
            request.setAttribute("lezioniPerPagina", lezioniPerPagina);
            
            // Mantieni i parametri per il form
            request.setAttribute("materiaParam", materia);
            request.setAttribute("cittaParam", citta);
            request.setAttribute("modalitaParam", modalita);
            //request.setAttribute("dataDaParam", dataDaStr);
            //request.setAttribute("dataAParam", dataAStr);
            request.setAttribute("prezzoMaxParam", prezzoMaxStr);
            
            // Forward al risultato
            request.getRequestDispatcher("/listaLezioni.jsp").forward(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Errore di database durante la ricerca: " + e.getMessage());
            request.getRequestDispatcher("/listaLezioni.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Errore durante la ricerca: " + e.getMessage());
            request.getRequestDispatcher("/listaLezioni.jsp").forward(request, response);
        }
    }
}