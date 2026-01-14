

document.addEventListener("DOMContentLoaded", () => {
	const searchbar = document.getElementById("searchbar");
	const category = document.getElementById("category");
	const container = document.querySelector(".row-products");

	/*if (!searchbar || !category || !container) {
		console.error("Elemento mancante:", { searchbar });
		return;
	}*/

	function loadFilteredProducts() {
		const selectedCategory = category.value;

		const xhr = new XMLHttpRequest();
		xhr.open("GET", "index?category=" + encodeURIComponent(selectedCategory), true);

		xhr.onreadystatechange = function() {
			if (xhr.readyState === 4 && xhr.status === 200) {
				document.querySelector(".row-products").innerHTML = xhr.responseText;
			}
		};

		xhr.send();

	}
	category.addEventListener("change", loadFilteredProducts);
});

