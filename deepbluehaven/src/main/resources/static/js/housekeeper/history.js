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

    function initTaskHistory() {
        const btnExport = document.querySelector(".panel-header a");
        if (btnExport) {
            btnExport.addEventListener("click", function () {
                const icon = this.querySelector("i");
                if (icon) {
                    icon.className = "fa-solid fa-spinner fa-spin";
                    setTimeout(() => {
                        icon.className = "fa-solid fa-file-export";
                    }, 1500);
                }
            });
        }
    }

    document.addEventListener("DOMContentLoaded", initTaskHistory);
})();
