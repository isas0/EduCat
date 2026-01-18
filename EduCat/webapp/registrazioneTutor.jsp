<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%request.setAttribute("tipoUtente", "TUTOR"); %>
<!DOCTYPE html>

<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>EduCat - Registrazione Tutor</title>
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/style.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="form-page-container">
        <div class="form-box">
            <h2 style="text-align: center; color: #1A5C61; margin-bottom: 10px;"><%
				if (request.getAttribute("errorMessage") != null) {
				%>
				<%=request.getAttribute("errorMessage")%>
				<%
				} else {
				%>
				Diventa Insegnante
				<%
				}
				%></h2>
            <p style="text-align: center; color: #888; margin-bottom: 30px; font-size: 0.9rem;">
                Devi essere maggiorenne per insegnare su EduCat.
            </p>
            
            <form action="<%= request.getContextPath() %>/registrazione" method="POST">
                
                <div class="form-group"><label class="form-label">Nome</label><input type="text" name="nome" class="form-input" required></div>
                <div class="form-group"><label class="form-label">Cognome</label><input type="text" name="cognome" class="form-input" required></div>
                <div class="form-group"><label class="form-label">Email</label><input type="email" name="email" class="form-input" required></div>
                <div class="form-group"><label class="form-label">Password</label><input type="password" name="password" class="form-input" required></div>

                <div class="form-group">
                    <label class="form-label">Data di Nascita</label>
                    <input type="date" name="dataNascita" id="tutorDate" class="form-input" required>
                </div>

                <div class="form-group"><label class="form-label">Indirizzo</label><input type="text" name="indirizzo" class="form-input" required></div>
                
                <input type="hidden" name="tipoUtente" value="TUTOR">
                <button type="submit" class="submit-btn" style="background-color: #1A5C61;">Crea Account Tutor</button>
            </form>
        </div>
    </div>

    <script>
        // Funzione corretta per il formato data locale
        function formatDateLocal(date) {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return year + '-' + month + '-' + day;
        }

        const today = new Date();
        const eighteenYearsAgo = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());

        // Imposta il MASSIMO: Oggi - 18 anni esatti.
        document.getElementById("tutorDate").setAttribute("max", formatDateLocal(eighteenYearsAgo));
    </script>

    <jsp:include page="footer.jsp" />
</body>
</html>