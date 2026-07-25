document.addEventListener("DOMContentLoaded", () => {
    initPasswordToggle();
    initForgotPasswordFlow();
});

function initPasswordToggle() {
    const toggleButtons = document.querySelectorAll("[data-toggle-password]");

    toggleButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const input = document.getElementById(button.dataset.togglePassword);
            const icon = button.querySelector("i");

            if (!input || !icon) return;

            const isHidden = input.type === "password";
            input.type = isHidden ? "text" : "password";

            icon.classList.toggle("fa-eye", isHidden);
            icon.classList.toggle("fa-eye-slash", !isHidden);
        });
    });
}

function initForgotPasswordFlow() {
    const openForgotBtn = document.getElementById("openForgotPassword");

    const forgotModal = document.getElementById("forgotPasswordModal");
    const otpModal = document.getElementById("otpModal");
    const resetModal = document.getElementById("resetPasswordModal");

    const forgotPasswordForm = document.getElementById("forgotPasswordForm");
    const otpForm = document.getElementById("otpForm");
    const resetPasswordForm = document.getElementById("resetPasswordForm");

    const forgotIdentityInput = document.getElementById("forgotIdentity");
    const forgotIdentityError = document.getElementById("forgotIdentityError");
    const otpError = document.getElementById("otpError");
    const resetError = document.getElementById("resetPasswordError");

    const forgotPreviewValue = document.getElementById("forgotPreviewValue");
    const backToForgotModal = document.getElementById("backToForgotModal");
    const resendOtpBtn = document.getElementById("resendOtpBtn");

    const otpInputs = document.querySelectorAll(".auth-otp__input");

    let currentIdentity = "";

    if (!openForgotBtn || !forgotModal || !otpModal || !resetModal) 
        return; 

    openForgotBtn.addEventListener("click", () => {
        openModal(forgotModal);
        setTimeout(() => forgotIdentityInput?.focus(), 100);
    });

    document.querySelectorAll("[data-close-forgot]").forEach((button) => {
        button.addEventListener("click", () => {
            closeModal(forgotModal);
            resetForgotForm();
        });
    });

    document.querySelectorAll("[data-close-otp]").forEach((button) => {
        button.addEventListener("click", () => {
            closeModal(otpModal);
            resetOtpForm();
        });
    });

    document.querySelectorAll("[data-close-reset]").forEach((button) => {
        button.addEventListener("click", () => {
            closeModal(resetModal);
            resetResetForm();
        });
    });

    forgotPasswordForm?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const identity = forgotIdentityInput.value.trim();
        forgotIdentityError.textContent = "";

        if (!identity) {
            forgotIdentityError.textContent = "Please enter your email or phone number.";
            forgotIdentityInput.focus();
            return;
        }

        if (!isValidEmailOrPhone(identity)) {
            forgotIdentityError.textContent = "Please enter a valid email or phone number.";
            forgotIdentityInput.focus();
            return;
        }

        const submitBtn = forgotPasswordForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Sending...';

        try {
            const response = await fetch('/deepbluehaven/api/auth/forgot-password', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ identity: identity })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || "Account not found or network error.");
            }

            currentIdentity = identity;
            forgotPreviewValue.textContent = identity;
            
            closeModal(forgotModal);
            openModal(otpModal);
            setTimeout(() => otpInputs[0]?.focus(), 100);

        } catch (error) {
            forgotIdentityError.textContent = error.message;
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Send code <i class="fa-solid fa-arrow-right"></i>';
        }
    });

    backToForgotModal?.addEventListener("click", () => {
        closeModal(otpModal);
        openModal(forgotModal);
        setTimeout(() => forgotIdentityInput?.focus(), 100);
    });

    resendOtpBtn?.addEventListener("click", async () => {
        otpError.textContent = "";
        resendOtpBtn.disabled = true;
        resendOtpBtn.textContent = "Sending...";
        
        try {
            const response = await fetch('/deepbluehaven/api/auth/forgot-password', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ identity: currentIdentity })
            });
            if (!response.ok) 
                throw new Error();
            if (typeof showToast === 'function') {
                showToast("success", "Success", "A new OTP code has been sent.");
            } 
            else {
                alert("A new OTP code has been sent.");
            }
        } catch (error) {
            otpError.textContent = "Failed to resend OTP. Please try again.";
        } finally {
            resendOtpBtn.disabled = false;
            resendOtpBtn.textContent = "Resend code";
        }
    });

    otpInputs.forEach((input, index) => {
        input.addEventListener("input", () => {
            input.value = input.value.replace(/\D/g, "").slice(0, 1);
            if (input.value && index < otpInputs.length - 1) {
                otpInputs[index + 1].focus();
            }
        });

        input.addEventListener("keydown", (event) => {
            if (event.key === "Backspace" && !input.value && index > 0) {
                otpInputs[index - 1].focus();
            }
        });

        input.addEventListener("paste", (event) => {
            event.preventDefault();
            const pasted = event.clipboardData.getData("text").replace(/\D/g, "").slice(0, otpInputs.length);
            pasted.split("").forEach((char, i) => {
                if (otpInputs[i]) {
                    otpInputs[i].value = char;
                }
            });
            const focusIndex = Math.min(pasted.length, otpInputs.length - 1);
            otpInputs[focusIndex]?.focus();
        });
    });

    otpForm?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const otpValue = Array.from(otpInputs).map((input) => input.value).join("");
        otpError.textContent = "";

        if (otpValue.length !== 6) {
            otpError.textContent = "Please enter the full 6-digit OTP code.";
            return;
        }

        const submitBtn = otpForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Verifying...';

        try {
            const response = await fetch('/deepbluehaven/api/auth/verify-otp', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ identity: currentIdentity, otp: otpValue })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || "Invalid or expired OTP.");
            }

            closeModal(otpModal);
            openModal(resetModal);
            setTimeout(() => document.getElementById("newPassword")?.focus(), 100);

        } catch (error) {
            otpError.textContent = error.message;
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = 'Verify OTP <i class="fa-solid fa-check"></i>';
        }
    });

    resetPasswordForm?.addEventListener("submit", async (event) => {
        event.preventDefault();

        const newPassword = document.getElementById("newPassword").value;
        const confirmPassword = document.getElementById("confirmNewPassword").value;
        
        if (resetError) resetError.textContent = "";

        if (newPassword !== confirmPassword) {
            if (resetError) resetError.textContent = "Passwords do not match!";
            return;
        }

        const submitBtn = resetPasswordForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Resetting...';

        try {
            const response = await fetch('/deepbluehaven/api/auth/reset-password', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify({ identity: currentIdentity, newPassword: newPassword })
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || "Failed to reset password.");
            }

            if (typeof showToast === 'function') {
                showToast("success", "Success", "Password reset successfully! Please login.");
            } 
            else {
                alert("Password reset successfully! Please login with your new password.");
            }
            closeModal(resetModal);
            resetForgotForm();
            resetOtpForm();
            resetResetForm();
            window.location.reload(); 

        } catch (error) {
            if (resetError) resetError.textContent = error.message;
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerText = 'Reset Password';
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") return;

        if (forgotModal.classList.contains("is-open")) {
            closeModal(forgotModal);
            resetForgotForm();
        }

        if (otpModal.classList.contains("is-open")) {
            closeModal(otpModal);
            resetOtpForm();
        }

        if (resetModal.classList.contains("is-open")) {
            closeModal(resetModal);
            resetResetForm();
        }
    });

    function openModal(modal) {
        modal.classList.add("is-open");
        modal.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
    }

    function closeModal(modal) {
        modal.classList.remove("is-open");
        modal.setAttribute("aria-hidden", "true");

        const hasOpenModal = document.querySelector(".auth-modal.is-open");
        if (!hasOpenModal) {
            document.body.style.overflow = "";
        }
    }

    function resetForgotForm() {
        forgotPasswordForm?.reset();
        if (forgotIdentityError) {
            forgotIdentityError.textContent = "";
        }
    }

    function resetOtpForm() {
        otpForm?.reset();

        if (otpError) {
            otpError.textContent = "";
        }

        otpInputs.forEach((input) => {
            input.value = "";
        });
    }

    function resetResetForm() {
        resetPasswordForm?.reset();
        if (resetError) resetError.textContent = "";
    }

    function isValidEmailOrPhone(value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const phoneRegex = /^[0-9+\s().-]{8,20}$/;

        return emailRegex.test(value) || phoneRegex.test(value);
    }

    function getHeaders() {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }
        return headers;
    }
}