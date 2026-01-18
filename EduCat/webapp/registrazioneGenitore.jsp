<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%request.setAttribute("tipoUtente", "GENITORE"); %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>EduCat - Registrazione Genitore</title>
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/style.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="form-page-container">
        <div class="form-box">
            
            <div class="step-indicator">
                <span class="step-dot active" id="dot1"></span>
                <span class="step-dot" id="dot2"></span>
            </div>

            <form action="<%= request.getContextPath() %>/registrazione" method="POST" id="parentForm">
                
                <div class="step-content active" id="step1">
                    <h2 style="text-align: center; color: #E6B800; margin-bottom: 20px;"><%
				if (request.getAttribute("errorMessage") != null) {
				%>
				<%=request.getAttribute("errorMessage")%>
				<%
				} else {
				%>
				Bentornato!
				<%
				}
				%></h2>
                    <p style="text-align: center; color: #666; font-size: 0.9rem; margin-bottom: 20px;">
                        Il genitore deve essere maggiorenne.
                    </p>

                    <div class="form-group"><label class="form-label">Nome Genitore</label><input type="text" name="nome" class="form-input" required></div>
                    <div class="form-group"><label class="form-label">Cognome Genitore</label><input type="text" name="cognome" class="form-input" required></div>
                    <div class="form-group"><label class="form-label">Email Genitore</label><input type="email" name="email" class="form-input" required></div>
                    <div class="form-group"><label class="form-label">Password</label><input type="password" name="password" class="form-input" required></div>
                    
                    <div class="form-group">
                        <label class="form-label">Data di Nascita Genitore</label>
                        <input type="date" name="dataNascita" id="parentDate" class="form-input" required>
                    </div>

                    <div class="form-group"><label class="form-label">Città</label><input type="text" name="città" class="form-input" required></div>
					<div class="form-group"><label class="form-label">CAP</label><input type="text" name="CAP" class="form-input" required></div>
					<div class="form-group"><label class="form-label">Via</label><input type="text" name="via" class="form-input" required></div>
					<div class="form-group"><label class="form-label">Civico</label><input type="text" name="civico" class="form-input" required></div>

                    <button type="button" class="btn-next" onclick="goToStep2()">Avanti &rarr;</button>
                </div>

                <div class="step-content" id="step2">
                    <h2 style="text-align: center; color: #1A5C61; margin-bottom: 20px;">Dati Studente</h2>
                    <p style="text-align: center; color: #666; font-size: 0.9rem; margin-bottom: 20px;">
                        Inserisci i dati del minorenne.
                    </p>

                    <div class="form-group"><label class="form-label">Nome Studente</label><input type="text" name="nomeFiglio" class="form-input" required></div>
                    <div class="form-group"><label class="form-label">Cognome Studente</label><input type="text" name="cognomeFiglio" class="form-input" required></div>
                    
                    <div class="form-group">
                        <label class="form-label">Data di Nascita Studente</label>
                        <input type="date" name="dataNascitaFiglio" id="childDate" class="form-input" required>
                        <small style="color: #E6B800; font-weight: bold;">Deve essere minorenne.</small>
                    </div>

                    <input type="hidden" name="tipoUtente" value="GENITORE">
                    
                    <div style="display: flex; gap: 10px;">
                        <button type="button" class="btn-next" style="background-color: #ccc; width: 30%;" onclick="goToStep1()">&larr;</button>
                        <button type="submit" class="submit-btn">Completa Registrazione</button>
                    </div>
                </div>

            </form>
        </div>
    </div>

    <script>
        // Funzione per formattare la data in YYYY-MM-DD usando l'ora locale (NO UTC)
        function formatDateLocal(date) {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0'); // Mesi partono da 0
            const day = String(date.getDate()).padStart(2, '0');
            return year + '-' + month + '-' + day;
        }

        const today = new Date();
        
        // CALCOLO 18 ANNI FA ESATTI
        const eighteenYearsAgo = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
        
        // CALCOLO LIMITE PER MINORENNI (Un giorno dopo i 18 anni)
        const minorMinDate = new Date(eighteenYearsAgo);
        minorMinDate.setDate(minorMinDate.getDate() + 1);

        // --- VALIDAZIONE GENITORE (Deve essere Maggiorenne) ---
        // Max data possibile: oggi - 18 anni (es. 16/01/2008 incluso)
        document.getElementById("parentDate").setAttribute("max", formatDateLocal(eighteenYearsAgo));

        // --- VALIDAZIONE FIGLIO (Deve essere Minorenne) ---
        // Min data possibile: oggi - 18 anni + 1 giorno (es. 17/01/2008)
        document.getElementById("childDate").setAttribute("min", formatDateLocal(minorMinDate));
        // Non può essere nato nel futuro
        document.getElementById("childDate").setAttribute("max", formatDateLocal(today));

        // Funzioni Wizard Step
        function goToStep2() {
            const step1Inputs = document.querySelectorAll('#step1 input');
            for(let input of step1Inputs) {
                if(!input.checkValidity()) {
                    input.reportValidity();
                    return;
                }
            }
            document.getElementById('step1').classList.remove('active');
            document.getElementById('step2').classList.add('active');
            document.getElementById('dot1').classList.remove('active');
            document.getElementById('dot2').classList.add('active');
        }

        function goToStep1() {
            document.getElementById('step2').classList.remove('active');
            document.getElementById('step1').classList.add('active');
            document.getElementById('dot2').classList.remove('active');
            document.getElementById('dot1').classList.add('active');
        }
    </script>

    <jsp:include page="footer.jsp" />
</body>
</html>