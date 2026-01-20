<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="it.unisa.educat.model.PrenotazioneDTO.StatoPrenotazione" %>
<%@ page import="it.unisa.educat.model.*" %>
<%@ page import="it.unisa.educat.dao.GestioneLezioneDAO" %>
<%@ page import="it.unisa.educat.dao.GestioneLezioneDAO.CriteriRicerca" %>

<%
UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
if (utente == null) {
	response.sendRedirect("login.jsp");
	return;
}

// Verifica che l'utente abbia permessi
if (!"TUTOR".equals(utente.getTipo().toString())) {
	request.setAttribute("errorMessage", "Accesso negato. \nIdentificati come tutor.");
	session.invalidate();
	request.getRequestDispatcher("/login.jsp").forward(request, response);
	return;
}

// Recupero prenotazioni
List<PrenotazioneDTO> prenotazioni = (List<PrenotazioneDTO>) request.getAttribute("prenotazioni");
if (prenotazioni == null) {
    try {
        GestioneLezioneDAO dao = new GestioneLezioneDAO();
        prenotazioni = dao.getPrenotazioniByTutor(utente.getUID());
    } catch(Exception e) {
        prenotazioni = new ArrayList<>();
    }
}

//Messaggi di error/success
String errorMessage = null;
String successMessage = null;

errorMessage = (String) session.getAttribute("errorMessage");
successMessage = (String) session.getAttribute("successMessage");
if (errorMessage != null) session.removeAttribute("errorMessage");
if (successMessage != null) session.removeAttribute("successMessage");

if (errorMessage == null && request.getAttribute("errorMessage") != null) {
	errorMessage = (String) request.getAttribute("errorMessage");
}

if (errorMessage == null && request.getParameter("error") != null) {
	errorMessage = request.getParameter("error");
}
if (successMessage == null && request.getParameter("success") != null) {
	successMessage = request.getParameter("success");
}

// --- MOCK DATA RECENSIONI ---
class RecensioneMock {
	public String autore;
	public String testo;
	public int stelle;
	public RecensioneMock(String a, String t, int s) { autore = a; testo = t; stelle = s; }
}
List<RecensioneMock> listaRecensioni = new ArrayList<>();
listaRecensioni.add(new RecensioneMock("Marco R.", "Spiegazione chiarissima, ho passato l'esame!", 5));
listaRecensioni.add(new RecensioneMock("Giulia B.", "Molto paziente e preparato.", 4));
listaRecensioni.add(new RecensioneMock("Luca S.", "Tutto ok, lezione utile.", 4));

// --- RECUPERO LEZIONI DISPONIBILI ---
List<LezioneDTO> lezioniDisponibili = new ArrayList<>();
try {
    GestioneLezioneDAO dao = new GestioneLezioneDAO();
    CriteriRicerca criteri = new CriteriRicerca();
    criteri.setIdTutor(utente.getUID());
    lezioniDisponibili = dao.doRetrieveByCriteria(criteri);
} catch(Exception e) {
    e.printStackTrace();
}

DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
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
    
    <style>
        /* CSS MODALE (Incluso qui per comodità, se non è nel file CSS esterno) */
        .modal-overlay-confirm {
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.5); z-index: 9999;
            display: none; justify-content: center; align-items: center;
        }
        .modal-box {
            background: white; padding: 30px; border-radius: 12px;
            width: 90%; max-width: 400px; text-align: center;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
        }
        .modal-icon-warning { font-size: 3rem; color: #f57f17; margin-bottom: 15px; }
        .modal-buttons { margin-top: 25px; display: flex; gap: 10px; justify-content: center; }
        .btn-modal-cancel {
            background: #eee; color: #333; padding: 10px 20px; border-radius: 6px; 
            border: none; cursor: pointer; font-weight: 600; font-family: inherit;
        }
        .btn-modal-confirm {
            background: #c62828; color: white; padding: 10px 20px; border-radius: 6px;
            border: none; cursor: pointer; font-weight: 600; font-family: inherit;
        }
    </style>
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="tutor-container">
    
        <%if(errorMessage!=null){ %>
            <h2 class="page-title" style="color: #c62828;"><%=errorMessage %></h2>
        <%} %>
        <%if(successMessage!=null){ %>
            <h2 class="page-title" style="color: #2e7d32;"><%=successMessage %></h2>
        <%} %>
        
        <div class="dashboard-header">
            <div>
                <h1 style="color: #1A5C61; margin: 0;">Dashboard Tutor</h1>
                <p style="color: #666; margin-top: 5px;">Bentornato, <%= utente.getNome() %>.</p>
            </div>
            <a href="nuovaLezione.jsp" class="btn-add-lesson">
                <i class="fa-solid fa-plus"></i> Nuova Lezione
            </a>
        </div>
        
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
                                <% for(int i=0; i<rec.stelle; i++) { %> <i class="fa-solid fa-star"></i> <% } %>
                                <% for(int i=rec.stelle; i<5; i++) { %> <i class="fa-regular fa-star" style="color:#ddd;"></i> <% } %>
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
                <h2>Le tue Disponibilità</h2>
                <p>Lezioni pianificate in attesa di studenti.</p>
            </div>

            <div class="table-wrapper">
                <table class="tutor-table">
                    <thead>
                        <tr>
                            <th>Data e Ora</th>
                            <th>Materia</th>
                            <th>Modalità</th>
                            <th>Prezzo</th>
                            <th>Stato</th>
                            <th style="text-align: right;">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (lezioniDisponibili == null || lezioniDisponibili.isEmpty()) { %>
                            <tr>
                                <td colspan="6" style="text-align: center; color: #888; padding: 30px;">
                                    Non hai lezioni in attesa. Clicca su "Nuova Lezione" per aggiungerne una.
                                </td>
                            </tr>
                        <% } else { 
                             for (LezioneDTO l : lezioniDisponibili) { %>
                            <tr>
                                <td><span class="td-bold"><%= l.getDataInizio().format(formatter) %></span></td>
                                <td><%= l.getMateria() %></td>
                                <td><%= l.getModalitaLezione() %></td>
                                <td style="font-family: monospace;">€ <%= String.format("%.2f", l.getPrezzo()) %></td>
                                <td>
                                    <span class="status-badge" style="background-color: #e3f2fd; color: #1565c0;">
                                        PIANIFICATA
                                    </span>
                                </td>
                                <td style="text-align: right;">
                                    <button type="button" class="action-btn btn-delete" 
                                            onclick="apriModalElimina(<%= l.getIdLezione() %>)">
                                        <i class="fa-solid fa-trash"></i> Elimina
                                    </button>
                                </td>
                            </tr>
                        <%   } 
                           } %>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="reservations-section" style="margin-top: 50px;">
            <div class="section-header">
                <h2>Prenotazioni Ricevute</h2>
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
                        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        if (prenotazioni != null) {
                            for (PrenotazioneDTO p : prenotazioni) { 
                                String badgeClass = "";
                                if(p.getStato() == StatoPrenotazione.ATTIVA) badgeClass = "status-attiva";
                                else if(p.getStato() == StatoPrenotazione.ANNULLATA) badgeClass = "status-annullata";
                                else badgeClass = "status-conclusa";
                        %>
						<tr>
							<td><span class="td-bold"><%= p.getLezione().getDataInizio().format(dateFmt) %></span></td>
							<td><%= p.getStudente().getNome() %> <%= p.getStudente().getCognome() %></td>
							<td><%= p.getLezione().getMateria() %></td>
							<td style="font-family: monospace; font-size: 1rem;">€ <%= String.format("%.2f", p.getImportoPagato()) %></td>
							<td><span class="status-badge <%= badgeClass %>"> <%= p.getStato().toString() %></span></td>
							<td style="text-align: right;">
							
								<%if(p.getStato().equals(StatoPrenotazione.ATTIVA)){ %>
                                    <button type="button" class="action-btn btn-delete" 
                                            onclick="apriModalAnnulla(<%= p.getIdPrenotazione() %>)">									
    									<i class="fa-solid fa-circle-exclamation"></i> Annulla
    								</button>
								<%} %>
							
								<button class="btn-report"
									onclick="apriSegnalazione(<%= p.getStudente().getUID()%>, '<%= p.getStudente().getNome()%> <%= p.getStudente().getCognome()%>')">
									<i class="fa-solid fa-circle-exclamation"></i> Segnala
								</button> 
							</td>
						</tr>
                        <%  } 
                          } %>
					</tbody>
				</table>
            </div>
        </div>

    </div>
    
	<jsp:include page="modalSegnalazione.jsp" />
    <jsp:include page="footer.jsp" />

    <div id="modalAnnulla" class="modal-overlay-confirm">
        <div class="modal-box">
            <i class="fa-solid fa-triangle-exclamation modal-icon-warning"></i>
            <h3 style="margin-top:0; color:#333;">Conferma Annullamento</h3>
            <p style="color:#666; margin-bottom: 20px;">
                Vuoi davvero annullare la prenotazione? <br>
                Lo studente verrà rimborsato.
            </p>
            <form action="annulla-prenotazione" method="POST">
                <input type="hidden" name="idPrenotazione" id="idPrenotazioneInput" value="">
                <div class="modal-buttons">
                    <button type="button" class="btn-modal-cancel" onclick="chiudiModal('modalAnnulla')">Indietro</button>
                    <button type="submit" class="btn-modal-confirm">Sì, Annulla</button>
                </div>
            </form>
        </div>
    </div>

    <div id="modalElimina" class="modal-overlay-confirm">
        <div class="modal-box">
            <i class="fa-solid fa-trash modal-icon-warning" style="color: #c62828;"></i>
            <h3 style="margin-top:0; color:#333;">Rimuovi Disponibilità</h3>
            <p style="color:#666; margin-bottom: 20px;">
                Vuoi rimuovere questa lezione dalle disponibilità? <br>
                Non sarà più visibile agli studenti.
            </p>
            <form action="cancella-lezione" method="POST">
                <input type="hidden" name="tipoOperazione" value="rimuoviDisponibilita">
                <input type="hidden" name="idLezione" id="idLezioneEliminaInput" value="">
                
                <div class="modal-buttons">
                    <button type="button" class="btn-modal-cancel" onclick="chiudiModal('modalElimina')">Indietro</button>
                    <button type="submit" class="btn-modal-confirm">Sì, Rimuovi</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function chiudiModal(id) {
            document.getElementById(id).style.display = 'none';
        }

        // Modale Annulla Prenotazione (Esistente)
        function apriModalAnnulla(idPrenotazione) {
            document.getElementById('idPrenotazioneInput').value = idPrenotazione;
            document.getElementById('modalAnnulla').style.display = 'flex';
        }

        // Modale Elimina Lezione Pianificata (Nuova)
        function apriModalElimina(idLezione) {
            document.getElementById('idLezioneEliminaInput').value = idLezione;
            document.getElementById('modalElimina').style.display = 'flex';
        }
        
        // Chiudi cliccando fuori
        window.onclick = function(event) {
            if (event.target.classList.contains('modal-overlay-confirm')) {
                event.target.style.display = "none";
            }
        }
    </script>

</body>
</html>