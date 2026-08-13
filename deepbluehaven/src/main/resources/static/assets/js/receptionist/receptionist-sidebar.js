document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.getElementById('receptionistSidebar') || document.getElementById('managerSidebar');
    const toggleBtn = document.getElementById('sidebarToggle');

    if (sidebar && toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('is-collapsed');
        });
    }
});
