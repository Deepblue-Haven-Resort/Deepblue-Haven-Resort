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

    function getIconForType(type) {
        switch (type) {
            case "BOOKING": return "fa-solid fa-calendar-check";
            case "SERVICE": return "fa-solid fa-concierge-bell";
            case "TASK": return "fa-solid fa-list-check";
            case "PROMOTION": return "fa-solid fa-gift";
            case "SYSTEM": return "fa-solid fa-gear";
            default: return "fa-solid fa-bell";
        }
    }

    function escapeHtml(str) {
        if (!str) return "";
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
    }

    let notifications = [];
    let activeFilter = "all";

    async function fetchNotifications() {
        const container = document.getElementById("notificationsList");
        try {
            const response = await fetch(getApiUrl("/api/notifications"));
            if (!response.ok) {
                if (container) {
                    container.innerHTML = `
                    <div class="notifications-empty">
                        <i class="fa-solid fa-lock"></i>
                        Vui lòng đăng nhập để xem thông báo.
                    </div>
                `;
                }
                return;
            }
            const data = await response.json();
            notifications = data.notifications || [];
            render();
        } catch (e) {
            if (container) {
                container.innerHTML = `
                <div class="notifications-empty">
                    <i class="fa-solid fa-circle-exclamation"></i>
                    Không thể kết nối đến máy chủ. Vui lòng thử lại.
                </div>
            `;
            }
        }
    }

    function render() {
        const container = document.getElementById("notificationsList");
        if (!container) return;

        let filtered = notifications;
        if (activeFilter === "unread") {
            filtered = notifications.filter(n => !n.isRead);
        } else if (activeFilter !== "all") {
            filtered = notifications.filter(n => n.type === activeFilter);
        }

        if (!filtered.length) {
            container.innerHTML = `
            <div class="notifications-empty">
                <i class="fa-regular fa-bell-slash"></i>
                No notifications found.
            </div>
        `;
            return;
        }

        container.innerHTML = filtered.map(item => `
        <div class="notification-page-item ${item.isRead ? '' : 'unread'}" data-id="${item.id}" data-link="${item.link || ''}">
            <div class="notification-page-icon">
                <i class="${getIconForType(item.type)}"></i>
            </div>
            <div class="notification-page-body">
                <div class="notification-page-title">${escapeHtml(item.title)}</div>
                <div class="notification-page-msg">${escapeHtml(item.message)}</div>
                <div class="notification-page-time"><i class="fa-regular fa-clock"></i> ${item.timeAgo || ''}</div>
            </div>
        </div>
    `).join("");

        container.querySelectorAll(".notification-page-item").forEach(itemEl => {
            itemEl.addEventListener("click", async () => {
                const id = itemEl.dataset.id;
                const link = itemEl.dataset.link;

                try {
                    await fetch(getApiUrl(`/api/notifications/${id}/read`), { method: "POST" });
                } catch (e) { }

                if (link && link !== "null" && link !== "" && link !== "#") {
                    window.location.href = getApiUrl(link);
                } else {
                    fetchNotifications();
                }
            });
        });
    }

    document.querySelectorAll(".notifications-tab").forEach(tab => {
        tab.addEventListener("click", () => {
            document.querySelectorAll(".notifications-tab").forEach(t => t.classList.remove("is-active"));
            tab.classList.add("is-active");
            activeFilter = tab.dataset.filter || "all";
            render();
        });
    });

    document.getElementById("btnReadAll")?.addEventListener("click", async () => {
        try {
            await fetch(getApiUrl("/api/notifications/read-all"), { method: "POST" });
            fetchNotifications();
        } catch (e) { }
    });

    document.addEventListener("DOMContentLoaded", fetchNotifications);
})();
