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
            case "TASK": 
                return "fa-solid fa-broom";
            case "SERVICE": 
                return "fa-solid fa-bell-concierge";
            case "SYSTEM": 
                return "fa-solid fa-gear";
            default: 
                return "fa-solid fa-bell";
        }
    }

    function escapeHtml(str) {
        if (!str) return "";
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
    }

    async function fetchWorkerNotifications() {
        const container = document.getElementById("workerNotifyList");
        try {
            const response = await fetch(getApiUrl("/api/notifications"));
            if (!response.ok) {
                if (container) {
                    container.innerHTML = `
                        <div class="worker-notify-empty">
                            <i class="fa-solid fa-lock"></i>
                            Vui lòng đăng nhập tài khoản nhân viên để xem thông báo.
                        </div>
                    `;
                }
                return;
            }
            const data = await response.json();
            const items = data.notifications || [];

            if (!items.length) {
                container.innerHTML = `
                    <div class="worker-notify-empty">
                        <i class="fa-regular fa-bell-slash"></i>
                        No notifications found.
                    </div>
                `;
                return;
            }

            container.innerHTML = items.map(item => `
                <div class="worker-notify-item ${item.isRead ? '' : 'unread'}" data-id="${item.id}" data-link="${item.link || ''}">
                    <div class="worker-notify-icon">
                        <i class="${getIconForType(item.type)}"></i>
                    </div>
                    <div class="worker-notify-body">
                        <div class="worker-notify-title">${escapeHtml(item.title)}</div>
                        <div class="worker-notify-msg">${escapeHtml(item.message)}</div>
                        <div class="worker-notify-time"><i class="fa-regular fa-clock"></i> ${item.timeAgo || ''}</div>
                    </div>
                </div>
            `).join("");

            container.querySelectorAll(".worker-notify-item").forEach(itemEl => {
                itemEl.addEventListener("click", async () => {
                    const id = itemEl.dataset.id;
                    const link = itemEl.dataset.link;

                    try {
                        await fetch(getApiUrl(`/api/notifications/${id}/read`), { method: "POST" });
                    } catch (e) {}

                    if (link && link !== "null" && link !== "" && link !== "#") {
                        window.location.href = getApiUrl(link);
                    } else {
                        fetchWorkerNotifications();
                    }
                });
            });

        } catch (e) {
            if (container) {
                container.innerHTML = `
                    <div class="worker-notify-empty">
                        <i class="fa-solid fa-exclamation-circle"></i>
                        Could not load notifications.
                    </div>
                `;
            }
        }
    }

    document.getElementById("btnReadAllWorker")?.addEventListener("click", async () => {
        try {
            await fetch(getApiUrl("/api/notifications/read-all"), { method: "POST" });
            fetchWorkerNotifications();
        } catch (e) {}
    });

    document.addEventListener("DOMContentLoaded", fetchWorkerNotifications);
})();
