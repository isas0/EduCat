// Format card number input
function formatCardNumber(input) {
	var value = input.value.replace(/\s+/g, '').replace(/[^0-9]/g, '');
	var groups = value.match(/.{1,4}/g);
	var formattedValue = groups ? groups.join(' ') : value;
	input.value = formattedValue;
}

// Format expiry date input
function formatExpiryDate(input) {
	var value = input.value.replace(/\D/g, '');
	if (value.length >= 2) {
		value = value.substring(0, 2) + '/' + value.substring(2, 4);
	}
	input.value = value;
}

// Menu toggle function for mobile (navbar)
function menutoggle() {
	var menuItems = document.getElementById('menuItems');
	if (menuItems) {
		menuItems.classList.toggle('show');
	}
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
	// Desktop menu items
	var menuItems = document.querySelectorAll('.menu-item');
	for (var i = 0; i < menuItems.length; i++) {
		menuItems[i].addEventListener('click', function (e) {
			
			if (!this.classList.contains('logout-item')) 
			
			e.preventDefault();
			var sectionId = this.getAttribute('data-section');
			if (sectionId) {
				showAccountSection(sectionId);
			}
		});
	}

	// Mobile navigation buttons
	var mobileButtons = document.querySelectorAll('.mobile-nav-btn');
	for (var i = 0; i < mobileButtons.length; i++) {
		mobileButtons[i].addEventListener('click', function () {
			var sectionId = this.getAttribute('data-section');
			if (sectionId) {
				showAccountSection(sectionId);
			}
		});
	}

	// Action buttons
	var actionButtons = document.querySelectorAll('[data-action]');
	for (var i = 0; i < actionButtons.length; i++) {
		actionButtons[i].addEventListener('click', function () {
			
			
			if (this.classList.contains('logout-item')) return;
			
			var action = this.getAttribute('data-action');
			var id = this.getAttribute('data-id');
			handleAction(action, id);
		});
	}

	
	
	// Show/Hide address form
	var btnShowAddress = document.getElementById('btn-show-add-address');
	if (btnShowAddress) {
		btnShowAddress.addEventListener('click', function () {
			document.getElementById('add-address-form').style.display = 'block';
			this.style.display = 'none';
		});
	}

	var btnCancelAddress = document.getElementById('btn-cancel-address');
	if (btnCancelAddress) {
		btnCancelAddress.addEventListener('click', function () {
			document.getElementById('add-address-form').style.display = 'none';
			var showBtn = document.getElementById('btn-show-add-address');
			if (showBtn) showBtn.style.display = 'block';
			var form = document.getElementById('form-nuovo-indirizzo');
			if (form) form.reset();
		});
	}

	// Show/Hide payment form
	var btnShowPayment = document.getElementById('btn-show-add-payment');
	if (btnShowPayment) {
		btnShowPayment.addEventListener('click', function () {
			document.getElementById('add-payment-form').style.display = 'block';
			this.style.display = 'none';
		});
	}

	var btnCancelPayment = document.getElementById('btn-cancel-payment');
	if (btnCancelPayment) {
		btnCancelPayment.addEventListener('click', function () {
			document.getElementById('add-payment-form').style.display = 'none';
			var showBtn = document.getElementById('btn-show-add-payment');
			if (showBtn) showBtn.style.display = 'block';
			var form = document.getElementById('form-nuovo-pagamento');
			if (form) form.reset();
		});
	}

	// Logout buttons
	/*var logoutLink = document.getElementById('logout-link');
	if (logoutLink) {
		logoutLink.addEventListener('click', function (e) {
			e.preventDefault();
			logout();
		});
	}

	var mobileLogout = document.getElementById('mobile-logout');
	if (mobileLogout) {
		mobileLogout.addEventListener('click', function (e) {
			e.preventDefault();
			logout();
		});
	}*/

	// Format card number and expiry date
	var numeroCarta = document.getElementById('numero-carta');
	if (numeroCarta) {
		numeroCarta.addEventListener('input', function () {
			formatCardNumber(this);
		});
	}

	var scadenza = document.getElementById('scadenza');
	if (scadenza) {
		scadenza.addEventListener('input', function () {
			formatExpiryDate(this);
		});
	}

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
	
	// Initial section
	showAccountSection('informazioni-personali');
	
});
