
//checkout
document.addEventListener("DOMContentLoaded", function() {
	const form = document.getElementById("checkoutForm");


	const fields = {
		cap: {
			regex: /^\d{5}$/,
			message: "Il CAP deve contenere esattamente 5 cifre.",
		},
		cardNumber: {
			regex: /^\d{16}$/,
			message: "Il numero di carta deve contenere 16 cifre.",
		},
		scadenza: {
			regex: /^(0[1-9]|1[0-2])\/\d{2}$/,
			message: "La scadenza deve essere nel formato MM/AA.",
		},
		cvv: {
			regex: /^\d{3}$/,
			message: "Il CVV deve contenere esattamente 3 cifre.",
		}
	};

	for (const fieldName in fields) {
		const input = form[fieldName];
		const errorDiv = document.getElementById(`${fieldName}-error`);
		input.addEventListener("change", function() {
			validateField(input, fields[fieldName], errorDiv);
		});
	}

	form.addEventListener("submit", function(e) {
		let valid = true;
		for (const fieldName in fields) {
			const input = form[fieldName];
			const errorDiv = document.getElementById(`${fieldName}-error`);
			const isValid = validateField(input, fields[fieldName], errorDiv);
			if (!isValid) valid = false;
		}

		if (!valid) e.preventDefault();
	});

	function validateField(input, rule, errorDiv) {
		const value = input.value.trim().replace(/\s/g, "");
		if (!rule.regex.test(value)) {
			errorDiv.textContent = rule.message;
			input.classList.add("invalid");
			return false;
		} else {
			errorDiv.textContent = "";
			input.classList.remove("invalid");
			return true;
		}
	}
});

//login e register
document.addEventListener("DOMContentLoaded", () => {

	function showError(elementId, message) {
		document.getElementById(elementId).textContent = message;
	}

	function clearError(elementId) {
		document.getElementById(elementId).textContent = "";
	}

	const loginForm = document.getElementById("loginForm");
	const loginUsername = document.getElementById("login-username");
	const loginPassword = document.getElementById("login-password");

	loginUsername.addEventListener("change", () => {
		if (loginUsername.value.trim() === "") {
			showError("login-error", "Inserisci username.");
		} else {
			clearError("login-error");
		}
	});

	loginPassword.addEventListener("change", () => {
		if (loginPassword.value.trim() === "") {
			showError("login-error", "Inserisci password.");
		} else {
			clearError("login-error");
		}
	});

	loginForm.addEventListener("submit", (e) => {
		if (loginUsername.value.trim() === "" || loginPassword.value.trim() === "") {
			e.preventDefault();
			showError("login-error", "Tutti i campi del login sono obbligatori.");
		}
	});

	// Validazione Registrazione
	const regForm = document.getElementById("regForm");
	const regEmail = document.getElementById("register-email");
	const regUsername = document.getElementById("register-username");
	const regNome = document.getElementById("register-nome");
	const regCognome = document.getElementById("register-cognome");
	const regPassword = document.getElementById("register-password");

	const regInputs = [regEmail, regUsername, regNome, regCognome, regPassword];

	regInputs.forEach(input => {
		input.addEventListener("change", () => {
			if (input.value.trim() === "") {
				showError("register-error", "Tutti i campi sono obbligatori.");
			} else {
				clearError("register-error");
			}
		});
	});

	regForm.addEventListener("submit", (e) => {
		let valid = true;
		regInputs.forEach(input => {
			if (input.value.trim() === "") valid = false;
		});

		if (!valid) {
			e.preventDefault();
			showError("register-error", "Compila tutti i campi per registrarti.");
		}
	});

});


//filtro ordini utente
document.getElementById("filter-by-user-and-date").addEventListener("click", function() {

	function showError(elementId, message) {
		document.getElementById(elementId).textContent = message;
	}

	function clearError(elementId) {
		document.getElementById(elementId).textContent = "";
	}

	const dataInizio = document.getElementById("data-inizio").value;
	const dataFine = document.getElementById("data-fine").value;

	if (!dataInizio || !dataFine ) {
		showError("filter-error", "Compila tutti i campi delle date per filtrare.");
		return;
	} else {
		clearError("filter-error");
	}


});



