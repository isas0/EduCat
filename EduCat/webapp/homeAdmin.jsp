<%@page import="it.unisa.educat.model.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%
UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
if (utente == null) {
	response.sendRedirect("../login.jsp");
	return;
}

//Messaggi di error/success
String errorMessage = null;
String successMessage = null;

//1. PRIMA controlla la SESSIONE (per redirect)
errorMessage = (String) session.getAttribute("errorMessage");
successMessage = (String) session.getAttribute("successMessage");

//Rimuovi dalla sessione dopo averli letti (IMPORTANTE!)
if (errorMessage != null) {
	session.removeAttribute("errorMessage");
}
if (successMessage != null) {
	session.removeAttribute("successMessage");
}

//2. POI controlla la REQUEST (per forward)
if (errorMessage == null && request.getAttribute("errorMessage") != null) {
	errorMessage = (String) request.getAttribute("errorMessage");
}

//3. Infine controlla PARAMETRI URL
if (errorMessage == null && request.getParameter("error") != null) {
	errorMessage = request.getParameter("error");
}
if (successMessage == null && request.getParameter("success") != null) {
	successMessage = request.getParameter("success");
}


// Verifica che l'utente sia admin o abbia permessi
if (!"AMMINISTRATORE_UTENTI".equals(utente.getTipo().toString())) {
	request.setAttribute("errorMessage", "Accesso negato. \nIdentificati come amministratore.");
	session.invalidate();
	request.getRequestDispatcher("/login.jsp").forward(request, response);
	return;
}

List<UtenteDTO> listaUtenti = (List<UtenteDTO>) request.getAttribute("utenti");

List<SegnalazioneDTO> listaSegnalazioni = (List<SegnalazioneDTO>) request.getAttribute("segnalazioni");

%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Admin Dashboard</title>
    
    <link rel="icon" href="<%= request.getContextPath() %>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/homeAdmin.css">
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="admin-container">
        
        <h1 style="color: #1A5C61; margin-bottom: 10px;">Pannello di Controllo</h1>
        <p style="color: #666; margin-bottom: 30px;">Benvenuto, Amministratore. Gestisci utenti e segnalazioni.</p>

        <div class="stats-grid">
            
            <div class="stat-card">
                <div class="stat-info">
                    <h3>Utenti Totali</h3>
                    <p><%= listaUtenti.size() %></p>
                </div>
                <i class="fa-solid fa-users stat-icon"></i>
            </div>
            
            <div class="stat-card">
                <div class="stat-info">
                    <h3>Segnalazioni</h3>
                    <p><%= listaSegnalazioni.size() %></p>
                </div>
                <i class="fa-solid fa-circle-exclamation stat-icon" style="color: #e57373; opacity: 0.3;"></i>
            </div>
            
        </div>


        <h2 class="section-title">Lista Utenti</h2>
        
        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nominativo</th>
                        <th>Email</th>
                        <th>Ruolo</th>
                        <th style="text-align: right;">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <% for(UtenteDTO u : listaUtenti) { %>
                    <tr>
                        <td>#<%= u.getUID() %></td>
                        <td>
                            <strong><%= u.getCognome() %></strong> <%= u.getNome() %>
                        </td>
                        <td><%= u.getEmail() %></td>
                        <td>
                            <span class="role-badge <%= u.getTipo().toString().equals("TUTOR") ? "role-tutor" : "role-student" %>">
                                <%= u.getTipo().toString() %>
                            </span>
                        </td>
                        <td style="text-align: right;">
                            <form action="<%= request.getContextPath() %>/elimina-account" method="POST" style="display:inline;">
                                <input type="hidden" name="idUtente" value="<%= u.getUID() %>">
                                <button type="submit" class="action-btn btn-delete" onclick="return confirm('Sei sicuro di voler eliminare questo utente? Questa azione è irreversibile.');">
                                    <i class="fa-solid fa-trash"></i> Elimina
                                </button>
                            </form>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>


        <h2 class="section-title" style="border-color: #e57373;">Segnalazioni Ricevute</h2>

        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Mittente</th>
                        <th>Segnalato</th>
                        <th>Motivo</th>
                        <th style="text-align: right;">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (listaSegnalazioni.isEmpty()) { %>
                        <tr><td colspan="5" style="text-align:center; padding: 20px;">Nessuna segnalazione presente.</td></tr>
                    <% } else { 
                        for(SegnalazioneDTO s : listaSegnalazioni) { %>
                        <tr>
                            <td><%= s.getIdSegnalazione()%></td>
                            <td><%= s.getSegnalante().getNome()%> <%= s.getSegnalante().getCognome()%></td>
                            <td style="color: #c62828; font-weight: bold;"><%= s.getSegnalato().getNome() %> <%= s.getSegnalato().getCognome()%></td>
                            <td><%= s.getDescrizione() %></td>
                            <td style="text-align: right;">

							<form action="<%=request.getContextPath()%>/risolvi-segnalazione"
								method="POST" style="display: inline;">
								<input type="hidden" name="idSegnalazione"
									value="<%=s.getIdSegnalazione()%>"> 
								
								<button type="submit" class="action-btn btn-view"
								onclick="return confirm('Vuoi segnare la segnalazione come risolta?');">
								<i class="fa-solid fa-check"></i> Risolvi
							</button>
							</form>
							
							<form action="<%=request.getContextPath()%>/elimina-account" method="POST" style="display: inline;">
							<input type="hidden" name="idUtente" value="<%=s.getSegnalato().getUID() %>"> 
							<button type="submit" class="action-btn btn-delete"
								onclick="return confirm('Vuoi bannare l\'utente segnalato?');">
								
								<i class="fa-solid fa-ban"></i> Ban Utente
							</button>
							</form>
						</td>
                        </tr>
                    <%
                    }
                    }
                    %>
                </tbody>
            </table>
        </div>

    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>