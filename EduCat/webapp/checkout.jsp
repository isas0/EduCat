<%@page import="it.unisa.educat.controller.*"%>
<%@page import="it.unisa.educat.model.*"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" href="images/loghi/simple-logo.png" type="image/png">
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com">
<link rel="stylesheet"
	href="https://fonts.googleapis.com/css2?family=Poppins:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@6.6.0/css/fontawesome.min.css">
<link rel="stylesheet" href="font-awesome/css/font-awesome.min.css">
<link rel="stylesheet" href="Poppins.css">
<link rel="stylesheet" href="slider.css">
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css"
	rel="stylesheet">
<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/styles/stylesheet.css">
<link rel="stylesheet"
	href="<%=request.getContextPath()%>/styles/footer.css">

<title>GOGOMusic!- Checkout</title>
</head>
<%
UtenteDTO user = (UtenteDTO) request.getSession().getAttribute("user");
if (user != null) { //se l'user appartiene alla sessione
	request.setAttribute("user", user); //lo aggiunge agli attributi della richiesta

} else {
	response.sendRedirect("login.jsp");
}
ArrayList<String> addresses = new ArrayList<String>();
ArrayList<String> methods = new ArrayList<String>();
//Methods è metodi di pagamento
%>
<body>

	<%@include file="/include/navbar.jsp"%>

	<div class="container-checkout">
		<form id="checkoutForm"
			action="<%=request.getContextPath()%>/common/checkout" method="post">
			<div class="row-checkout">
				<div class="col-checkout">
					<h3 class="title-checkout">Indirizzo di fattura</h3>
					<div class="input-box">
						<span>Nome e Cognome:</span> <input name="name" type="text"
							value="<%=user.getNome()%> <%=user.getCognome()%>">
					</div>
					<div class="input-box">
						<span>Email:</span> <input name="email" type="email"
							value="<%=user.getEmail()%>">
					</div>
					<div class="input-box">
						<span>Indirizzo:</span> <input name="indirizzo" type="text"
							placeholder="Via / Piazza, numero civico"
							value="<%=addresses.isEmpty() ? "" : addresses.get(0).getIndirizzo()%>">
					</div>
					<div class="input-box">
						<span>Città:</span> <input name="città" placeholder="Città"
							value="<%= addresses.isEmpty() ? "" : addresses.get(0).getCittà() %>">
					</div>
					<div class="flex">
						<div class="input-box">
							<span>Paese:</span> <input name="paese" type="text"
								placeholder="Paese"
								value="<%=addresses.isEmpty() ? "" : addresses.get(0).getPaese()%>">
						</div>
						<div class="input-box">
							<span>CAP:</span> <input name="cap" type="text" placeholder="CAP"
								value="<%=addresses.isEmpty() ? "" : addresses.get(0).getCAP()%>">
							<div class="error-message" id="cap-error"></div>
						</div>
					</div>
				</div>

				<div class="col-checkout">
					<h3 class="title-checkout">Metodo di pagamento</h3>
					<div class="input-box">
						<span>Carte accettate:</span> <img
							src="<%=request.getContextPath()%>/images/loghi/carte-credito.png">
					</div>
					<div class="input-box">
						<span>Nome e Cognome titolare:</span> <input name="nomeTitolare"
							type="text" placeholder="Nome e Cognome"
							value="<%= methods.isEmpty() ? "" : methods.get(0).getNomeCarta() %>">
					</div>
					<div class="input-box">
						<span>Numero di carta:</span> <input name="cardNumber" type="text"
							placeholder="1234567890123456"
							value="<%= methods.isEmpty() ? "" : methods.get(0).getNumeroCarta() %>">
						<div class="error-message" id="cardNumber-error"></div>
					</div>
					<div class="flex">
						<div class="input-box">
							<span>Scadenza:</span> <input name="scadenza" type="text"
								placeholder="MM/AA"
								value="<%= methods.isEmpty() ? "" : methods.get(0).getScadenza() %>">
							<div class="error-message" id="scadenza-error"></div>
						</div>
						<div class="input-box">
							<span>CVV:</span> <input name="cvv" type="text" placeholder="CVV"
								value="<%= methods.isEmpty() ? "" : methods.get(0).getCvv() %>">
							<div class="error-message" id="cvv-error"></div>
						</div>
					</div>
				</div>
			</div>
			<button type="submit" class="btn-checkout">Acquista</button>
		</form>


	</div>



	<!-- footer -->
	<div class="footer-clean">
		<footer>
			<div class="container">
				<div class="row justify-content-center">
					<div class="col-sm-4 col-md-3 item">
						<h3>Servizi</h3>
						<ul>
							<li>Web design</li>
							<li>Sviluppo</li>
							<li>Hosting</li>
						</ul>
					</div>
					<div class="col-sm-4 col-md-3 item">
						<h3>Chi siamo</h3>
						<ul>
							<li>Azienda</li>
							<li>Team</li>
							<li>Storia</li>
						</ul>
					</div>
					<div class="col-sm-4 col-md-3 item">
						<h3>Info</h3>
						<ul>
							<li>Biglietteria</li>
							<li>Metodo di pagamento</li>
							<li>Metodo di spedizione</li>
						</ul>
					</div>
					<div class="col-lg-3 item social">
						<a href="https://www.facebook.com/?locale=it_IT"><i
							class="fa fa-facebook-square" aria-hidden="true"></i></a> <a
							href="https://x.com"><i class="fa fa-twitter"
							aria-hidden="true"></i></a> <a href="https://www.youtube.com"><i
							class="fa fa-youtube-play" aria-hidden="true"></i></a> <a
							href="https://www.instagram.com"><i class="fa fa-instagram"
							aria-hidden="true"></i></a>
						<p class="copyright">GOGOMusic! © 2025</p>
					</div>
				</div>
			</div>
		</footer>
	</div>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
	<script
		src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.1.3/js/bootstrap.bundle.min.js"></script>
	<script src="<%=request.getContextPath()%>/scripts/javascript.js"></script>
		<script src="<%=request.getContextPath()%>/scripts/validate.js"></script>
	
	<script src="https://kit.fontawesome.com/b53f3cfd48.js"></script>
</body>
</html>