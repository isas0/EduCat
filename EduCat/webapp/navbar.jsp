<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>



<!-- inserire codice di validazione di sessione per utente -->


<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/new/navbar.css">

<div class="header">
    <div class="container">
        <div class="navbar">
            
            <div class="logo">
                <a href="<%=request.getContextPath()%>/index.jsp">
                    <img src="<%= request.getContextPath() %>/images/EduCatLogo.png" alt="EduCat" class="header-logo-img">
                </a>
            </div>

            <nav>
                <ul>
                    <li><a href="<%=request.getContextPath()%>/index.jsp">Home</a></li>
                    <li><a href="<%=request.getContextPath()%>/listaLezioni.jsp">Prenotazioni</a></li>
                    <li><a href="<%=request.getContextPath()%>/account.jsp">Account</a></li>
                </ul>
            </nav>

            <div class="search-container">
                </div>

            <a href="<%=request.getContextPath()%>/carrello.jsp">
                <img src="<%=request.getContextPath()%>/images/carrello-grande.png" alt="Carrello" class="cart-icon">
            </a>
            
            <img src="<%= request.getContextPath() %>/images/menu.png" class="menu-icon" onclick="menutoggle()">

        </div>
    </div>
</div>

<!--serve a gestire il menu a tendina per i dispositivi mobili-->
<script>
    var menuItems = document.querySelector("nav ul");
    menuItems.style.maxHeight = "0px";
    function menutoggle() {
        if (menuItems.style.maxHeight == "0px") {
            menuItems.style.maxHeight = "200px";
        } else {
            menuItems.style.maxHeight = "0px";
        }
    }
</script>