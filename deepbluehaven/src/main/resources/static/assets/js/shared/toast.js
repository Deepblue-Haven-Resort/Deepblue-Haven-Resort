function showToast(type = "info", title = "Information", message = "", options = {}) {
    let container = document.getElementById("toast-container");

    if (!container) {
        container = document.createElement("div");
        container.id = "toast-container";
        document.body.appendChild(container);
    }

    const toast = document.createElement("div");
    const theme = options.theme === "dark" ? "toast-dark" : "";

    toast.className = `toast toast-${type} ${theme}`;

    toast.innerHTML = `
        <div class="toast-icon">
            <i class="fa-solid ${getToastIcon(type)}"></i>
        </div>

        <div class="toast-body">
            <strong class="toast-title">${title}</strong>
            <p class="toast-message">${message}</p>
        </div>

        <button class="toast-close" type="button" aria-label="Close toast">
            <i class="fa-solid fa-xmark"></i>
        </button>
    `;

    container.appendChild(toast);

    toast.querySelector(".toast-close").addEventListener("click", function () {
        removeToast(toast);
    });

    const duration = options.duration ?? 4000;

    if (duration !== false) {
        setTimeout(function () {
            removeToast(toast);
        }, duration);
    }
}

function removeToast(toast) {
    toast.classList.add("hide");

    setTimeout(function () {
        toast.remove();
    }, 280);
}

function getToastIcon(type) {
    switch (type) {
        case "success":
            return "fa-circle-check";
        case "error":
            return "fa-circle-xmark";
        case "warning":
            return "fa-triangle-exclamation";
        case "info":
        default:
            return "fa-circle-info";
    }
}

document.addEventListener("DOMContentLoaded", function () {
    const container = document.getElementById("toast-container");
    if (!container) return;

    const successMsg = container.getAttribute("data-flash-success");
    const errorMsg = container.getAttribute("data-flash-error");
    const infoMsg = container.getAttribute("data-flash-info");

    if (successMsg && successMsg.trim() !== "") {
        showToast("success", "Success", successMsg.trim());
    }
    if (errorMsg && errorMsg.trim() !== "") {
        showToast("error", "Notice", errorMsg.trim());
    }
    if (infoMsg && infoMsg.trim() !== "") {
        showToast("info", "Information", infoMsg.trim());
    }
});