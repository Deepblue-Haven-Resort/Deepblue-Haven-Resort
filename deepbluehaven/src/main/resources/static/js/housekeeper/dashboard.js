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

    function initDashboard() {
        const searchInput = document.querySelector(".filter-bar .search-box input");
        const tableRows = document.querySelectorAll(".account-table tbody tr");

        if (searchInput && tableRows.length) {
            searchInput.addEventListener("input", function () {
                const keyword = this.value.toLowerCase().trim();
                tableRows.forEach(row => {
                    const text = row.textContent.toLowerCase();
                    row.style.display = text.includes(keyword) ? "" : "none";
                });
            });
        }

        const btnRefresh = document.querySelector(".panel-header button");
        if (btnRefresh) {
            btnRefresh.addEventListener("click", function () {
                const icon = this.querySelector("i");
                if (icon) icon.classList.add("fa-spin");
                setTimeout(() => window.location.reload(), 300);
            });
        }
    }

    document.addEventListener("DOMContentLoaded", initDashboard);
})();
