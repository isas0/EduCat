<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="it.unisa.educat.model.PrenotazioneDTO.StatoPrenotazione" %>
<%@ page import="it.unisa.educat.model.*" %>
<%
UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
if (utente == null) {
	response.sendRedirect("../login.jsp");
	return;
}

List<PrenotazioneDTO> prenotazioni = (List<PrenotazioneDTO>) request.getAttribute("prenotazioni");

// Verifica che l'utente abbia permessi
if (!"TUTOR".equals(utente.getTipo().toString())) {
	request.setAttribute("errorMessage", "Accesso negato. \nIdentificati come tutor.");
	session.invalidate();
	request.getRequestDispatcher("/login.jsp").forward(request, response);
	return;
}

//Messaggi di error/success
String errorMessage = null;
String successMessage = null;

// 1. PRIMA controlla la SESSIONE (per redirect)
errorMessage = (String) session.getAttribute("errorMessage");
successMessage = (String) session.getAttribute("successMessage");

// Rimuovi dalla sessione dopo averli letti (IMPORTANTE!)
if (errorMessage != null) {
	session.removeAttribute("errorMessage");
}
if (successMessage != null) {
	session.removeAttribute("successMessage");
}

// 2. POI controlla la REQUEST (per forward)
if (errorMessage == null && request.getAttribute("errorMessage") != null) {
	errorMessage = (String) request.getAttribute("errorMessage");
}

// 3. Infine controlla PARAMETRI URL
if (errorMessage == null && request.getParameter("error") != null) {
	errorMessage = request.getParameter("error");
}
if (successMessage == null && request.getParameter("success") != null) {
	successMessage = request.getParameter("success");
}

// --- MOCK DATA (CLASSI FINTE PER VISUALIZZAZIONE) ---

// 1. Utente Finto
class UtenteMock {
	private String nome, cognome;

	public UtenteMock(String n, String c) {
		this.nome = n;
		this.cognome = c;
	}

	public String getNome() {
		return nome;
	}

	public String getCognome() {
		return cognome;
	}
}

// 3. Recensione Finta
class RecensioneMock {
	public String autore;
	public String testo;
	public int stelle;

	public RecensioneMock(String a, String t, int s) {
		autore = a;
		testo = t;
		stelle = s;
	}
}

// --- POPOLAZIONE DATI ---

// Lista Recensioni
List<RecensioneMock> listaRecensioni = new ArrayList<>();
listaRecensioni.add(new RecensioneMock("Marco R.", "Spiegazione chiarissima, ho passato l'esame!", 5));
listaRecensioni.add(new RecensioneMock("Giulia B.", "Molto paziente e preparato.", 4));
listaRecensioni.add(new RecensioneMock("Luca S.", "Tutto ok, lezione utile.", 4));

// Helpers
UtenteMock studente1 = new UtenteMock("Giovanni", "Muciaccia");
UtenteMock studente2 = new UtenteMock("Neil", "Armstrong");
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Home Tutor</title>
    
    <link rel="icon" href="<%= request.getContextPath() %>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/homeTutor.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/modalSegnalazioni.css">
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="tutor-container">
    
    
    <%if(errorMessage!=null){ %>
        <h2 class="page-title"><%=errorMessage %></h2>
    <%} %>
        
        <div class="reviews-section">
            <div class="section-header">
                <h2>Dicono di te</h2>
                <p>I feedback recenti dei tuoi studenti.</p>
            </div>

            <% if (listaRecensioni == null || listaRecensioni.isEmpty()) { %>
                <div class="no-reviews-box">
                    <h3 class="no-reviews-text">Nessuna recensione.</h3>
                    <p>Appena completerai delle lezioni, i feedback appariranno qui.</p>
                </div>
            <% } else { %>
                <div class="reviews-grid">
                    <% for (RecensioneMock rec : listaRecensioni) { %>
                    <div class="review-card">
                        <div class="review-header">
                            <span class="student-name"><%= rec.autore %></span>
                            <div class="stars">
                                <% for(int i=0; i<rec.stelle; i++) { %>
                                    <i class="fa-solid fa-star"></i>
                                <% } %>
                                <% for(int i=rec.stelle; i<5; i++) { %>
                                    <i class="fa-regular fa-star" style="color:#ddd;"></i>
                                <% } %>
                            </div>
                        </div>
                        <p class="review-text">"<%= rec.testo %>"</p>
                    </div>
                    <% } %>
                </div>
            <% } %>
        </div>


        <div class="reservations-section">
            <div class="section-header">
                <h2>Le tue Prenotazioni</h2>
                <p>Storico e prossimi appuntamenti.</p>
            </div>

            <div class="table-wrapper">
                <table class="tutor-table">
                    <thead>
                        <tr>
                            <th>Data</th>
                            <th>Studente</th>
                            <th>Materia</th>
                            <th>Importo</th>
                            <th>Stato</th>
                            <th style="text-align: right;">Azioni</th>
                        </tr>
                    </thead>
					<tbody>
						<% 
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        for (PrenotazioneDTO p : prenotazioni) { 
                            String badgeClass = "";
                            // Uso l'enum importato per fare il check
                            if(p.getStato() == StatoPrenotazione.ATTIVA) badgeClass = "status-attiva";
                            else if(p.getStato() == StatoPrenotazione.ANNULLATA) badgeClass = "status-annullata";
                            else badgeClass = "status-conclusa";
                        %>
						<tr>
							<td><span class="td-bold"><%= p.getLezione().getDataInizio().format(formatter) %></span>
							</td>
							<td><%= p.getStudente().getNome() %> <%= p.getStudente().getCognome() %>
							</td>
							<td><%= p.getLezione().getMateria() %></td>
							<td style="font-family: monospace; font-size: 1rem;">€ <%= String.format("%.2f", p.getImportoPagato()) %>
							</td>
							<td><span class="status-badge <%= badgeClass %>"> <%= p.getStato().toString() %>
							</span></td>


							<td style="text-align: right;">
							
								<%if(p.getStato().equals(StatoPrenotazione.ATTIVA)){ %>
								<form action="annulla-prenotazione" method="post" style="display: inline;">
								<input type="hidden" name="idPrenotazione" value="<%=p.getIdPrenotazione() %>"> 
								
								<button type="submit" class="action-btn btn-view"
									onclick="return confirm('Vuoi davvero annullare la prenotazione? L'utente verrà rimborsato.');">									
									<i class="fa-solid fa-circle-exclamation"></i> Annulla
								</button>
								
								</form>
								<%} %>
							
								<button class="btn-report"
									onclick="apriSegnalazione(<%= p.getStudente().getUID()%>, '<%= p.getStudente().getNome()%> <%= p.getStudente().getCognome()%>')">
									<i class="fa-solid fa-circle-exclamation"></i> Segnala
								</button> 
							<% } %>
								
								
								
							</td>
						</tr>
					</tbody>
				</table>
            </div>
        </div>

    </div>
    
	<jsp:include page="modalSegnalazione.jsp" />
    <jsp:include page="footer.jsp" />

</body>
</html>