<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="it.unisa.educat.model.LezioneDTO" %>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>

<%
    UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
    if (utente == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Verifica permessi
    if (!"GENITORE".equals(utente.getTipo().toString()) && !"STUDENTE".equals(utente.getTipo().toString())) {
        session.invalidate();
        response.sendRedirect("login.jsp");
        return;
    }

    // Recupero parametri per mantenere la selezione nei filtri
    String searchMateria = request.getParameter("materia");
    String searchCitta = request.getParameter("citta");
    String searchModalita = request.getParameter("modalita");
    String searchPrezzo = request.getParameter("prezzoMax");
    
    // Lista risultati dalla Servlet
    List<LezioneDTO> lezioni = (List<LezioneDTO>) request.getAttribute("lezioni");

    // Default per i campi null
    if (searchMateria == null) searchMateria = "";
    if (searchCitta == null) searchCitta = "";
    if (searchModalita == null) searchModalita = "";
    if (searchPrezzo == null) searchPrezzo = "";
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>EduCat - Cerca Lezioni</title>
    <link rel="icon" href="<%= request.getContextPath() %>/images/mini-logo.png" type="image/png">
    
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
                cittaSelect.style.opacity = "0.6";
            } else {
                cittaSelect.disabled = false;
                cittaSelect.style.opacity = "1";
            }
        }
    </script>

    <div class="results-container">
        
        <h2 class="results-title">Risultati della ricerca</h2>

        <% if (lezioni == null || lezioni.isEmpty()) { %>
            
            <div style="text-align:center; padding: 60px 20px; color:#888;">
                <i class="fa-solid fa-ghost" style="font-size: 3rem; margin-bottom: 20px; color: #ddd;"></i>
                <h3>Nessuna lezione trovata.</h3>
                <p>Prova a modificare i filtri di ricerca.</p>
            </div>

        <% } else { %>
            
            <div class="lessons-grid">
                <% for(LezioneDTO l : lezioni) { 
                    String nomeTutor = (l.getTutor() != null) ? l.getTutor().getNome() + " " + l.getTutor().getCognome() : "Tutor";
                    String modalita = (l.getModalitaLezione() != null) ? l.getModalitaLezione().toString() : "N/D";
                    String citta = (l.getCitta() != null) ? l.getCitta() : "";
                    String dataLezione = (l.getDataInizio() != null) ? l.getDataInizio().format(formatter) : "Data da concordare";
                    
                    // Colori badge
                    String badgeColor = "#e0f2f1"; 
                    String badgeText = "#00695c";
                    if("Matematica".equalsIgnoreCase(l.getMateria())) { badgeColor="#fff3e0"; badgeText="#e65100"; }
                    else if("Inglese".equalsIgnoreCase(l.getMateria())) { badgeColor="#e3f2fd"; badgeText="#1565c0"; }
                %>
                
                <div class="lesson-card">
                    <div class="card-top">
                        <span class="subject-badge" style="background-color: <%= badgeColor %>; color: <%= badgeText %>;">
                            <%= l.getMateria() %>
                        </span>
                        <span class="price-tag">
                            € <%= String.format("%.2f", l.getPrezzo()) %>
                            <small style="font-size:0.5em; font-weight:400;">/h</small>
                        </span>
                    </div>

                    <div class="card-content">
                        <h3 class="tutor-name"><%= nomeTutor %></h3>
                        
                        <div class="lesson-info">
                            <i class="fa-regular fa-calendar" style="color:#38B4BC;"></i> 
                            <%= dataLezione %>
                        </div>
                        
                        <div class="lesson-info">
                            <i class="fa-solid fa-video" style="color:#38B4BC;"></i> 
                            <%= modalita %>
                            <% if(!"ONLINE".equals(modalita) && !citta.isEmpty()) { %>
                                (<%= citta %>)
                            <% } %>
                        </div>
                    </div>

                    <div class="card-actions">
                        <a href="<%=request.getContextPath()%>/DettaglioLezioneServlet?id=<%= l.getIdLezione() %>" class="btn-details">
                            Vedi Dettagli
                        </a>
                    </div>
                </div>

                <% } %>
            </div> 
        
        <% } %>
        
    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>