<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>

<%
    UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
    if (utente == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    Boolean isMultiRuolo = (Boolean) session.getAttribute("multiRuolo");
    if (isMultiRuolo == null) isMultiRuolo = false;

    String iniziali = "";
    if (utente.getNome() != null) iniziali += utente.getNome().charAt(0);
    if (utente.getCognome() != null) iniziali += utente.getCognome().charAt(0);
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>EduCat - Profilo</title>
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/account.css">
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="account-container">
        <div class="profile-card">
            
            <div class="profile-avatar"><%= iniziali.toUpperCase() %></div>
            <h1 class="profile-name"><%= utente.getNome() %> <%= utente.getCognome() %></h1>
            <p class="profile-email"><%= utente.getEmail() %></p>
            
            <span class="role-badge"><%= utente.getTipo() %></span>

            <hr style="border: 0; border-top: 1px solid #eee; margin: 25px 0;">

            <div class="actions-grid">
                
                <% if (isMultiRuolo) { %>
                <a href="<%= request.getContextPath() %>/sceltaRuolo.jsp" class="action-btn btn-change-role">
                    <i class="fa-solid fa-users-gear"></i> Cambia Ruolo
                </a>
                <% } %>

                <a href="<%= request.getContextPath() %>/logout" class="action-btn btn-logout">
                    <i class="fa-solid fa-right-from-bracket"></i> Esci
                </a>

                <form action="<%= request.getContextPath() %>/elimina-account" method="POST" id="deleteForm">
                    <input type="hidden" name="idUtente" value="<%= utente.getUID() %>">
                    <button type="button" class="action-btn btn-delete" onclick="confermaEliminazione()">
                        <i class="fa-solid fa-trash-can"></i> Elimina Account
                    </button>
                </form>

            </div>
        </div>
    </div>

    <script>
        function confermaEliminazione() {
            if (confirm("Sei sicuro di voler eliminare l'account?")) {
                document.getElementById("deleteForm").submit();
            }
        }
    </script>

    <jsp:include page="footer.jsp" />
</body>
</html>