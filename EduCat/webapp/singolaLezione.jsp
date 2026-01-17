<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="it.unisa.educat.model.LezioneDTO" %>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale"%>

<%
UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
if (utente == null) {
	response.sendRedirect("../login.jsp");
	return;
}

// Verifica che l'utente abbia permessi
if (!"GENITORE".equals(utente.getTipo().toString()) && !"STUDENTE".equals(utente.getTipo().toString())) {
	request.setAttribute("errorMessage", "Accesso negato. \nIdentificati come studente o genitore.");
	session.invalidate();
	request.getRequestDispatcher("/login.jsp").forward(request, response);
	return;
}

LezioneDTO lezione = (LezioneDTO) request.getAttribute("lezione");
if (lezione == null) {
	response.sendRedirect("error.jsp");
	return;
}

DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", Locale.ITALIAN);
String dataFormattata = lezione.getDataInizio().format(formatter);
float prezzoTotale = lezione.getPrezzo() * lezione.getDurata();

UtenteDTO tutor = lezione.getTutor();
String nomeCompletoTutor = tutor.getNome() + " " + tutor.getCognome();
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dettaglio Lezione - <%= lezione.getMateria() %></title>
    
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/styleLezione.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="container-dettaglio">
        
        <div class="card">
            
            <div class="card-header">
                <h1 class="materia-titolo"><%= lezione.getMateria() %></h1>
                
                <div class="tutor-row">
                    <span class="tutor-label">Insegnante:</span>
                    <span class="tutor-nome"><%= lezione.getTutor().getNome() %> <%=lezione.getTutor().getCognome() %></span>
                </div>
            </div>

            <div class="card-body">
                
                <div class="info-grid">
                    <div class="info-item">
                        <span class="label"><i class="fa-regular fa-calendar"></i> Quando</span>
                        <span class="value"><%= dataFormattata %></span>
                    </div>

                    <div class="info-item">
                        <span class="label"><i class="fa-regular fa-clock"></i> Durata</span>
                        <span class="value"><%= lezione.getDurata() %> ore</span>
                    </div>

                    <div class="info-item">
                        <span class="label"><i class="fa-solid fa-video"></i> Modalità</span>
                        <span class="value"><%= lezione.getModalitaLezione().toString() %></span>
                    </div>

                    <div class="info-item">
                        <span class="label"><i class="fa-solid fa-location-dot"></i> Luogo</span>
                        <span class="value"><%= lezione.getCitta() %></span>
                    </div>
                </div>

                <div class="card-footer">
                    
                    <div class="prezzo-box">
                        <span class="label">Totale</span>
                        <span class="prezzo-totale">€ <%= String.format("%.2f", prezzoTotale) %></span>
                        <span class="prezzo-dettaglio">(€ <%= String.format("%.2f", lezione.getPrezzo()) %>/ora)</span>
                    </div>

                    <form action="<%=request.getContextPath()%>/checkout.jsp" method="POST">
                        <input type="hidden" name="idLezione" value="<%= lezione.getIdLezione() %>">
                        
                        <button type="submit" class="btn-prenota">
                            Procedi al Pagamento <i class="fa-solid fa-arrow-right"></i>
                        </button>
                    </form>

                </div>
            </div>
        </div>
    </div>
    
    <jsp:include page="footer.jsp" />

</body>
</html>