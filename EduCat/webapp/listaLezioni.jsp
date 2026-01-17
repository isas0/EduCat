<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="it.unisa.educat.model.LezioneDTO" %>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>
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

// Recupero parametri per mantenere la selezione dopo la ricerca
String searchMateria = request.getParameter("materia");
String searchCitta = request.getParameter("citta");
String searchModalita = request.getParameter("modalita");
String searchPrezzo = request.getParameter("prezzoMax");

if (searchMateria == null)
	searchMateria = "";
if (searchCitta == null)
	searchCitta = "";
if (searchModalita == null)
	searchModalita = "";
if (searchPrezzo == null)
	searchPrezzo = "";
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>EduCat - Cerca Lezioni</title>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/listaLezioni.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <section class="filter-section">
        <div class="filter-container">
            <form action="<%= request.getContextPath() %>/cerca-lezione" method="GET" class="filter-form">
                
                <div class="filter-group">
                    <label class="filter-label"><i class="fa-solid fa-book"></i> Materia</label>
                    <select name="materia" class="filter-input">
                        <option value="">Tutte le materie</option>
                        <option value="Matematica" <%= "Matematica".equals(searchMateria) ? "selected" : "" %>>Matematica</option>
                        <option value="Fisica" <%= "Fisica".equals(searchMateria) ? "selected" : "" %>>Fisica</option>
                        <option value="Inglese" <%= "Inglese".equals(searchMateria) ? "selected" : "" %>>Inglese</option>
                        <option value="Informatica" <%= "Informatica".equals(searchMateria) ? "selected" : "" %>>Informatica</option>
                        <option value="Latino" <%= "Latino".equals(searchMateria) ? "selected" : "" %>>Latino</option>
                        <option value="Storia" <%= "Storia".equals(searchMateria) ? "selected" : "" %>>Storia</option>
                        <option value="Chimica" <%= "Chimica".equals(searchMateria) ? "selected" : "" %>>Chimica</option>
                    </select>
                </div>

                <div class="filter-group">
                    <label class="filter-label"><i class="fa-solid fa-video"></i> Modalità</label>
                    <select name="modalita" class="filter-input" id="modalitaSelect" onchange="gestisciCitta()">
                        <option value="">Tutte</option>
                        <option value="ONLINE" <%= "ONLINE".equals(searchModalita) ? "selected" : "" %>>Online</option>
                        <option value="PRESENZA" <%= "PRESENZA".equals(searchModalita) ? "selected" : "" %>>In Presenza</option>
                    </select>
                </div>

                <div class="filter-group">
                    <label class="filter-label"><i class="fa-solid fa-location-dot"></i> Città</label>
                    <select name="citta" class="filter-input" id="cittaSelect" <%= "ONLINE".equals(searchModalita) ? "disabled" : "" %>>
                        <option value="">Tutte le città</option>
                        <option value="Roma" <%= "Roma".equals(searchCitta) ? "selected" : "" %>>Roma</option>
                        <option value="Milano" <%= "Milano".equals(searchCitta) ? "selected" : "" %>>Milano</option>
                        <option value="Napoli" <%= "Napoli".equals(searchCitta) ? "selected" : "" %>>Napoli</option>
                        <option value="Torino" <%= "Torino".equals(searchCitta) ? "selected" : "" %>>Torino</option>
                        <option value="Bologna" <%= "Bologna".equals(searchCitta) ? "selected" : "" %>>Bologna</option>
                        <option value="Firenze" <%= "Firenze".equals(searchCitta) ? "selected" : "" %>>Firenze</option>
                        <option value="Salerno" <%= "Salerno".equals(searchCitta) ? "selected" : "" %>>Salerno</option>
                    </select>
                </div>

                <div class="filter-group">
                    <label class="filter-label"><i class="fa-solid fa-euro-sign"></i> Prezzo Max /h</label>
                    <input type="number" name="prezzoMax" class="filter-input" placeholder="Es. 25" min="0" value="<%= searchPrezzo %>">
                </div>

                <button type="submit" class="btn-search">
                    <i class="fa-solid fa-magnifying-glass"></i> Cerca
                </button>

            </form>
        </div>
    </section>

    <script>
        function gestisciCitta() {
            var modalita = document.getElementById("modalitaSelect").value;
            var cittaSelect = document.getElementById("cittaSelect");

            if (modalita === "ONLINE") {
                cittaSelect.value = "";
                cittaSelect.disabled = true;
            } else {
                cittaSelect.disabled = false;
            }
        }
    </script>

    <div class="results-container">
        </div>

    <jsp:include page="footer.jsp" />

</body>
</html>