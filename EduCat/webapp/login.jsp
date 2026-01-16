<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Accedi</title>
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/style.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="form-page-container">
        <div class="form-box" style="max-width: 450px;">
            
            <h2 style="text-align: center; color: #1A5C61; margin-bottom: 10px;">Bentornato!</h2>
            <p style="text-align: center; color: #888; margin-bottom: 30px; font-size: 0.9rem;">
                Inserisci le tue credenziali per accedere.
            </p>

            <% 
                String error = (String) request.getAttribute("loginError");
                if (error != null) { 
            %>
                <div style="background-color: #ffebee; color: #c62828; padding: 10px; border-radius: 8px; margin-bottom: 20px; text-align: center; border: 1px solid #ef9a9a;">
                    <%= error %>
                </div>
            <% } %>

            
            <form action="<%= request.getContextPath() %>/LoginServlet" method="POST">
                
                <div class="form-group">
                    <label class="form-label">Email</label>
                    <input type="email" name="email" class="form-input" required placeholder="esempio@email.com">
                </div>

                <div class="form-group">
                    <label class="form-label">Password</label>
                    <input type="password" name="password" class="form-input" required placeholder="********">
                </div>

                <button type="submit" class="submit-btn" style="background-color: #1A5C61;">Accedi</button>
            </form>

            <p style="text-align: center; margin-top: 30px; font-size: 0.95rem;">
                Non hai ancora un account? <br>
                <a href="<%=request.getContextPath()%>/index.jsp" class="text-link">Registrati qui</a>
            </p>
            
        </div>
    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>