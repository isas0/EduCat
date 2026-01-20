<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.*" %>
<%@ page import="it.unisa.educat.dao.GestioneLezioneDAO" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.LocalDateTime" %>

<%
    // 1. Controllo Login
    UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
    if (utente == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // 2. Costruzione Indirizzo Utente (dai campi separati del tuo UtenteDTO)
    String indirizzoUtente = "";
    if (utente.getVia() != null) indirizzoUtente += utente.getVia();
    if (utente.getCivico() != null) indirizzoUtente += " " + utente.getCivico();
    if (utente.getCittà() != null) indirizzoUtente += ", " + utente.getCittà(); // Nota: getCittà() con accento come nel tuo DTO
    if (utente.getCAP() != null) indirizzoUtente += " " + utente.getCAP();
    
    if (indirizzoUtente.trim().isEmpty() || indirizzoUtente.equals(", ")) indirizzoUtente = ""; 

    // 3. Recupero Dati Lezione dal DB
    String idLezioneStr = request.getParameter("idLezione");
    
    LezioneDTO lezione = (LezioneDTO) session.getAttribute("lezioneCheckout");

    // Se la lezione non esiste o l'ID è sbagliato, torna alla home
    if (lezione == null) {
        response.sendRedirect("homePageStudenteGenitore.jsp");
        return;
    }

    // 4. Preparazione Dati per Visualizzazione
    UtenteDTO tutor = lezione.getTutor();
    String nomeTutor = "N/D";
    if (tutor != null) {
        nomeTutor = tutor.getNome() + " " + tutor.getCognome();
    }
    
    String materia = lezione.getMateria();
    
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALIAN);
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    
    // CORREZIONE: Uso i metodi getDataInizio() e getDataFine() presenti nel tuo LezioneDTO
    LocalDateTime dataInizio = lezione.getDataInizio(); 
    LocalDateTime dataFine = lezione.getDataFine();

    String dataLezioneStr = (dataInizio != null) ? dataInizio.format(dateFmt) : "Data da concordare";
    String orarioStr = (dataInizio != null && dataFine != null) ? 
                       dataInizio.format(timeFmt) + " - " + dataFine.format(timeFmt) : "Orario da definire";
    
    // Calcolo Prezzi
    float prezzoBase = lezione.getPrezzo();
    float durata = lezione.getDurata(); 
    
    double totaleLezione = prezzoBase * durata; 
    double costiServizio = 2.00; 
    double totaleFinale = totaleLezione + costiServizio;
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Checkout Sicuro</title>
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/checkout.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    
    <style>
        .input-error { border: 1px solid #d32f2f !important; background-color: #ffebee; }
        .error-msg { color: #d32f2f; font-size: 0.8rem; margin-top: 5px; display: none; }
        .form-group { margin-bottom: 20px; }
    </style>
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="checkout-container">
        <h1 class="page-title">Conferma e Paga</h1>

        <form action="<%= request.getContextPath() %>/prenota-lezione" method="POST" class="checkout-grid" id="paymentForm" onsubmit="return validatePayment(event)">
            
            <div class="payment-section card-box">
                <div class="section-title">
                    <span>Dati di Pagamento</span>
                    <div style="font-size: 1.5rem; color: #888;">
                        <i class="fa-brands fa-cc-visa"></i>
                        <i class="fa-brands fa-cc-mastercard"></i>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label">Indirizzo di Fatturazione</label>
                    <input type="text" name="indirizzo" class="form-input" required
                           value="<%= indirizzoUtente %>">
                </div>

                <div class="form-group">
                    <label class="form-label">Intestatario Carta</label>
                    <input type="text" name="intestatario" id="cardName" class="form-input" placeholder="Es. MARIO ROSSI" 
                           value="<%= utente.getNome().toUpperCase() + " " + utente.getCognome().toUpperCase() %>" required>
                    <span class="error-msg" id="errorName">Inserisci nome e cognome validi.</span>
                </div>

                <div class="form-group">
                    <label class="form-label">Numero Carta</label>
                    <div style="position: relative;">
                        <input type="text" name="numeroCarta" id="cardNumber" class="form-input" 
                               placeholder="0000 0000 0000 0000" maxlength="19" required oninput="formatCardNumber(this)">
                        <i class="fa-solid fa-credit-card" style="position: absolute; right: 15px; top: 12px; color: #aaa;"></i>
                    </div>
                    <span class="error-msg" id="errorCard">Il numero di carta deve avere 16 cifre.</span>
                </div>

                <div class="row-split">
                    <div class="form-group">
                        <label class="form-label">Scadenza (MM/YY)</label>
                        <input type="text" name="scadenza" id="expiryDate" class="form-input" 
                               placeholder="MM/YY" maxlength="5" required oninput="formatExpiry(this)">
                        <span class="error-msg" id="errorDate">Data non valida o scaduta.</span>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label">CVV</label>
                        <input type="password" name="cvv" id="cvv" class="form-input" 
                               placeholder="123" maxlength="3" required oninput="onlyNumbers(this)">
                        <span class="error-msg" id="errorCvv">Inserisci 3 cifre.</span>
                    </div>
                </div>
            </div>

            <input type="hidden" name="idLezione" value="<%= lezione.getIdLezione() %>">
            
            <input type="hidden" name="importoPagato" value="<%= String.format(Locale.US, "%.2f", totaleFinale) %>">

            <div class="summary-section card-box">
                <div class="section-title">Riepilogo Ordine</div>
                <div class="summary-item highlight"><span>Materia</span><span><%= materia %></span></div>
                <div class="summary-item"><span>Tutor</span><span><%= nomeTutor %></span></div>
                <div class="summary-item"><span>Data</span><span><%= dataLezioneStr %></span></div>
                <div class="summary-item"><span>Orario</span><span><%= orarioStr %></span></div>
                <hr style="border: 0; border-top: 1px solid #eee; margin: 15px 0;">
                <div class="summary-item"><span>Prezzo Lezione (<%= durata %>h)</span><span>€ <%= String.format("%.2f", totaleLezione) %></span></div>
                <div class="summary-item"><span>Servizio</span><span>€ <%= String.format("%.2f", costiServizio) %></span></div>
                <div class="total-row"><span>Totale</span><span>€ <%= String.format("%.2f", totaleFinale) %></span></div>

                <button type="submit" class="btn-pay" id="btnPay">
                    <i class="fa-solid fa-lock"></i> Paga Ora € <%= String.format("%.2f", totaleFinale) %>
                </button>
            </div>
        </form>
    </div>

    <script>
        function formatCardNumber(input) {
            let value = input.value.replace(/\D/g, '');
            let formattedValue = '';
            for (let i = 0; i < value.length; i++) {
                if (i > 0 && i % 4 === 0) formattedValue += ' ';
                formattedValue += value[i];
            }
            input.value = formattedValue;
            resetError('cardNumber', 'errorCard');
        }

        function formatExpiry(input) {
            let value = input.value.replace(/\D/g, '');
            if (value.length >= 2) {
                input.value = value.substring(0, 2) + '/' + value.substring(2, 4);
            } else {
                input.value = value;
            }
            resetError('expiryDate', 'errorDate');
        }

        function onlyNumbers(input) {
            input.value = input.value.replace(/\D/g, '');
            resetError('cvv', 'errorCvv');
        }

        function resetError(inputId, errorId) {
            document.getElementById(inputId).classList.remove('input-error');
            document.getElementById(errorId).style.display = 'none';
        }

        function validatePayment(event) {
            let valid = true;

            const cardNum = document.getElementById('cardNumber');
            const cardVal = cardNum.value.replace(/\s/g, '');
            if (cardVal.length !== 16) {
                showError('cardNumber', 'errorCard');
                valid = false;
            }

            const cvv = document.getElementById('cvv');
            if (cvv.value.length !== 3) {
                showError('cvv', 'errorCvv');
                valid = false;
            }

            const name = document.getElementById('cardName');
            if (!name.value.trim().includes(' ')) {
                showError('cardName', 'errorName');
                valid = false;
            }

            const dateInput = document.getElementById('expiryDate');
            const dateVal = dateInput.value;
            const datePattern = /^(0[1-9]|1[0-2])\/\d{2}$/;

            if (!datePattern.test(dateVal)) {
                showError('expiryDate', 'errorDate');
                valid = false;
            } else {
                const parts = dateVal.split('/');
                const month = parseInt(parts[0], 10);
                const year = parseInt("20" + parts[1], 10);
                const now = new Date();
                const currentYear = now.getFullYear();
                const currentMonth = now.getMonth() + 1;

                if (year < currentYear || (year === currentYear && month < currentMonth)) {
                    document.getElementById('errorDate').innerText = "Carta scaduta.";
                    showError('expiryDate', 'errorDate');
                    valid = false;
                }
            }

            if (!valid) {
                event.preventDefault();
            } else {
                const btn = document.getElementById('btnPay');
                btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Elaborazione...';
                btn.style.opacity = "0.7";
            }
            return valid;
        }

        function showError(inputId, errorId) {
            document.getElementById(inputId).classList.add('input-error');
            document.getElementById(errorId).style.display = 'block';
        }
    </script>

    <jsp:include page="footer.jsp" />

</body>
</html>