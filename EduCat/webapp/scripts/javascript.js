//menu itmes
// Menu toggle function
function menutoggle() {
	const menuItems = document.getElementById('menuItems');

	if (menuItems) {
		menuItems.classList.toggle('show');
		console.log('Menu toggled, classes:', menuItems.className);
	} else {
		console.error('Element menuItems not found');
	}
}

// Gestione scroll e click 
document.addEventListener('DOMContentLoaded', function() {
	let scrollTimeout;
	let lastScrollPosition = 0;

	// Gestione scroll
	window.addEventListener('scroll', function() {
		const currentScroll = window.pageYOffset || document.documentElement.scrollTop;
		const navbar = document.querySelector('.navbar');
		const menuItems = document.getElementById('menuItems');

		if (currentScroll > lastScrollPosition && navbar && menuItems) {
			// Scrolling down
			navbar.classList.add('scrolling');
			if (menuItems.classList.contains('show')) {
				menuItems.classList.remove('show');
			}
		} else {
			// Scrolling up
			if (navbar) navbar.classList.remove('scrolling');
		}
		lastScrollPosition = currentScroll;
	});

	// Chiudi menu quando si clicca fuori
	document.addEventListener('click', function(event) {
		const menuItems = document.getElementById('menuItems');
		const menuIcon = document.querySelector('.menu-icon');
		const navbar = document.querySelector('.navbar');

		if (menuItems && menuIcon && navbar) {
			const isClickInsideNav = navbar.contains(event.target);
			const isMenuOpen = menuItems.classList.contains('show');

			if (!isClickInsideNav && isMenuOpen) {
				menuItems.classList.remove('show');
			}
		}
	});

	// Chiudi menu quando si clicca su un link
	const menuLinks = document.querySelectorAll('#menuItems a');
	menuLinks.forEach(link => {
		link.addEventListener('click', function() {
			const menuItems = document.getElementById('menuItems');
			if (window.innerWidth <= 800 && menuItems) {
				setTimeout(() => {
					menuItems.classList.remove('show');
				}, 100);
			}
		});
	});
});



window.addEventListener("DOMContentLoaded", () => {
	const loginForm = document.getElementById("loginForm");
	const regForm = document.getElementById("regForm");
	const indicator = document.getElementById("indicator");

	if (!loginForm || !regForm || !indicator) {
		console.error("Uno degli elementi non esiste!");
		return;
	}

	window.register = function() {
		loginForm.style.transform = "translateX(-100%)";
		regForm.style.transform = "translateX(0)";
		indicator.style.transform = "translateX(100px)";
	};

	window.login = function() {
		loginForm.style.transform = "translateX(0)";
		regForm.style.transform = "translateX(100%)";
		indicator.style.transform = "translateX(0)";
	};
});




document.addEventListener("DOMContentLoaded", function() {
	// Galleria prodotti
	const productImg = document.getElementById("product-img");
	const smallImgs = document.getElementsByClassName("small-img");

	for (let i = 0; i < smallImgs.length; i++) {
		smallImgs[i].addEventListener("click", function() {
			productImg.src = smallImgs[i].src;
		});
	}

	// Overlay mappa
	const btn = document.getElementById("showImageBtn");
	const overlay = document.getElementById("imageOverlay");
	const closeBtn = document.querySelector(".close-btn");

	btn?.addEventListener("click", function() {
		overlay.style.display = "block";
	});

	closeBtn?.addEventListener("click", function() {
		overlay.style.display = "none";
	});

	overlay?.addEventListener("click", function(e) {
		if (e.target === overlay) {
			overlay.style.display = "none";
		}
	});
});


/*pagina ordini*/
function toggleDetails(orderId) {
	const detailsElement = document.getElementById(orderId);
	const button = detailsElement.previousElementSibling.querySelector('.details-btn');
	const btnText = button.querySelector('.btn-text');
	const arrow = button.querySelector('.arrow');

	if (detailsElement.style.display === 'block' || detailsElement.classList.contains('show')) {
		// Nascondi i dettagli
		detailsElement.style.display = 'none';
		detailsElement.classList.remove('show');
		btnText.textContent = 'Vedi Dettagli';
		arrow.style.transform = 'rotate(0deg)';
	} else {
		// Mostra i dettagli
		detailsElement.style.display = 'block';
		detailsElement.classList.add('show');
		btnText.textContent = 'Nascondi Dettagli';
		arrow.style.transform = 'rotate(180deg)';
	}
}



// Admin Dashboard 
// Show/Hide admin sections
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

// Seat Management Functions
/*function updateSeatPrices() {
	const spettacolo = document.getElementById('spettacolo-select').value;
	const categoria = document.getElementById('categoria-select').value;
	const prezzo = document.getElementById('nuovo-prezzo').value;
	const applicaCategoria = document.getElementById('applica-categoria').checked;

	if (!spettacolo || !categoria || !prezzo) {
		alert('Compila tutti i campi obbligatori');
		return;
	}

	console.log('Aggiornamento prezzi:', {
		spettacolo,
		categoria,
		prezzo,
		applicaCategoria
	});

	alert('Prezzi aggiornati con successo!');

	// Reset form
	document.getElementById('spettacolo-select').value = '';
	document.getElementById('categoria-select').value = '';
	document.getElementById('nuovo-prezzo').value = '';
	document.getElementById('applica-categoria').checked = false;
}

// Show Management Functions
function addShow() {
	const form = document.querySelector('#gestione-spettacoli .admin-form');
	const formData = new FormData(form);

	// Validate required fields
	const evento = formData.get('evento');
	const venue = formData.get('venue');
	const data = formData.get('data');
	const ora = formData.get('ora');

	if (!evento || !venue || !data || !ora) {
		alert('Compila tutti i campi obbligatori');
		return;
	}

	// Simulate API call
	console.log('Nuovo spettacolo:', Object.fromEntries(formData));
	alert('Spettacolo aggiunto con successo!');
	form.reset();
}

function editShow(id) {
	alert(`Modifica spettacolo ID: ${id}`);
	// Here you would typically open a modal or redirect to edit page
}

function deleteShow(id) {
	if (confirm('Sei sicuro di voler eliminare questo spettacolo?')) {
		console.log(`Eliminazione spettacolo ID: ${id}`);
		alert('Spettacolo eliminato con successo!');
		// Here you would make API call and remove row from table
	}
}

function filterShows() {
	const eventoFilter = document.getElementById('filter-evento').value;
	console.log('Filtra spettacoli per evento:', eventoFilter);
	// Implement filtering logic here
	//alert('Filtro applicato per evento: ' + (eventoFilter || 'Tutti'));
	//non ritengo necessario un alert anche per questo	
}

// Event Management Functions
function addEvent() {
	const form = document.querySelector('#gestione-eventi .admin-form');
	const formData = new FormData(form);

	const nome = formData.get('nome');
	const categoria = formData.get('categoria');
	const venueId = formData.get('venue_id');

	if (!nome || !categoria || !venueId) {
		alert('Compila tutti i campi obbligatori');
		return;
	}

	console.log('Nuovo evento:', Object.fromEntries(formData));
	alert('Evento aggiunto con successo!');
	form.reset();
}

function editEvent(id) {
	alert(`Modifica evento ID: ${id}`);

}

function deleteEvent(id) {
	if (confirm('Sei sicuro di voler eliminare questo evento e tutti i suoi spettacoli?')) {
		console.log(`Eliminazione evento ID: ${id}`);
		alert('Evento eliminato con successo!');
	}
}

function viewShows(eventId) {
	alert(`Visualizza spettacoli per evento ID: ${eventId}`);
	// Implement show viewing functionality
}

function filterEvents() {
	const categoriaFilter = document.getElementById('filter-categoria-eventi').value;
	const venueFilter = document.getElementById('filter-venue-eventi').value;
	console.log('Filtra eventi:', { categoriaFilter, venueFilter });
	//alert('Filtri applicati per categoria: ' + (categoriaFilter || 'Tutte') + 
	//      ' e venue: ' + (venueFilter || 'Tutti'));
}

// User Management Functions
function searchUsers() {
	const searchTerm = document.getElementById('search-users').value;
	console.log('Cerca utenti:', searchTerm);
	alert('Ricerca utenti per: ' + (searchTerm || 'tutti gli utenti'));
}

function editUser(id) {
	alert(`Modifica utente ID: ${id}`);
	// Implement edit functionality
}

function deleteUser(id) {
	if (confirm('Sei sicuro di voler eliminare questo utente?')) {
		console.log(`Eliminazione utente ID: ${id}`);
		alert('Utente eliminato con successo!');
	}
}

function viewUserOrders(userId) {
	alert(`Visualizza ordini per utente ID: ${userId}`);
	// Implement order viewing functionality
}

// Order Management Functions
function filterOrders() {
	const dataInizio = document.getElementById('data-inizio').value;
	const dataFine = document.getElementById('data-fine').value;
	const cliente = document.getElementById('cliente-select').value;
	const stato = document.getElementById('stato-ordine').value;

	console.log('Filtra ordini:', {
		dataInizio,
		dataFine,
		cliente,
		stato
	});

	let filterMessage = 'Filtri applicati:';
	if (dataInizio) filterMessage += ` Da: ${dataInizio}`;
	if (dataFine) filterMessage += ` A: ${dataFine}`;
	if (cliente) filterMessage += ` Cliente: ${cliente}`;
	if (stato) filterMessage += ` Stato: ${stato}`;

	alert(filterMessage);
}

function viewOrder(orderId) {
	alert(`Visualizza dettagli ordine: ${orderId}`);
	// Implement order details functionality
}

function cancelOrder(orderId) {
	if (confirm('Sei sicuro di voler annullare questo ordine?')) {
		console.log(`Annullamento ordine: ${orderId}`);
		alert('Ordine annullato con successo!');
	}
}

// Catalog Management Functions
function filterCatalog() {
	const categoriaFilter = document.getElementById('filter-categoria-catalogo').value;
	console.log('Filtra catalogo per categoria:', categoriaFilter);
	// alert('Filtro catalogo applicato per categoria: ' + (categoriaFilter || 'Tutte'));
}

function editProduct(id) {
	alert(`Modifica prodotto ID: ${id}`);
	// Implement edit functionality
}

function deleteProduct(id) {
	if (confirm('Sei sicuro di voler eliminare questo prodotto?')) {
		console.log(`Eliminazione prodotto ID: ${id}`);
		alert('Prodotto eliminato con successo!');
	}
}

// Form submission handlers
document.addEventListener('DOMContentLoaded', function() {
	// Handle form submissions
	const forms = document.querySelectorAll('.admin-form');
	forms.forEach(form => {
		form.addEventListener('submit', function(e) {
			e.preventDefault();

			// Determine which form was submitted
			const section = form.closest('.admin-section');
			const sectionId = section.id;

			switch (sectionId) {
				case 'gestione-posti':
					updateSeatPrices();
					break;
				case 'gestione-spettacoli':
					addShow();
					break;
				case 'gestione-eventi':
					addEvent();
					break;
				default:
					console.log('Form submitted for section:', sectionId);
			}
		});
	});	showAdminSection('gestione-posti');
	});
*/

// Initialize first section as active

// Menu toggle function for mobile (from original theme)
function menutoggle() {
	const menuItems = document.getElementById('menuItems');
	if (menuItems) {
		menuItems.classList.toggle('show');
	}
}

// Utility functions
function formatCurrency(amount) {
	return new Intl.NumberFormat('it-IT', {
		style: 'currency',
		currency: 'EUR'
	}).format(amount);
}

function formatDate(date) {
	return new Intl.DateTimeFormat('it-IT').format(new Date(date));
}

// Handle window scroll for mobile menu (from original theme)
let lastScrollTop = 0;
window.addEventListener('scroll', function() {
	let scrollTop = window.pageYOffset || document.documentElement.scrollTop;
	const nav = document.querySelector('nav');

	if (scrollTop > lastScrollTop && nav) {
		// Scrolling down - close mobile menu
		nav.classList.add('scrolling');
		const menuItems = document.getElementById('menuItems');
		if (menuItems && menuItems.classList.contains('show')) {
			menuItems.classList.remove('show');
		}
	} else {
		// Scrolling up
		if (nav) nav.classList.remove('scrolling');
	}
	lastScrollTop = scrollTop;
});

// Export functions for external use
window.adminFunctions = {
	showAdminSection,
	updateSeatPrices,
	addShow,
	editShow,
	deleteShow,
	filterShows,
	addEvent,
	editEvent,
	deleteEvent,
	viewShows,
	filterEvents,
	searchUsers,
	editUser,
	deleteUser,
	viewUserOrders,
	filterOrders,
	viewOrder,
	cancelOrder,
	filterCatalog,
	editProduct,
	deleteProduct
};


// Account Page 

// Show/Hide account sections
function showAccountSection(sectionId) {
	// Hide all sections
	const sections = document.querySelectorAll('.content-section');
	sections.forEach(section => {
		section.classList.remove('active');
	});

	// Show selected section
	const targetSection = document.getElementById(sectionId);
	if (targetSection) {
		targetSection.classList.add('active');
	}

	// Update menu active state (desktop)
	const menuItems = document.querySelectorAll('.menu-item');
	menuItems.forEach(item => {
		item.classList.remove('active');
	});

	const activeMenuItem = document.querySelector(`.menu-item[data-section="${sectionId}"]`);
	if (activeMenuItem) {
		activeMenuItem.classList.add('active');
	}

	// Update mobile buttons active state
	const mobileButtons = document.querySelectorAll('.mobile-nav-btn');
	mobileButtons.forEach(btn => {
		btn.classList.remove('active');
	});

	const activeMobileBtn = document.querySelector(`.mobile-nav-btn[data-section="${sectionId}"]`);
	if (activeMobileBtn) {
		activeMobileBtn.classList.add('active');
	}
}

// Form handlers
/*
function updatePersonalInfo() {
	const form = document.getElementById('form-info-personali');
	const formData = new FormData(form);

	console.log('Aggiornamento informazioni personali:', Object.fromEntries(formData));
	alert('Informazioni personali aggiornate con successo!');
}

function addNewAddress() {
	const form = document.getElementById('form-nuovo-indirizzo');
	const formData = new FormData(form);

	if (!form.checkValidity()) {
		alert('Compila tutti i campi obbligatori');
		return;
	}

	console.log('Nuovo indirizzo:', Object.fromEntries(formData));
	alert('Indirizzo aggiunto con successo!');

	// Hide form and reset
	document.getElementById('add-address-form').style.display = 'none';
	document.getElementById('btn-show-add-address').style.display = 'block';
	form.reset();
}

function addNewPayment() {
	const form = document.getElementById('form-nuovo-pagamento');
	const formData = new FormData(form);

	if (!form.checkValidity()) {
		alert('Compila tutti i campi obbligatori');
		return;
	}

	console.log('Nuovo metodo di pagamento:', Object.fromEntries(formData));
	alert('Metodo di pagamento aggiunto con successo!');

	// Hide form and reset
	document.getElementById('add-payment-form').style.display = 'none';
	document.getElementById('btn-show-add-payment').style.display = 'block';
	form.reset();
}

function changePassword() {
	const form = document.getElementById('form-password');
	const passwordAttuale = document.getElementById('password-attuale').value;
	const nuovaPassword = document.getElementById('nuova-password').value;
	const confermaPassword = document.getElementById('conferma-password').value;

	if (!passwordAttuale || !nuovaPassword || !confermaPassword) {
		alert('Compila tutti i campi');
		return;
	}

	if (nuovaPassword !== confermaPassword) {
		alert('Le password non coincidono');
		return;
	}

	if (nuovaPassword.length < 8) {
		alert('La password deve essere di almeno 8 caratteri');
		return;
	}

	console.log('Cambio password richiesto');
	alert('Password cambiata con successo!');
	form.reset();
}

function logout() {
	if (confirm('Sei sicuro di voler effettuare il log out?')) {
		console.log('Logout richiesto');
		alert('Log out in corso...');
		// Redirect to login page
		window.location.href = 'Login.html';
	}
}*/

// Generic action handler
function handleAction(action, id) {
	switch (action) {
		case 'edit-address':
			alert(`Modifica indirizzo ID: ${id}`);
			break;
		case 'delete-address':
			if (confirm('Sei sicuro di voler eliminare questo indirizzo?')) {
				alert('Indirizzo eliminato con successo!');
			}
			break;
		case 'edit-payment':
			alert(`Modifica metodo di pagamento ID: ${id}`);
			break;
		case 'delete-payment':
			if (confirm('Sei sicuro di voler rimuovere questo metodo di pagamento?')) {
				alert('Metodo di pagamento rimosso con successo!');
			}
			break;
		default:
			console.log('Azione non riconosciuta:', action, id);
	}
}

// Format card number input
function formatCardNumber(input) {
	let value = input.value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
	let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value;
	input.value = formattedValue;
}

// Format expiry date input
function formatExpiryDate(input) {
	let value = input.value.replace(/\D/g, '');
	if (value.length >= 2) {
		value = value.substring(0, 2) + '/' + value.substring(2, 4);
	}
	input.value = value;
}

// Menu toggle function for mobile (navbar)
function menutoggle() {
	const menuItems = document.getElementById('menuItems');
	if (menuItems) {
		menuItems.classList.toggle('show');
	}
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
	// Desktop menu items
	const menuItems = document.querySelectorAll('.menu-item');
	menuItems.forEach(item => {
		item.addEventListener('click', function(e) {
			// Qui `this` è l'elemento cliccato
			if (this.classList.contains('logout-item')) return; // lascia fare il link logout

			e.preventDefault();
			const sectionId = this.getAttribute('data-section');
			if (sectionId) {
				showAccountSection(sectionId);
			}
		});
	});

	// Mobile navigation buttons
	const mobileButtons = document.querySelectorAll('.mobile-nav-btn');
	mobileButtons.forEach(button => {

		if (this.classList.contains('logout-item')) return;

		button.addEventListener('click', function() {


			const sectionId = this.getAttribute('data-section');
			if (sectionId) {
				showAccountSection(sectionId);
			}
		});
	});

	// Action buttons
	const actionButtons = document.querySelectorAll('[data-action]');
	actionButtons.forEach(button => {
		button.addEventListener('click', function() {
			const action = this.getAttribute('data-action');
			const id = this.getAttribute('data-id');
			handleAction(action, id);
		});
	});

	// Form submissions
	/*document.getElementById('form-info-personali')?.addEventListener('submit', function(e) {
		e.preventDefault();
		updatePersonalInfo();
	});

	document.getElementById('form-nuovo-indirizzo')?.addEventListener('submit', function(e) {
		e.preventDefault();
		addNewAddress();
	});

	document.getElementById('form-nuovo-pagamento')?.addEventListener('submit', function(e) {
		e.preventDefault();
		addNewPayment();
	});

	document.getElementById('form-password')?.addEventListener('submit', function(e) {
		e.preventDefault();
		changePassword();
	});*/

	// Show/Hide forms
	

	// Logout (both desktop and mobile)
	/*document.getElementById('logout-link')?.addEventListener('click', function(e) {
		e.preventDefault();
		logout();
	});

	document.getElementById('mobile-logout')?.addEventListener('click', function(e) {
		e.preventDefault();
		logout();
	});*/

	// Format card number and expiry date
	document.getElementById('numero-carta')?.addEventListener('input', function() {
		formatCardNumber(this);
	});

	document.getElementById('scadenza')?.addEventListener('input', function() {
		formatExpiryDate(this);
	});

	// Initialize first section
	showAccountSection('informazioni-personali');
});
