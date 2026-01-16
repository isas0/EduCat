<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    <title>EduCat - Scegli il tuo percorso</title>
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <div class="hero-container">
        <div class="overlay"></div>
        
        <div class="content" style="max-width: 1000px;"> <h1>EduCat</h1>
            <p>La piattaforma per l'apprendimento su misura.</p>
            
            <h3 style="color: white; margin-bottom: 30px; font-weight: 400;">Come vuoi registrarti?</h3>

            <div class="btn-container">
                
                <a href="<%=request.getContextPath()%>/registrazioneGenitore.jsp" class="btn-role btn-parent">
                    <i class="fa-solid fa-user-group"></i>
                    <span>Genitore</span>
                    <span style="font-size: 0.8rem; font-weight: normal; opacity: 0.8;">(Per studenti minorenni)</span>
                </a>

                <a href="<%=request.getContextPath()%>/registrazioneStudente.jsp" class="btn-role btn-student-adult">
                    <i class="fa-solid fa-graduation-cap"></i>
                    <span>Studente</span>
                    <span style="font-size: 0.8rem; font-weight: normal; opacity: 0.8;">(Solo maggiorenni)</span>
                </a>

                <a href="<%=request.getContextPath()%>/registrazioneTutor.jsp" class="btn-role btn-tutor">
                    <i class="fa-solid fa-chalkboard-user"></i>
                    <span>Tutor</span>
                    <span style="font-size: 0.8rem; font-weight: normal; opacity: 0.8;">(Diventa insegnante)</span>
                </a>

            </div>

            <p style="margin-top: 40px; font-size: 1rem;">
                Hai già un account? <a href="<%=request.getContextPath()%>/login.jsp" style="color: #E6B800; font-weight: bold; text-decoration: underline;">Accedi qui</a>
            </p>
        </div>
    </div>

</body>
</html>