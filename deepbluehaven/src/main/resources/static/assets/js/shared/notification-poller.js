/**
 * Realtime Notification & Badge Poller - DeepBlue Haven PMS
 */
(function () {
    const getContextPath = () => {
        const path = window.location.pathname;
        if (path.startsWith('/deepbluehaven/')) return '/deepbluehaven';
        return '';
    };
    const ctx = getContextPath();

    let lastUnreadCount = 0;

    const updateBadges = (unreadCount) => {
        const badgeElements = document.querySelectorAll('.notification-badge, .nav-badge-count, .sidebar-badge-count');
        badgeElements.forEach(badge => {
            if (unreadCount > 0) {
                badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                badge.style.display = 'inline-flex';
                if (unreadCount > lastUnreadCount) {
                    badge.classList.add('pulse-anim');
                    setTimeout(() => badge.classList.remove('pulse-anim'), 1500);
                }
            } else {
                badge.style.display = 'none';
            }
        });
        lastUnreadCount = unreadCount;
    };

    const pollNotifications = async () => {
        try {
            const res = await fetch(`${ctx}/api/notifications/realtime-badges`, {
                headers: { 'Accept': 'application/json' }
            });
            if (res.ok) {
                const data = await res.json();
                if (data && typeof data.unreadNotifications === 'number') {
                    updateBadges(data.unreadNotifications);
                }
            }
        } catch (err) {
            // Quiet fail during network blips
        }
    };

    document.addEventListener('DOMContentLoaded', () => {
        // Initial poll after 1s
        setTimeout(pollNotifications, 1000);
        // Polling interval: 15 seconds
        setInterval(pollNotifications, 15000);
    });
})();
