<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    <title>EduCat - Benvenuto!</title>
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <div class="hero-container">
        <div class="overlay"></div>
        
        <div class="content">
            <h1>EduCat</h1>
            <p>Il tuo futuro inizia con la lezione giusta.</p>
            
            <div class="btn-container">
                <a href="<%=request.getContextPath()%>/login.jsp" class="btn btn-login">Log In</a>
                <button onclick="openModal()" class="btn btn-register">Registrazione</button>
            </div>
        </div>
    </div>

    <div id="roleModal" class="modal-overlay">
        <div class="modal-content">
            <span class="close-modal" onclick="closeModal()">&times;</span>
            <h2 class="modal-title">Come vuoi iscriverti?</h2>
            
            <a href="<%=request.getContextPath()%>/registrazioneStudente.jsp" class="role-choice-btn role-student">
                <i class="fa-solid fa-graduation-cap"></i> Studente / Genitore
            </a>
            
            <a href="<%=request.getContextPath()%>/registrazioneTutor.jsp" class="role-choice-btn role-tutor">
                <i class="fa-solid fa-chalkboard-user"></i> Tutor / Insegnante
            </a>
        </div>
    </div>

    <script>
        function openModal() {
            document.getElementById('roleModal').style.display = 'flex';
        }
        function closeModal() {
            document.getElementById('roleModal').style.display = 'none';
        }
        // Chiude il modale se clicchi fuori dal box bianco
        window.onclick = function(event) {
            var modal = document.getElementById('roleModal');
            if (event.target == modal) {
                modal.style.display = "none";
            }
        }
    </script>

</body>
</html>