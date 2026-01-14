<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@page import="it.unisa.educat.control.*"%>
<%@page import="it.unisa.educat.storage.*"%>
<%@ page import="java.util.ArrayList"%>


<%

String utente = null;
%>
<!-- navbar  -->

<link rel="stylesheet"
	href="<%= request.getContextPath() %>/styles/suggestions.css">

<div class="header">
	<div class="container">
		<div class="navbar">
			<div class="logo">
				<a href="<%= request.getContextPath() %>/common/index"><img
					src="<%= request.getContextPath() %>/images/loghi/logo-no.png"
					id="logo" width="125px"></a>
			</div>
			<nav>
				<ul id="menuItems">
					<li><a href="<%= request.getContextPath() %>/common/index">Home</a></li>
					<li><a
						href="<%=request.getContextPath()%>/common/index?all=yes">Tutti
							gli eventi</a></li>
					<%if(true){ %>

					<li><a href="<%= request.getContextPath() %>/common/login.jsp">Log
							in</a></li>
					<%} %>

					<%if(true){ %>
					<li><a
						href="<%=request.getContextPath() %>/common/order-history">Ordini</a></li>

					<li><a
						href="<%=request.getContextPath() %>/common/user-account">Account</a></li>


					<%if(true){ %>

					<li><a
						href="<%=request.getContextPath()%>/admin/admin-page">Admin</a></li>
					<%}}if(true){ %>
					<%} %>
				</ul>
			</nav>



			<div class="search-container d-flex align-items-center">
				<form autocomplete="off" class="d-flex" role="search">
					<div class="autocomplete" style="width: 300px;">
						<input id="searchbar" class="form-control me-2" type="search"
							placeholder="Search" aria-label="Search" />
					</div>
				</form>
			</div>
			<a href="<%= request.getContextPath() %>/common/cart-products"><img
				src="<%= request.getContextPath() %>/images/loghi/carrello-grande.png"
				width="30px" height="30px"></a> <img
				src="<%= request.getContextPath() %>/images/loghi/menu-icon.png"
				class="menu-icon" onclick="menutoggle()">
		</div>
	</div>
</div>

<script src="<%= request.getContextPath() %>/scripts/javascript.js"></script>
<script src="<%= request.getContextPath() %>/scripts/suggestions.js"></script>
<script src="<%= request.getContextPath() %>/scripts/navbar.js"></script>
<script>
fetch('<%= request.getContextPath() %>/common/search-data')
  .then(response => response.json())
  .then(concerts => {
    autocomplete(document.getElementById("searchbar"), concerts);
  });
</script>



