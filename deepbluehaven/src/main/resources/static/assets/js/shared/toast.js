function showToast(type = "info", title = "Information", message = "", options = {}) {
    const container = document.getElementById("toast-container");

    if (!container) return;

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