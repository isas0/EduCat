<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>

<%
    // 1. Recupero Utente dalla sessione
    UtenteDTO utenteLoggato = (UtenteDTO) session.getAttribute("utente");
    boolean isLogged = (utenteLoggato != null);

    // 2. Default: Se non sei loggato, la Home è la pagina generica di benvenuto
    String linkHome = request.getContextPath() + "/homePageGenerica.jsp";
    String linkPrenotazioni = "#"; // Default vuoto
    
    // 3. Se sei loggato, calcoliamo i percorsi in base al Ruolo
    if (isLogged) {
        String ruolo = utenteLoggato.getTipo().toString();

        if (ruolo.equals("STUDENTE") || ruolo.equals("GENITORE")) {
            // Studente/Genitore: Home Ricerca e Pagina Prenotazioni dedicata
            linkHome = request.getContextPath() + "/homePageStudenteGenitore.jsp";
            linkPrenotazioni = request.getContextPath() + "/prenotazioni.jsp";
        } 
        else if (ruolo.equals("TUTOR")) {
            // Tutor: Dashboard completa (Home e Prenotazioni sono la stessa pagina)
            linkHome = request.getContextPath() + "/homeTutor.jsp";
            linkPrenotazioni = request.getContextPath() + "/homeTutor.jsp";
        } 
        else if (ruolo.equals("AMMINISTRATORE_UTENTI")) {
            // Admin
            linkHome = request.getContextPath() + "/homeAdmin.jsp";
            linkPrenotazioni = "#"; // Admin non ha prenotazioni personali
        }
    }
%>

<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">

<div class="header">
    <div class="container">
        <div class="navbar">
            
            <div class="logo">
                <a href="<%= linkHome %>">
                    <img src="<%= request.getContextPath() %>/images/EduCatLogo.png" alt="EduCat" class="header-logo-img">
                </a>
            </div>

            <% if (isLogged) { %>
                <nav>
                    <ul id="MenuItems">
                        <li><a href="<%= linkHome %>">Home</a></li>
                        
                        <% if (!"AMMINISTRATORE_UTENTI".equals(utenteLoggato.getTipo().toString())) { %>
                            <li><a href="<%= linkPrenotazioni %>">Prenotazioni</a></li>
                        <% } %>
                        
                        <li><a href="<%= request.getContextPath() %>/account.jsp">Account</a></li>
                    </ul>
                </nav>
                
                <img src="<%= request.getContextPath() %>/images/menu.png" class="menu-icon" onclick="menutoggle()">
            <% } %>

        </div>
    </div>
</div>

<% if (isLogged) { %>
<script>
    var menuItems = document.getElementById("MenuItems");
    if(menuItems) {
        menuItems.style.maxHeight = "0px";
    }

    function menutoggle() {
        if (menuItems.style.maxHeight == "0px") {
            menuItems.style.maxHeight = "200px";
        } else {
            menuItems.style.maxHeight = "0px";
        }
    }
</script>
<% } %>