<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>

<%
    // Recupero Utente
    UtenteDTO utenteLoggato = (UtenteDTO) session.getAttribute("utente");
    
    // Link Home Default
    String homeLink = request.getContextPath() + "/homePageGenerica.jsp"; 
    
    // Calcolo Home Specifica
    if (utenteLoggato != null && utenteLoggato.getTipo() != null) {
        switch(utenteLoggato.getTipo().toString()) {
            case "STUDENTE": homeLink = request.getContextPath() + "/homePageStudenteGenitore.jsp"; break;
            case "GENITORE": homeLink = request.getContextPath() + "/homePageStudenteGenitore.jsp"; break;
            case "TUTOR":    homeLink = request.getContextPath() + "/homeTutor.jsp"; break;
            case "AMMINISTRATORE_UTENTI": homeLink = request.getContextPath() + "/homeAdmin.jsp"; break;
        }
    }
%>

<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">

<div class="header">
    <div class="container">
        <div class="navbar">
            
            <div class="logo">
                <a href="<%= homeLink %>">
                    <img src="<%= request.getContextPath() %>/images/EduCatLogo.png" alt="EduCat" class="header-logo-img">
                </a>
            </div>

            <% if (utenteLoggato != null) { %>
                <nav>
                    <ul id="MenuItems">
                        <li><a href="<%= homeLink %>">Home</a></li>
                        <li><a href="<%= request.getContextPath() %>/prenotazioni.jsp">Prenotazioni</a></li>
                        <li><a href="<%= request.getContextPath() %>/account.jsp">Account</a></li>
                    </ul>
                </nav>
                
                <img src="<%= request.getContextPath() %>/images/menu.png" class="menu-icon" onclick="menutoggle()">
            <% } %>

        </div>
    </div>
</div>

<% if (utenteLoggato != null) { %>
<script>
    var menuItems = document.querySelector("nav ul");
    menuItems.style.maxHeight = "0px";
    function menutoggle() {
        if (menuItems.style.maxHeight == "0px") {
            menuItems.style.maxHeight = "200px";
        } else {
            menuItems.style.maxHeight = "0px";
        }
    }
</script>
<% } %>