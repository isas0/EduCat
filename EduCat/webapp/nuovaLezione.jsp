<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>
<%
UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
if (utente == null) {
	response.sendRedirect(request.getContextPath()+ "/login.jsp");
	return;
}

// Verifica che l'utente abbia permessi
if (!"TUTOR".equals(utente.getTipo().toString())) {
	request.setAttribute("errorMessage", "Accesso negato. \nIdentificati come tutor.");
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
    <title>EduCat - Crea Nuova Lezione</title>
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/style.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="form-page-container">
        <div class="form-box">
            
            
            <h2 style="text-align: center; color: #1A5C61; margin-bottom: 10px;"><%
				if (request.getParameter("error") != null) {
				%>
				<%=request.getParameter("error")%>
				<%
				} else {
				%>
				Nuova Disponibilità!
				<%
				}
				%></h2>
           
            <p style="text-align: center; color: #888; margin-bottom: 30px; font-size: 0.9rem;">
                Benvenuto! Imposta subito la tua prima lezione per farti trovare dagli studenti.
            </p>
            
            <form action="<%= request.getContextPath() %>/pubblica-annuncio" method="POST">
                
                <div class="form-group">
                    <label class="form-label">Materia</label>
                    <select name="materia" class="form-input" required>
                        <option value="" disabled selected>Seleziona materia...</option>
                        <option value="Matematica">Matematica</option>
                        <option value="Fisica">Fisica</option>
                        <option value="Inglese">Inglese</option>
                        <option value="Informatica">Informatica</option>
                        <option value="Latino">Latino</option>
                        <option value="Storia">Storia</option>
                        <option value="Chimica">Chimica</option>
                    </select>
                </div>

                <div class="form-group">
                    <label class="form-label">Giorno della Lezione</label>
                    <input type="date" name="data" class="form-input" required id="lessonDate">
                </div>

                <div style="display: flex; gap: 15px;">
                    <div class="form-group" style="flex: 1;">
                        <label class="form-label">Ora Inizio</label>
                        <input type="time" name="oraInizio" class="form-input" required>
                    </div>
                    <div class="form-group" style="flex: 1;">
                        <label class="form-label">Ora Fine</label>
                        <input type="time" name="oraFine" class="form-input" required>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label">Prezzo Orario (€)</label>
                    <input type="number" name="prezzo" class="form-input" min="5" step="0.50" placeholder="Es. 15.00" required>
                </div>

                <div class="form-group">
                    <label class="form-label">Modalità</label>
                    <select name="modalita" class="form-input" required>
                        <option value="ONLINE">Online (Webcam)</option>
                        <option value="PRESENZA">In Presenza</option>
                    </select>
                </div>

                <button type="submit" class="submit-btn" style="background-color: #1A5C61;">Pubblica Disponibilità</button>
            </form>
        </div>
    </div>
    
    <script>
        function formatDateLocal(date) {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return year + '-' + month + '-' + day;
        }

        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1); // Imposta minimo a domani

        document.getElementById("lessonDate").setAttribute('min', formatDateLocal(tomorrow));
    </script>

    <jsp:include page="footer.jsp" />

</body>
</html>