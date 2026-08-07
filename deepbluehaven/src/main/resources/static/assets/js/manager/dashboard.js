document.addEventListener('DOMContentLoaded', function() {
    const refreshBtn = document.getElementById('refreshActivityBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            window.location.reload();
        });
    }
});
