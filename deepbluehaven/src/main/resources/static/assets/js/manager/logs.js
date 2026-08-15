document.addEventListener('DOMContentLoaded', () => {
    const tabButtons = document.querySelectorAll('.log-tabs .tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabId = btn.getAttribute('data-tab');
            document.querySelectorAll('.tab-content').forEach(content => {
                content.classList.remove('active');
            });
            document.querySelectorAll('.log-tabs .tab-btn').forEach(b => {
                b.classList.remove('active');
            });
            const targetTab = document.getElementById(tabId);
            if (targetTab) {
                targetTab.classList.add('active');
            }
            btn.classList.add('active');
            if (typeof window.initTablePagination === 'function') {
                window.initTablePagination();
            }
        });
    });
});
