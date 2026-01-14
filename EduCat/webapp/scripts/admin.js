document.addEventListener("DOMContentLoaded", () => {
	// 1. Filtro evento per spettacoli
	const filterEvent = document.getElementById("filter-evento");
	if (filterEvent) {
		document.getElementById("filter-by-event-button")?.addEventListener("click", () => {
			const selectedEvent = filterEvent.value;
			const xhr = new XMLHttpRequest();
			xhr.open("GET", "admin-page?eventId=" + encodeURIComponent(selectedEvent), true);
			xhr.onreadystatechange = () => {
				if (xhr.readyState === 4 && xhr.status === 200) {
					document.querySelector("#show-table").innerHTML = xhr.responseText;
				}
			};
			xhr.send();
		});
	}

	// 2. Filtro categoria + venue per eventi
	const filterCat = document.getElementById("filter-categoria-eventi");
	const filterVenue = document.getElementById("filter-venue-eventi");
	if (filterCat && filterVenue) {
		document.getElementById("filter-by-category-and-venue")?.addEventListener("click", () => {
			const xhr = new XMLHttpRequest();
			xhr.open("GET", "admin-page?categoria=" + encodeURIComponent(filterCat.value) + "&venue=" + encodeURIComponent(filterVenue.value), true);
			xhr.onreadystatechange = () => {
				if (xhr.readyState === 4 && xhr.status === 200) {
					document.querySelector("#event-table").innerHTML = xhr.responseText;
				}
			};
			xhr.send();
		});
	}

	// 3. Filtro utenti
	const filterUser = document.getElementById("search-users");
	if (filterUser) {
		document.getElementById("filter-by-user")?.addEventListener("click", () => {
			const xhr = new XMLHttpRequest();
			xhr.open("GET", "admin-page?keyword=" + encodeURIComponent(filterUser.value), true);
			xhr.onreadystatechange = () => {
				if (xhr.readyState === 4 && xhr.status === 200) {
					document.querySelector("#user-table").innerHTML = xhr.responseText;
				}
			};
			xhr.send();
		});
	}

	// 4. Filtro ordini per utente e date
	const clienteSelect = document.getElementById("cliente-select");
	const dataInizio = document.getElementById("data-inizio");
	const dataFine = document.getElementById("data-fine");
	if (clienteSelect && dataInizio && dataFine) {
		document.getElementById("filter-by-user-and-date")?.addEventListener("click", () => {
			const xhr = new XMLHttpRequest();
			xhr.open("GET", "admin-page?userOrder=" + encodeURIComponent(clienteSelect.value)
				+ "&startDate=" + encodeURIComponent(dataInizio.value)
				+ "&endDate=" + encodeURIComponent(dataFine.value), true);
			xhr.onreadystatechange = () => {
				if (xhr.readyState === 4 && xhr.status === 200) {
					document.querySelector("#order-table").innerHTML = xhr.responseText;
				}
			};
			xhr.send();
		});
	}
});

function showAdminSection(sectionId) {
	// Hide all sections
	const sections = document.querySelectorAll('.admin-section');
	sections.forEach(section => {
		section.classList.remove('active');
	});

	// Show selected section
	document.getElementById(sectionId).classList.add('active');

	// Update navigation active state
	const navButtons = document.querySelectorAll('.admin-nav-btn');
	navButtons.forEach(btn => {
		btn.classList.remove('active');
	});

	// Find and activate the corresponding nav button
	const activeButton = Array.from(navButtons).find(btn =>
		btn.getAttribute('onclick').includes(sectionId)
	);
	if (activeButton) {
		activeButton.classList.add('active');
	}
}
