document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("form");

    form.addEventListener("submit", (e) => {
        const password = document.getElementById("password").value;
        const confirmPassword = document.getElementById("confirmPassword").value;

        if (password !== confirmPassword) {
            e.preventDefault();
            const errorMessage = document.getElementById("errorMessage");
            errorMessage.textContent = "Passwords do not match.";
            errorMessage.style.display = "block";
        }
    });

    // Check for backend error messages on page load
    const urlParams = new URLSearchParams(window.location.search);
    const errorMessage = urlParams.get("error");

    if (errorMessage) {
        const errorDiv = document.getElementById("errorMessage");
        errorDiv.textContent = decodeURIComponent(errorMessage);
        errorDiv.style.display = "block";

        // Remove the error query parameter from the URL without reloading the page
        const newUrl = `${window.location.origin}${window.location.pathname}`;
        window.history.replaceState({}, document.title, newUrl);
    }
});
