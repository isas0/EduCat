<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="images/mini-logo.png" type="image/png">
    <title>EduCat - Benvenuto!</title>
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/style.css">
</head>
<body>

    <div class="hero-container">
        <div class="overlay"></div>
        
        <div class="content">
            <h1>EduCat</h1>
            <p>Il tuo futuro inizia con la lezione giusta.</p>
            
            <div class="btn-container">
                <a href="<%=request.getContextPath()%>/login.jsp" class="btn btn-login">Log In</a>
                <a href="<%=request.getContextPath()%>/registrazione.jsp" class="btn btn-register">Registrazione</a>
            </div>
        </div>
    </div>

</body>
</html>