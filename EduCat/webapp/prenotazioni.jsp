<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.util.ArrayList" %>
<%@ page import="it.unisa.educat.model.*, it.unisa.educat.dao.GestioneLezioneDAO" %>

<%
    // Protezione Login
    UtenteDTO utente = (UtenteDTO) session.getAttribute("utente");
    if (utente == null) { response.sendRedirect("login.jsp"); return; }

    // Recupero Dati
    GestioneLezioneDAO lezioneDAO = new GestioneLezioneDAO();
    List<PrenotazioneDTO> miePrenotazioni = new ArrayList<>();
    try {
        miePrenotazioni = lezioneDAO.getPrenotazioniByStudente(utente.getUID());
    } catch (Exception e) { e.printStackTrace(); }
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Le mie Prenotazioni</title>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/prenotazioni.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="prenotazioni-container">
        <h1 class="page-title">Le mie Prenotazioni</h1>
        
        <div class="table-wrapper">
            <% if (miePrenotazioni.isEmpty()) { %>
                <div class="empty-state">
                    <p>Non hai ancora effettuato prenotazioni.</p>
                </div>
            <% } else { %>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Data</th><th>Materia</th><th>Tutor</th><th>Stato</th><th style="text-align: right;">Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for(PrenotazioneDTO p : miePrenotazioni) { 
                           // Null check per sicurezza
                           String nomeTutor = "N/D";
                           int idTutor = 0;
                           if(p.getLezione() != null && p.getLezione().getTutor() != null) {
                               nomeTutor = p.getLezione().getTutor().getNome() + " " + p.getLezione().getTutor().getCognome();
                               idTutor = p.getLezione().getTutor().getUID();
                           }
                        %>
                        <tr>
                            <td><%= p.getDataPrenotazione() %></td>
                            <td><strong><%= p.getLezione() != null ? p.getLezione().getMateria() : "-" %></strong></td>
                            <td><%= nomeTutor %></td>
                            <td><span class="status-pill"><%= p.getStato() %></span></td>
                            <td style="text-align: right;">
                                <button class="btn-report" onclick="apriSegnalazione(<%= idTutor %>, '<%= nomeTutor %>')">
                                    <i class="fa-solid fa-circle-exclamation"></i> Segnala
                                </button>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } %>
        </div>
    </div>

    <jsp:include page="modalSegnalazione.jsp" />
    
    <jsp:include page="footer.jsp" />
</body>
</html>