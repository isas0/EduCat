let selectedSeatsIds = [];


function loadSeats() {

	const categorySelect = document.getElementById("selectbtn");
	const categoria = categorySelect.value;

	const showSelect = document.getElementById("showSelect");

	const showId = showSelect.value;

	const venueId = document.getElementById("buy-button").getAttribute("data-venue-id");

	// verifica che siano state selezionate entrambe le opzioni
	if (!showId || showId === "Seleziona spettacolo" || !categoria || categoria === "Seleziona settore") {
		return; // esce se uno dei due non è valido
	}

	const xhr = new XMLHttpRequest();
	xhr.open("get", `show-seat?showId=${showId}&categoria=${encodeURIComponent(categoria)}`, true);
	//xhr.setRequestHeader("Connection", "close");

	xhr.onreadystatechange = function() {
		if (xhr.readyState === 4 && xhr.status === 200) {
			document.querySelector(".grid-container").innerHTML = xhr.responseText;
			selectedSeatsIds = [];

			const buyLink = document.getElementById("buy-button");
			buyLink.href = "#";
			buyLink.setAttribute("data-show-id", showId);
		}
	};

	xhr.send();
}


function selectSeat(seatId) {

	const categorySelect = document.getElementById("selectbtn");
	const categoria = categorySelect.value;

	const seatButton = document.getElementById(seatId);

	//se è già selezionato
	if (selectedSeatsIds.includes(seatId)) {

		selectedSeatsIds = selectedSeatsIds.filter(id => id !== seatId);
		seatButton.classList.remove("selected-seat");

	} else {

		selectedSeatsIds.push(seatId);
		seatButton.classList.add("selected-seat");

	}

	//ora acquista porta all'acquisto corretto
	const buyLink = document.getElementById("buy-button");

	//concatena e separa con virgola
	const Ids = selectedSeatsIds.join(",");

	const contextPath = window.location.pathname.split("/")[1];
	const baseUrl = "/" + contextPath;
	//const url = baseUrl + "/common/MyServlet";

	//passo la stringa con gli id
	buyLink.href = baseUrl + "/common/update-seat?seatIds=" + encodeURIComponent(Ids) + "&pId=" + buyLink.dataset.venueId + "&showId=" + buyLink.dataset.showId + "&quantity=" + selectedSeatsIds.length
		+ "&type=" + categoria;

}

