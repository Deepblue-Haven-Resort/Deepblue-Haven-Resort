document.addEventListener('DOMContentLoaded', function() {
    const refreshBtn = document.getElementById('refreshActivityBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            window.location.reload();
        });
    }
});

function filterLiveActivity(category, btn) {
    document.querySelectorAll('.activity-filter-pill').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');

    const rows = document.querySelectorAll('.activity-table tbody tr');
    rows.forEach(row => {
        const catBadge = row.querySelector('.activity-badge');
        const statusElem = row.querySelector('.status');
        const hasActionBtn = row.querySelector('form button') !== null;
        const statusText = statusElem ? statusElem.textContent.trim().toUpperCase() : '';

        if (category === 'all') {
            row.style.display = '';
        } else if (category === 'action') {
            if (hasActionBtn || statusText === 'PENDING' || statusText === 'DIRTY' || statusText === 'CLEANING') {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        } else {
            const rowClass = catBadge ? catBadge.className : '';
            if (rowClass.includes(category)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        }
    });
}
