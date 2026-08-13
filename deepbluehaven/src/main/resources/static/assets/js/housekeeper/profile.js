(function () {
    function getApiUrl(path) {
        let contextPath = document.querySelector('meta[name="_context_path"]')?.content;
        if (!contextPath || contextPath === "/") {
            const match = window.location.pathname.match(/^\/([^\/]+)/);
            if (match && match[1] === "deepbluehaven") {
                contextPath = "/" + match[1];
            } else {
                contextPath = "";
            }
        }
        return contextPath.replace(/\/$/, "") + (path.startsWith("/") ? path : "/" + path);
    }

    function initProfile() {
        const avatarWrapper = document.querySelector(".profile-avatar-wrapper");
        if (avatarWrapper) {
            avatarWrapper.addEventListener("click", function () {
                this.classList.toggle("active");
            });
        }
    }

    document.addEventListener("DOMContentLoaded", initProfile);
})();
