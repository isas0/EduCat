<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="it.unisa.educat.model.PrenotazioneDTO.StatoPrenotazione" %>
<%@ page import="it.unisa.educat.model.UtenteDTO" %>
<%

UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
if (utente == null) {
	response.sendRedirect("../login.jsp");
	return;
}

// Verifica che l'utente abbia permessi
if (!"TUTOR".equals(utente.getTipo().toString())) {
	request.setAttribute("errorMessage", "Accesso negato. \nIdentificati come tutor.");
	session.invalidate();
	request.getRequestDispatcher("/login.jsp").forward(request, response);
	return;
}

	

    // --- MOCK DATA (CLASSI FINTE PER VISUALIZZAZIONE) ---
    
    // 1. Utente Finto
    class UtenteMock {
        private String nome, cognome;
        public UtenteMock(String n, String c) { this.nome=n; this.cognome=c; }
        public String getNome() { return nome; }
        public String getCognome() { return cognome; }
    }

    // 2. Lezione Finta
    class LezioneMock {
        private String materia;
        public LezioneMock(String m) { this.materia = m; }
        public String getMateria() { return materia; }
    }

    // 3. Recensione Finta
    class RecensioneMock {
        public String autore;
        public String testo;
        public int stelle;
        public RecensioneMock(String a, String t, int s) { autore=a; testo=t; stelle=s; }
    }

    // 4. PRENOTAZIONE FINTA
    class PrenotazioneMock {
        private int idPrenotazione;
        private LocalDate dataPrenotazione;
        private StatoPrenotazione stato;
        private float importoPagato;
        private UtenteMock studente;
        private LezioneMock lezione;

        public void setIdPrenotazione(int id) { this.idPrenotazione = id; }
        public void setDataPrenotazione(LocalDate d) { this.dataPrenotazione = d; }
        public LocalDate getDataPrenotazione() { return dataPrenotazione; }
        
        public void setStato(StatoPrenotazione s) { this.stato = s; }
        public StatoPrenotazione getStato() { return stato; }
        
        public void setImportoPagato(float i) { this.importoPagato = i; }
        public float getImportoPagato() { return importoPagato; }
        
        public void setStudente(UtenteMock s) { this.studente = s; }
        public UtenteMock getStudente() { return studente; }
        
        public void setLezione(LezioneMock l) { this.lezione = l; }
        public LezioneMock getLezione() { return lezione; }
    }

    // --- POPOLAZIONE DATI ---
    
    // Lista Recensioni
    List<RecensioneMock> listaRecensioni = new ArrayList<>();
    listaRecensioni.add(new RecensioneMock("Marco R.", "Spiegazione chiarissima, ho passato l'esame!", 5));
    listaRecensioni.add(new RecensioneMock("Giulia B.", "Molto paziente e preparato.", 4));
    listaRecensioni.add(new RecensioneMock("Luca S.", "Tutto ok, lezione utile.", 4));

    // Lista Prenotazioni (Usiamo la classe Mock definita sopra)
    List<PrenotazioneMock> listaPrenotazioni = new ArrayList<>();
    
    // Helpers
    UtenteMock studente1 = new UtenteMock("Giovanni", "Muciaccia");
    UtenteMock studente2 = new UtenteMock("Neil", "Armstrong");
    LezioneMock lezione1 = new LezioneMock("Matematica");
    LezioneMock lezione2 = new LezioneMock("Fisica");
    
    // Prenotazione 1 (ATTIVA)
    PrenotazioneMock p1 = new PrenotazioneMock();
    p1.setIdPrenotazione(10);
    p1.setDataPrenotazione(LocalDate.now().plusDays(2)); 
    p1.setStato(StatoPrenotazione.ATTIVA);
    p1.setImportoPagato(25.0f);
    p1.setStudente(studente1);
    p1.setLezione(lezione1);
    
    // Prenotazione 2 (CONCLUSA)
    PrenotazioneMock p2 = new PrenotazioneMock();
    p2.setIdPrenotazione(11);
    p2.setDataPrenotazione(LocalDate.now().minusDays(5));
    p2.setStato(StatoPrenotazione.CONCLUSA);
    p2.setImportoPagato(30.0f);
    p2.setStudente(studente2);
    p2.setLezione(lezione2);

    // Prenotazione 3 (ANNULLATA)
    PrenotazioneMock p3 = new PrenotazioneMock();
    p3.setIdPrenotazione(12);
    p3.setDataPrenotazione(LocalDate.now().plusDays(10));
    p3.setStato(StatoPrenotazione.ANNULLATA);
    p3.setImportoPagato(25.0f);
    p3.setStudente(studente1);
    p3.setLezione(lezione1);

    listaPrenotazioni.add(p1);
    listaPrenotazioni.add(p3);
    listaPrenotazioni.add(p2);
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Home Tutor</title>
    
    <link rel="icon" href="<%= request.getContextPath() %>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/homeTutor.css">
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="tutor-container">
        
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
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        for (PrenotazioneMock p : listaPrenotazioni) { 
                            String badgeClass = "";
                            // Uso l'enum importato per fare il check
                            if(p.getStato() == StatoPrenotazione.ATTIVA) badgeClass = "status-attiva";
                            else if(p.getStato() == StatoPrenotazione.ANNULLATA) badgeClass = "status-annullata";
                            else badgeClass = "status-conclusa";
                        %>
                        <tr>
                            <td>
                                <span class="td-bold"><%= p.getDataPrenotazione().format(formatter) %></span>
                            </td>
                            <td>
                                <%= p.getStudente().getNome() %> <%= p.getStudente().getCognome() %>
                            </td>
                            <td>
                                <%= p.getLezione().getMateria() %>
                            </td>
                            <td style="font-family: monospace; font-size: 1rem;">
                                € <%= String.format("%.2f", p.getImportoPagato()) %>
                            </td>
                            <td>
                                <span class="status-badge <%= badgeClass %>">
                                    <%= p.getStato().toString() %>
                                </span>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>