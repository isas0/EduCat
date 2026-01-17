<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.*" %>
<%@ page import="java.util.List" %>
<%
UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
if (utente == null) {
	response.sendRedirect("../login.jsp");
	return;
}


// Verifica che l'utente abbia permessi
if (!"GENITORE".equals(utente.getTipo().toString()) && !"STUDENTE".equals(utente.getTipo().toString())) {
	request.setAttribute("errorMessage", "Accesso negato. \nIdentificati come studente o genitore.");
	session.invalidate();
	request.getRequestDispatcher("/login.jsp").forward(request, response);
	return;
}



%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Home Studente</title>

    <link rel="icon" href="<%= request.getContextPath() %>/images/mini-logo.png" type="image/png">

    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/homeStudenteGenitore.css">
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="hero-section">
        <div class="hero-overlay"></div>
        
        <div class="hero-content">
            <h1 class="hero-title">Trova i migliori insegnanti<br>per lezioni private</h1>
            <p class="hero-subtitle">Prenota la tua lezione online o in presenza in pochi click.</p>

            <form action="<%= request.getContextPath() %>/cerca-lezione" method="GET" class="search-form">
                
                <div class="search-bar-container">
                    
                    <div class="input-group">
                        <i class="fa-solid fa-graduation-cap input-icon"></i>
                        <div class="select-wrapper">
                            <label class="field-label">Materia</label>
                            <select name="materia" class="modern-select">
                                <option value="" disabled selected>Seleziona materia...</option>
                                <option value="Matematica">Matematica</option>
                                <option value="Fisica">Fisica</option>
                                <option value="Inglese">Inglese</option>
                                <option value="Informatica">Informatica</option>
                                <option value="Latino">Latino</option>
                                <option value="Storia">Storia</option>
                            </select>
                        </div>
                    </div>

                    <div class="input-group">
                        <i class="fa-solid fa-location-dot input-icon"></i>
                        <div class="select-wrapper">
                            <label class="field-label">Luogo</label>
                            <select name="modalita" class="modern-select">
                                <option value="Tutti" selected>Online o Presenza</option>
                                <option value="Online">Solo Online</option>
                                <option value="InPresenza">Solo in Presenza</option>
                            </select>
                        </div>
                    </div>
                    
                    <!-- TODO -->

                    <button type="submit" class="btn-search">
                        Cerca <i class="fa-solid fa-arrow-right" style="margin-left: 10px; font-size: 0.9em;"></i>
                    </button>

                </div>
            </form>
        </div>
    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>