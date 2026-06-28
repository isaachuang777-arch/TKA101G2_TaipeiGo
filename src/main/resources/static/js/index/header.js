document.addEventListener("DOMContentLoaded", loadCartCount);

async function loadCartCount() {
    const badge = document.getElementById("cartCount");
    if (!badge) {
        return;
    }
    try {
        const response = await fetch("/frontend/cart/count");
        if (!response.ok) return;
        const count = await response.json();
        if (count > 0) {
            badge.textContent = count;
            badge.style.display = "flex";
        } else {
            badge.style.display = "none";
        }
    } catch (e) {
        console.log(e);
    }
}