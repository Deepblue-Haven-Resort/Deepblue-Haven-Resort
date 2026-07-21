document.querySelectorAll("[data-toggle-password]").forEach((button) => {
        button.addEventListener("click", () => {
            const input = document.getElementById(button.dataset.togglePassword);
            const icon = button.querySelector("i");

            if (!input) return;

            const isHidden = input.type === "password";
            input.type = isHidden ? "text" : "password";

            icon.classList.toggle("fa-eye", isHidden);
            icon.classList.toggle("fa-eye-slash", !isHidden);
        });
        const urlParams = new URLSearchParams(window.location.search);
    
    if (urlParams.has("error")) {
        if (typeof showToast === "function") {
            showToast("error", "Login Failed", "Username or password is incorrect.");
        }
    }
    
    if (urlParams.has("logout")) {
        if (typeof showToast === "function") {
            showToast("success", "Logged Out", "You have logged out successfully.");
        }
    }
});    