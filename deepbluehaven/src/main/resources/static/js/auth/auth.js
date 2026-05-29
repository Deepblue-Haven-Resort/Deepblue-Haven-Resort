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

    const forgotPasswordForm = document.getElementById("forgotPasswordForm");
    const otpForm = document.getElementById("otpForm");

    const forgotIdentityInput = document.getElementById("forgotIdentity");
    const forgotIdentityError = document.getElementById("forgotIdentityError");
    const otpError = document.getElementById("otpError");

    const forgotPreviewValue = document.getElementById("forgotPreviewValue");
    const backToForgotModal = document.getElementById("backToForgotModal");
    const resendOtpBtn = document.getElementById("resendOtpBtn");

    const otpInputs = document.querySelectorAll(".auth-otp__input");

    if (!openForgotBtn || !forgotModal || !otpModal) return;

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

    forgotPasswordForm?.addEventListener("submit", (event) => {
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

        forgotPreviewValue.textContent = identity;

        closeModal(forgotModal);
        openModal(otpModal);

        setTimeout(() => otpInputs[0]?.focus(), 100);
    });

    backToForgotModal?.addEventListener("click", () => {
        closeModal(otpModal);
        openModal(forgotModal);

        setTimeout(() => forgotIdentityInput?.focus(), 100);
    });

    resendOtpBtn?.addEventListener("click", () => {
        otpError.textContent = "";
        alert("A new OTP code has been sent.");
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

            const pasted = event.clipboardData
                .getData("text")
                .replace(/\D/g, "")
                .slice(0, otpInputs.length);

            pasted.split("").forEach((char, i) => {
                if (otpInputs[i]) {
                    otpInputs[i].value = char;
                }
            });

            const focusIndex = Math.min(pasted.length, otpInputs.length - 1);
            otpInputs[focusIndex]?.focus();
        });
    });

    otpForm?.addEventListener("submit", (event) => {
        event.preventDefault();

        const otpValue = Array.from(otpInputs)
            .map((input) => input.value)
            .join("");

        otpError.textContent = "";

        if (otpValue.length !== 6) {
            otpError.textContent = "Please enter the full 6-digit OTP code.";
            return;
        }

        alert("OTP verified successfully!");

        closeModal(otpModal);
        resetForgotForm();
        resetOtpForm();
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

    function isValidEmailOrPhone(value) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        const phoneRegex = /^[0-9+\s().-]{8,20}$/;

        return emailRegex.test(value) || phoneRegex.test(value);
    }
}