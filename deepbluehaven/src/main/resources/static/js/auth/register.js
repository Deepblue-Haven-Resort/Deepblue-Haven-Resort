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
    });