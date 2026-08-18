document.addEventListener('DOMContentLoaded', function() {
    const refreshBtn = document.getElementById('refreshActivityBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function() {
            window.location.reload();
        });
    }

    const walkInModal = document.getElementById('walkInModal');
    const openWalkInBtn = document.querySelector('[data-open-walkin]');
    if (openWalkInBtn && walkInModal) {
        openWalkInBtn.addEventListener('click', function() {
            walkInModal.classList.add('active');
        });
    }

    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', function() {
            document.querySelectorAll('.modal-overlay.active').forEach(m => m.classList.remove('active'));
        });
    });

    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal-overlay')) {
            e.target.classList.remove('active');
        }
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.active').forEach(m => m.classList.remove('active'));
        }
    });
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
