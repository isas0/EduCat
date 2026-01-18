<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.*" %>

<%
    // 1. Controllo Login (Sicurezza)
    UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
    if (utente == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    LezioneDTO lezione = (LezioneDTO) session.getAttribute("lezioneCheckout");
    
    // --- MOCK DATA: SIMULAZIONE CARRELLO ---
    // In produzione: PrenotazioneDTO p = (PrenotazioneDTO) session.getAttribute("prenotazioneInCorso");
    
    String materia = "Matematica - Analisi 1";
    String tutorNome = "Mario Rossi";
    String dataLezione = "18/01/2026";
    String orario = "15:00 - 17:00";
    double prezzoOrario = 15.00;
    int ore = 2;
    double totale = prezzoOrario * ore;
    double costiServizio = 2.00; // Esempio fee piattaforma
    double totaleFinale = totale + costiServizio;
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Checkout</title>
    <link rel="icon" href="<%=request.getContextPath()%>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/checkout.css">
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="checkout-container">
        <h1 class="page-title">Conferma e Paga</h1>

        <form action="<%= request.getContextPath() %>/prenota-lezione" method="POST" class="checkout-grid">
            
            <div class="payment-section card-box">
                <div class="section-title">
                    <span>Metodo di Pagamento</span>
                    <div style="font-size: 1.5rem; color: #888;">
                        <i class="fa-brands fa-cc-visa"></i>
                        <i class="fa-brands fa-cc-mastercard"></i>
                        <i class="fa-brands fa-cc-paypal"></i>
                    </div>
                </div>

                <div class="form-group">
                    <label class="form-label">Indirizzo di Fatturazione</label>
                    <input type="text" name="indirizzo" class="form-input" 
                           value="<%=utente.getCAP()+", "+utente.getCittà()+", "+utente.getVia()+" "+utente.getCivico()%>" required>
                </div>

                <div class="form-group">
                    <label class="form-label">Intestatario Carta</label>
                    <input type="text" name="intestatario" class="form-input" placeholder="Es. LUCA BIANCHI" required>
                </div>

                <div class="form-group">
                    <label class="form-label">Numero Carta</label>
                    <div style="position: relative;">
                        <input type="text" name="numeroCarta" id="cardNumber" class="form-input" placeholder="0000 0000 0000 0000" maxlength="19" required>
                        <i class="fa-solid fa-credit-card" style="position: absolute; right: 15px; top: 12px; color: #aaa;"></i>
                    </div>
                </div>

                <div class="row-split">
                    <div class="form-group">
                        <label class="form-label">Scadenza (MM/YY)</label>
                        <input type="text" name="scadenza" class="form-input" placeholder="MM/YY" maxlength="5" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">CVV</label>
                        <input type="text" name="cvv" class="form-input" placeholder="123" maxlength="3" required>
                    </div>
                </div>

            </div>

			<input type="hidden" name="idLezione" value=<%=lezione.getIdLezione() %>>

            <div class="summary-section card-box">
                <div class="section-title">Riepilogo Ordine</div>

                <div class="summary-item highlight">
                    <span>Materia</span>
                    <span><%= lezione.getMateria() %></span>
                </div>
                
                <div class="summary-item">
                    <span>Tutor</span>
                    <span><%= lezione.getTutor().getNome() %> <%= lezione.getTutor().getCognome() %></span>
                </div>

                <div class="summary-item">
                    <span>Data</span>
                    <span><%= lezione.getDataInizio().getDayOfMonth() %>/<%=lezione.getDataInizio().getMonthValue() %></span>
                </div>

                <div class="summary-item">
                    <span>Orario</span>
                    <span><%= lezione.getDataInizio().getHour() %>:<%=lezione.getDataInizio().getMinute() %></span>
                </div>

                <hr style="border: 0; border-top: 1px solid #eee; margin: 15px 0;">

                <div class="summary-item">
                    <span>Prezzo Lezione (<%= ore %>h)</span>
                    <span>€ <%=String.format("%.2f",lezione.getPrezzo()) %></span>
                </div>
                
                <div class="summary-item">
                    <span>Costi di servizio</span>
                    <span>€ <%= String.format("%.2f", costiServizio) %></span>
                </div>

                <div class="total-row">
                    <span>Totale</span>
                    <span>€ <%= String.format("%.2f",lezione.getPrezzo() + costiServizio) %></span>
                </div>

                <button type="submit" class="btn-pay">
                    <i class="fa-solid fa-lock"></i> Paga Ora
                </button>
                
                <p style="text-align: center; font-size: 0.8rem; color: #888; margin-top: 10px;">
                    Pagamenti sicuri e crittografati SSL.
                </p>
            </div>

        </form>
    </div>

    <script>
        document.getElementById('cardNumber').addEventListener('input', function (e) {
            e.target.value = e.target.value.replace(/[^\d]/g, '').replace(/(.{4})/g, '$1 ').trim();
        });
    </script>

    <jsp:include page="footer.jsp" />

</body>
</html>