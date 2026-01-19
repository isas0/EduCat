<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="it.unisa.educat.model.*, it.unisa.educat.dao.GestioneLezioneDAO" %>
<%@ page import="it.unisa.educat.model.PrenotazioneDTO.StatoPrenotazione" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    // Protezione Login
    UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
    if (utente == null) { response.sendRedirect("login.jsp"); return; }

    
    
    
    //Messaggi di error/success
    String errorMessage = null;
    String successMessage = null;
    
 	// 1. PRIMA controlla la SESSIONE (per redirect)
    errorMessage = (String) session.getAttribute("errorMessage");
    successMessage = (String) session.getAttribute("successMessage");
    
    // Rimuovi dalla sessione dopo averli letti (IMPORTANTE!)
    if (errorMessage != null) {
        session.removeAttribute("errorMessage");
    }
    if (successMessage != null) {
        session.removeAttribute("successMessage");
    }
    
    // 2. POI controlla la REQUEST (per forward)
    if (errorMessage == null && request.getAttribute("errorMessage") != null) {
        errorMessage = (String) request.getAttribute("errorMessage");
    }
    
    // 3. Infine controlla PARAMETRI URL
    if (errorMessage == null && request.getParameter("error") != null) {
        errorMessage = request.getParameter("error");
    }
    if (successMessage == null && request.getParameter("success") != null) {
        successMessage = request.getParameter("success");
    }
    
    
    
    
    
    
 	// Verifica Permessi
    if (!"GENITORE".equals(utente.getTipo().toString()) && !"STUDENTE".equals(utente.getTipo().toString())) {
    	session.invalidate();
    	response.sendRedirect("login.jsp");
    	return;
    }
    
    // Recupero Dati
    List<PrenotazioneDTO> miePrenotazioni = (List<PrenotazioneDTO>) request.getAttribute("prenotazioni");
    
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Le mie Prenotazioni</title>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/modalSegnalazioni.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/prenotazioni.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="prenotazioni-container">
        <h1 class="page-title">Le mie Prenotazioni</h1>
        
        <%if(errorMessage!=null){ %>
        <h2 class="page-title"><%=errorMessage %></h2>
        <%} %>
        
        
        <div class="table-wrapper">
            <% if (miePrenotazioni == null) { %>
                <div class="empty-state">
                    <p>Non hai ancora effettuato prenotazioni.</p>
                </div>
            <% } else { %>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Data</th><th>Materia</th><th>Tutor</th><th>Stato</th><th style="text-align: right;">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        for(PrenotazioneDTO p : miePrenotazioni) { 
                           // Null check per sicurezza
                           String nomeTutor = "N/D";
                           int idTutor = 0;
                           if(p.getLezione() != null && p.getLezione().getTutor() != null) {
                               nomeTutor = p.getLezione().getTutor().getNome() + " " + p.getLezione().getTutor().getCognome();
                               idTutor = p.getLezione().getTutor().getUID();
                           }
                        %>
                        <tr>
                            <td><%= p.getLezione().getDataInizio().format(formatter) %></td>
                            <td><strong><%= p.getLezione() != null ? p.getLezione().getMateria() : "-" %></strong></td>
                            <td><%= nomeTutor %></td>
                            <td><span class="status-pill"><%= p.getStato() %></span></td>
                            <td style="text-align: right;">
                            
                            	<%if(p.getStato().equals(StatoPrenotazione.ATTIVA)){ %>
								<form action="annulla-prenotazione" method="post" style="display: inline;">
								<input type="hidden" name="idPrenotazione" value="<%=p.getIdPrenotazione() %>"> 
								
								<button type="submit" class="action-btn btn-view"	>									
									<i class="fa-solid fa-circle-exclamation"></i> Annulla
								</button>
								
								</form>
								<%} %>
                            
                                <button class="btn-report" onclick="apriSegnalazione(<%= idTutor %>, '<%= nomeTutor %>')">
                                    <i class="fa-solid fa-circle-exclamation"></i> Segnala
                                </button>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>
    </div>

    <jsp:include page="modalSegnalazione.jsp" />
    
    <jsp:include page="footer.jsp" />
</body>
</html>