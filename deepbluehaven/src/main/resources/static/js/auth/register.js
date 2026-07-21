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
    
const registerForm = document.querySelector(".auth-form");
 
if (registerForm) {
    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
 
        clearErrors();
 
        const payload = {
            username: registerForm.username.value.trim(),
            fullName: registerForm.fullName.value.trim(),
            contact: registerForm.contact.value.trim(),
            password: registerForm.password.value,
            confirmPassword: registerForm.confirmPassword.value,
            terms: registerForm.terms.checked,
        };
 
        const submitBtn = registerForm.querySelector(".auth-submit");
        submitBtn.disabled = true;
 
        try {
            const response = await fetch(getApiUrl("register"), {
                method: "POST",
                headers: getHeaders(),
                body: JSON.stringify(payload),
            });
 
            const data = await response.json();
 
            if (response.ok) {
                showToast("success", "Registration successful", "Redirecting you to the login page...");
                setTimeout(() => {
                    window.location.href = "/deepbluehaven/home";
                }, 1500); 
                return;
            }
 
            if (Array.isArray(data.errors)) {
                showFieldErrors(data.errors);
            } else if (data.message) {
                showGeneralError(data.message);
            } else {
                showGeneralError("Something went wrong. Please try again.");
            }
        } catch (err) {
            showGeneralError("Network error. Please check your connection and try again.");
        } finally {
            submitBtn.disabled = false;
        }
    });
}
 
function getApiUrl(path) {
    const contextPath = document.querySelector('meta[name="_context_path"]')?.content || "/";
    return contextPath + path;
}
 
function getHeaders() {
    const token = document.querySelector('meta[name="_csrf"]')?.content;
    const header = document.querySelector('meta[name="_csrf_header"]')?.content;
 
    const headers = { "Content-Type": "application/json" };
    if (token && header) headers[header] = token;
 
    return headers;
}
 
function showFieldErrors(errors) {
    errors.forEach(({ field, defaultMessage }) => {
        const targetField = field === "passwordMatching" ? "confirmPassword" : field;
        const input = document.getElementById(targetField);
        if (!input) return;
 
        const errorEl = document.createElement("span");
        errorEl.className = "auth-field__error";
        errorEl.textContent = defaultMessage;
 
        input.closest(".auth-field, .auth-checkbox")?.appendChild(errorEl);
    });
}
 
function showGeneralError(message) {
    const alertEl = document.createElement("div");
    alertEl.className = "auth-alert auth-alert--error";
    alertEl.id = "registerGeneralError";
    alertEl.textContent = message;
    registerForm.prepend(alertEl);
}
 
function clearErrors() {
    document.querySelectorAll(".auth-field__error").forEach((el) => el.remove());
    document.getElementById("registerGeneralError")?.remove();
}
 