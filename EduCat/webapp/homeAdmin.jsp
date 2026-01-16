<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%
    // --- MOCK DATA (DA RIMUOVERE QUANDO HAI IL DATABASE) ---
    
    class UtenteMock {
        public int id;
        public String nome, cognome, email, ruolo;
        public UtenteMock(int i, String n, String c, String e, String r) { id=i; nome=n; cognome=c; email=e; ruolo=r; }
    }
    
    class SegnalazioneMock {
        public int id;
        public String mittente, segnalato, motivo;
        public SegnalazioneMock(int i, String m, String s, String mot) { id=i; mittente=m; segnalato=s; motivo=mot; }
    }

    List<UtenteMock> listaUtenti = new ArrayList<>();
    listaUtenti.add(new UtenteMock(1, "Mario", "Rossi", "mario@student.it", "Studente"));
    listaUtenti.add(new UtenteMock(2, "Luigi", "Verdi", "luigi@tutor.it", "Tutor"));
    listaUtenti.add(new UtenteMock(3, "Anna", "Bianchi", "anna@student.it", "Studente"));

    List<SegnalazioneMock> listaSegnalazioni = new ArrayList<>();
    listaSegnalazioni.add(new SegnalazioneMock(101, "Mario Rossi", "Luigi Verdi", "Comportamento scorretto in lezione"));
    listaSegnalazioni.add(new SegnalazioneMock(102, "Anna Bianchi", "Luigi Verdi", "Non si è presentato"));
    
    // -----------------------------------------------------------------------------
%>

<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>EduCat - Admin Dashboard</title>
    
    <link rel="icon" href="<%= request.getContextPath() %>/images/mini-logo.png" type="image/png">
    
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/styles/new/homeAdmin.css">
    
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>

    <jsp:include page="navbar.jsp" />

    <div class="admin-container">
        
        <h1 style="color: #1A5C61; margin-bottom: 10px;">Pannello di Controllo</h1>
        <p style="color: #666; margin-bottom: 30px;">Benvenuto, Amministratore. Gestisci utenti e segnalazioni.</p>

        <div class="stats-grid">
            
            <div class="stat-card">
                <div class="stat-info">
                    <h3>Utenti Totali</h3>
                    <p><%= listaUtenti.size() %></p>
                </div>
                <i class="fa-solid fa-users stat-icon"></i>
            </div>
            
            <div class="stat-card">
                <div class="stat-info">
                    <h3>Segnalazioni</h3>
                    <p><%= listaSegnalazioni.size() %></p>
                </div>
                <i class="fa-solid fa-circle-exclamation stat-icon" style="color: #e57373; opacity: 0.3;"></i>
            </div>
            
        </div>


        <h2 class="section-title">Lista Utenti</h2>
        
        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nominativo</th>
                        <th>Email</th>
                        <th>Ruolo</th>
                        <th style="text-align: right;">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <% for(UtenteMock u : listaUtenti) { %>
                    <tr>
                        <td>#<%= u.id %></td>
                        <td>
                            <strong><%= u.cognome %></strong> <%= u.nome %>
                        </td>
                        <td><%= u.email %></td>
                        <td>
                            <span class="role-badge <%= u.ruolo.equals("Tutor") ? "role-tutor" : "role-student" %>">
                                <%= u.ruolo %>
                            </span>
                        </td>
                        <td style="text-align: right;">
                            <form action="<%= request.getContextPath() %>/EliminaUtenteServlet" method="POST" style="display:inline;">
                                <input type="hidden" name="idUtente" value="<%= u.id %>">
                                <button type="submit" class="action-btn btn-delete" onclick="return confirm('Sei sicuro di voler eliminare questo utente? Questa azione è irreversibile.');">
                                    <i class="fa-solid fa-trash"></i> Elimina
                                </button>
                            </form>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        </div>


        <h2 class="section-title" style="border-color: #e57373;">Segnalazioni Ricevute</h2>

        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Mittente</th>
                        <th>Segnalato</th>
                        <th>Motivo</th>
                        <th style="text-align: right;">Azioni</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (listaSegnalazioni.isEmpty()) { %>
                        <tr><td colspan="5" style="text-align:center; padding: 20px;">Nessuna segnalazione presente.</td></tr>
                    <% } else { 
                        for(SegnalazioneMock s : listaSegnalazioni) { %>
                        <tr>
                            <td>#<%= s.id %></td>
                            <td><%= s.mittente %></td>
                            <td style="color: #c62828; font-weight: bold;"><%= s.segnalato %></td>
                            <td><%= s.motivo %></td>
                            <td style="text-align: right;">
                                <a href="#" class="action-btn btn-view">
                                    <i class="fa-solid fa-check"></i> Risolvi
                                </a>
                                
                                <button class="action-btn btn-delete" onclick="return confirm('Vuoi bannare l\'utente segnalato?');">
                                    <i class="fa-solid fa-ban"></i> Ban Utente
                                </button>
                            </td>
                        </tr>
                    <%  } 
                       } %>
                </tbody>
            </table>
        </div>

    </div>

    <jsp:include page="footer.jsp" />

</body>
</html>