document.addEventListener("DOMContentLoaded", () => {
    const passwordToggleButtons = document.querySelectorAll("[data-toggle-password]");

    passwordToggleButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const inputId = button.getAttribute("data-toggle-password");
            const input = document.getElementById(inputId);
            const icon = button.querySelector("i");

            if (!input) return;

            const isPassword = input.type === "password";
            input.type = isPassword ? "text" : "password";

            if (icon) {
                icon.classList.toggle("fa-eye", isPassword);
                icon.classList.toggle("fa-eye-slash", !isPassword);
            }
        });
    });
});