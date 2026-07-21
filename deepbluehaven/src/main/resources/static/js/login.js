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

    const loginForm = document.getElementById("form-login");

    if (loginForm) {
        loginForm.addEventListener("submit", async (event) => {
            event.preventDefault(); 

            const submitBtn = loginForm.querySelector(".auth-submit");
            if (submitBtn) submitBtn.disabled = true;

            const formData = new URLSearchParams();
            formData.append("username", loginForm.username.value.trim());
            formData.append("password", loginForm.password.value);

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
            
            const headers = { "Content-Type": "application/x-www-form-urlencoded" };
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            try {
                const response = await fetch(loginForm.action, {
                    method: "POST",
                    headers: headers,
                    body: formData.toString()
                });

                if (response.redirected && response.url.includes("error")) {
                    if (typeof showToast === "function") {
                        showToast("error", "Login Failed", "Username or password is incorrect.");
                    }
                    if (submitBtn) submitBtn.disabled = false;
                } else {
                    if (typeof showToast === "function") {
                        showToast("success", "Login Successful", "Redirecting you to the home page...");
                    }
                    setTimeout(() => {
                        window.location.href = response.url || "/deepbluehaven/home";
                    }, 1500);
                }
            } catch (err) {
                loginForm.submit();
            }
        });
    }
});