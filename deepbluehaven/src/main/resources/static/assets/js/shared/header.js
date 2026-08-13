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

  let allNotifications = [];
  let currentFilter = "all";

  async function loadNotifications() {
    const notifyBadge = document.getElementById("notifyBadge");
    const notifyList = document.getElementById("notifyList");

    try {
      const response = await fetch(getApiUrl("/api/notifications"));
      if (!response.ok) return;

      const data = await response.json();
      allNotifications = data.notifications || [];
      const unreadCount = data.unreadCount || 0;

      if (notifyBadge) {
        if (unreadCount > 0) {
          notifyBadge.textContent = unreadCount > 99 ? "99+" : unreadCount;
          notifyBadge.classList.add("show");
        } else {
          notifyBadge.textContent = "";
          notifyBadge.classList.remove("show");
        }
      }

      renderNotificationList(notifyList);
    } catch (e) { }
  }

  function renderNotificationList(container) {
    if (!container) return;

    const filtered = currentFilter === "unread"
      ? allNotifications.filter(n => !n.isRead)
      : allNotifications;

    if (!filtered.length) {
      container.innerHTML = `<div class="notify-empty">Không có thông báo nào</div>`;
      return;
    }

    container.innerHTML = filtered.map(item => `
      <div class="notify-item ${item.isRead ? '' : 'unread'}" data-id="${item.id}" data-link="${item.link || ''}">
        <div class="notify-icon"><i class="${getIconForType(item.type)}"></i></div>
        <div class="notify-content">
          <div class="notify-item-title">${escapeHtml(item.title)}</div>
          <div class="notify-item-msg">${escapeHtml(item.message)}</div>
          <div class="notify-item-time">${item.timeAgo || ''}</div>
        </div>
      </div>
    `).join("");

    container.querySelectorAll(".notify-item").forEach(itemEl => {
      itemEl.addEventListener("click", async () => {
        const id = itemEl.dataset.id;
        const link = itemEl.dataset.link;

        try {
          await fetch(getApiUrl(`/api/notifications/${id}/read`), { method: "POST" });
        } catch (e) { }

        if (link && link !== "null" && link !== "" && link !== "#") {
          window.location.href = getApiUrl(link);
        } else {
          loadNotifications();
        }
      });
    });
  }

  function updateActiveHeader() {
    const currentPath = window.location.pathname.replace(/\/$/, "");
    const menuLinks = document.querySelectorAll("#menu .header-link");
    if (menuLinks.length) {
      const homeLink = Array.from(menuLinks).find((link) => {
        const path = new URL(link.href, window.location.origin).pathname.replace(/\/$/, "");
        const rawHref = link.getAttribute("href");
        return path === "" || path.endsWith("/home") || rawHref === "/" || rawHref === "/home";
      }) || menuLinks[0];

      const homePath = homeLink ? new URL(homeLink.href, window.location.origin).pathname.replace(/\/$/, "") : "";
      let matched = false;

      menuLinks.forEach((link) => {
        const href = link.getAttribute("href");
        if (!href) return;

        try {
          const linkPath = new URL(link.href, window.location.origin).pathname.replace(/\/$/, "");
          const isHomeLink = (link === homeLink) || linkPath === homePath || linkPath === "" || linkPath.endsWith("/home");

          let isActive = false;

          if (isHomeLink) {
            isActive = (currentPath === homePath) || (currentPath === homePath + "/home") || (currentPath === "");
          } else {
            isActive = (currentPath === linkPath) || (linkPath !== "" && currentPath.startsWith(linkPath + "/"));
          }

          if (isActive) {
            link.classList.add("is-active");
            matched = true;
          } else {
            link.classList.remove("is-active");
          }
        } catch (e) { }
      });

      if (!matched && (currentPath.includes("/profile") || currentPath.includes("/booking") || currentPath.includes("/favorites"))) {
        menuLinks.forEach((link) => link.classList.remove("is-active"));
      }
    }

    const dropdownLinks = document.querySelectorAll("#userDropdown a");
    dropdownLinks.forEach((link) => {
      const href = link.getAttribute("href");
      if (!href) return;
      try {
        const linkPath = new URL(link.href, window.location.origin).pathname.replace(/\/$/, "");
        if (currentPath === linkPath) {
          link.classList.add("is-active");
        } else {
          link.classList.remove("is-active");
        }
      } catch (e) { }
    });
  }

  function initHeaderEvents() {
    const notifyBtn = document.getElementById("notifyBtn");
    const notifyPop = document.getElementById("notifyPop");
    const notifyClose = document.getElementById("notifyClose");

    if (notifyBtn && notifyPop) {
      notifyBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        const isOpen = notifyPop.classList.contains("show");
        notifyPop.classList.toggle("show", !isOpen);
        notifyBtn.setAttribute("aria-expanded", (!isOpen).toString());
        if (!isOpen) {
          loadNotifications();
        }
      });

      if (notifyClose) {
        notifyClose.addEventListener("click", () => {
          notifyPop.classList.remove("show");
          notifyBtn.setAttribute("aria-expanded", "false");
        });
      }

      document.addEventListener("click", (e) => {
        if (!notifyPop.contains(e.target) && !notifyBtn.contains(e.target)) {
          notifyPop.classList.remove("show");
          notifyBtn.setAttribute("aria-expanded", "false");
        }
      });

      const notifyMarkAll = document.getElementById("notifyMarkAll");
      if (notifyMarkAll) {
        notifyMarkAll.addEventListener("click", async () => {
          try {
            await fetch(getApiUrl("/api/notifications/read-all"), { method: "POST" });
            loadNotifications();
          } catch (e) { }
        });
      }

      const tabs = notifyPop.querySelectorAll(".notify-tab");
      tabs.forEach(tab => {
        tab.addEventListener("click", () => {
          tabs.forEach(t => t.classList.remove("is-active"));
          tab.classList.add("is-active");
          currentFilter = tab.dataset.filter || "all";
          renderNotificationList(document.getElementById("notifyList"));
        });
      });
    }

    loadNotifications();
  }

  function initHeader() {
    updateActiveHeader();
    initHeaderEvents();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initHeader);
  } else {
    initHeader();
  }
})();
