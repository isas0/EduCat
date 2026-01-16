<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Registrazione Tutor</title>
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/style.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="form-page-container">
        <div class="form-box">
            <h2 style="text-align: center; color: #1A5C61; margin-bottom: 10px;">Diventa un Insegnante</h2>
            <p style="text-align: center; color: #888; margin-bottom: 30px; font-size: 0.9rem;">
                Compila il modulo per iniziare a insegnare su EduCat.
            </p>
            
            <form action="<%= request.getContextPath() %>/RegistrazioneServlet" method="POST">
                
                <div class="form-group">
                    <label class="form-label">Nome</label>
                    <input type="text" name="nome" class="form-input" required placeholder="Es. Mario">
                </div>

                <div class="form-group">
                    <label class="form-label">Cognome</label>
                    <input type="text" name="cognome" class="form-input" required placeholder="Es. Rossi">
                </div>

                <div class="form-group">
                    <label class="form-label">Email</label>
                    <input type="email" name="email" class="form-input" required placeholder="nome@email.com">
                </div>

                <div class="form-group">
                    <label class="form-label">Password</label>
                    <input type="password" name="password" class="form-input" required placeholder="********">
                </div>

                <div class="form-group">
                    <label class="form-label">Data di Nascita</label>
                    <input type="date" name="dataNascita" class="form-input" required>
                </div>

                <div class="form-group">
                    <label class="form-label">Indirizzo Completo</label>
                    <input type="text" name="indirizzo" class="form-input" required placeholder="Via, Civico, Città, CAP">
                </div>
                
                <input type="hidden" name="tipoUtente" value="TUTOR">

                <button type="submit" class="submit-btn">Crea Account Tutor</button>
            </form>
        </div>
    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>