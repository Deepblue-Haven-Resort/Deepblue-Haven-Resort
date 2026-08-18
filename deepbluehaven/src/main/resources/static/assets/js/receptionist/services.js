/**
 * Receptionist Guest Service Orders & Fulfillment Logic
 */
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('tableSearchInput');
    const rows = document.querySelectorAll('.activity-table tbody tr:not(.table-empty-row)');
    let currentStatusFilter = 'ALL';

    function filterTable() {
        const query = searchInput ? searchInput.value.toLowerCase().trim() : '';

        rows.forEach(row => {
            const text = row.innerText.toLowerCase();
            const status = row.getAttribute('data-status') || '';

            const matchesQuery = !query || text.includes(query);
            const matchesStatus = (currentStatusFilter === 'ALL') || (status === currentStatusFilter);

            if (matchesQuery && matchesStatus) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    }

    if (searchInput) {
        searchInput.addEventListener('input', filterTable);
    }

    // Status Dropdown filter
    const dropdownItems = document.querySelectorAll('.filter-menu .dropdown-item');
    const filterLabel = document.querySelector('.filter-label strong');

    dropdownItems.forEach(item => {
        item.addEventListener('click', () => {
            currentStatusFilter = item.getAttribute('data-status');
            if (filterLabel) filterLabel.textContent = item.textContent;
            filterTable();
        });
    });
});
