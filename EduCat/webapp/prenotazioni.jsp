<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="it.unisa.educat.model.*, it.unisa.educat.dao.GestioneLezioneDAO" %>
<%@ page import="it.unisa.educat.model.PrenotazioneDTO.StatoPrenotazione" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    // Protezione Login
    UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
    if (utente == null) { 
        response.sendRedirect(request.getContextPath()+ "/login.jsp");
        return; 
    }

    //Messaggi di error/success
    String errorMessage = null;
    String successMessage = null;
    
    errorMessage = (String) session.getAttribute("errorMessage");
    successMessage = (String) session.getAttribute("successMessage");
    
    if (errorMessage != null) session.removeAttribute("errorMessage");
    if (successMessage != null) session.removeAttribute("successMessage");
    
    if (errorMessage == null && request.getAttribute("errorMessage") != null) {
        errorMessage = (String) request.getAttribute("errorMessage");
    }
    
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
    
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/homeStudenteGenitore.css">
    
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
            <h2 class="page-title" style="color: #c62828;"><%=errorMessage %></h2>
        <%} %>
        <%if(successMessage!=null){ %>
            <h2 class="page-title" style="color: #2e7d32;"><%=successMessage %></h2>
        <%} %>
        
        <div class="table-wrapper">
            <% if (miePrenotazioni == null || miePrenotazioni.isEmpty()) { %>
                <div class="empty-state">
                    <p>Non hai ancora effettuato prenotazioni.</p>
                </div>
            <% } else { %>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Data</th>
                            <th>Materia</th>
                            <th>Tutor</th>
                            <th>Stato</th>
                            <th style="text-align: right;">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        for(PrenotazioneDTO p : miePrenotazioni) { 
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
                                    <button type="button" class="action-btn btn-delete" 
                                            onclick="apriModalAnnulla(<%=p.getIdPrenotazione() %>)">									
    									<i class="fa-solid fa-circle-exclamation"></i> Annulla
    								</button>
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
    
    <div id="modalAnnulla" class="modal-overlay-confirm">
        <div class="modal-box">
            <i class="fa-solid fa-triangle-exclamation modal-icon-warning"></i>
            <h3 style="margin-top:0; color:#333;">Vuoi annullare la prenotazione?</h3>
            <p style="color:#666; margin-bottom: 20px;">
                L'operazione è definitiva. <br>
                Ti verrà rimborsato l'importo pagato.
            </p>
            
            <form action="annulla-prenotazione" method="POST">
                <input type="hidden" name="idPrenotazione" id="idPrenotazioneInput" value="">
                
                <div class="modal-buttons">
                    <button type="button" class="btn-modal-cancel" onclick="chiudiModalAnnulla()">Non annullare</button>
                    <button type="submit" class="btn-modal-confirm">Sì, Conferma</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function apriModalAnnulla(idPrenotazione) {
            document.getElementById('idPrenotazioneInput').value = idPrenotazione;
            document.getElementById('modalAnnulla').style.display = 'flex';
        }

        function chiudiModalAnnulla() {
            document.getElementById('modalAnnulla').style.display = 'none';
        }
        
        // Chiudi se clicchi fuori dalla box
        window.onclick = function(event) {
            var modal = document.getElementById('modalAnnulla');
            if (event.target == modal) {
                modal.style.display = "none";
            }
        }
    </script>
</body>
</html>