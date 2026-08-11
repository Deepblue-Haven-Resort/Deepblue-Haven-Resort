// services.js
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('serviceModal');
    const addBtn = document.getElementById('addServiceBtn');
    const closeBtn = document.getElementById('closeServiceModalBtn');
    const cancelBtn = document.getElementById('cancelServiceModalBtn');
    const form = document.getElementById('serviceForm');
    const title = document.getElementById('serviceModalTitle');

    // Open Modal for Add
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            title.innerHTML = '<i class="fa-solid fa-bell-concierge"></i> Add New Service';
            form.reset();
            window.setDropdownValue('serviceCategory', '');
            window.setDropdownValue('serviceStatus', 'ACTIVE');
            modal.classList.add('active');
        });
    }

    // Close Modal
    const closeModal = () => {
        modal.classList.remove('active');
    };

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);

    // Close on outside click
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });

    // Handle Edit Buttons
    const editBtns = document.querySelectorAll('.edit-btn');
    editBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const row = e.target.closest('tr');
            const serviceName = row.querySelector('td:nth-child(1)').textContent.trim();
            const category = row.querySelector('td:nth-child(2)').textContent.trim();
            const priceStr = row.querySelector('td:nth-child(3)').textContent.trim();
            
            title.innerHTML = '<i class="fa-solid fa-bell-concierge"></i> Edit Service';
            document.getElementById('serviceName').value = serviceName;
            
            if (category.includes('Food')) window.setDropdownValue('serviceCategory', 'FNB');
            else if (category.includes('Spa')) window.setDropdownValue('serviceCategory', 'SPA');
            else if (category.includes('Laundry')) window.setDropdownValue('serviceCategory', 'LAUNDRY');
            else window.setDropdownValue('serviceCategory', 'OTHER');
            
            document.getElementById('servicePrice').value = priceStr.replace(/\D/g, '');
            window.setDropdownValue('serviceStatus', 'ACTIVE');
            
            modal.classList.add('active');
        });
    });

    // Handle Delete Buttons
    const deleteBtns = document.querySelectorAll('.delete-btn');
    deleteBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const row = e.target.closest('tr');
            const serviceName = row.querySelector('td:nth-child(1)').textContent.trim();
            
            if (confirm(`Are you sure you want to delete "${serviceName}"?`)) {
                row.remove();
                alert(`Service "${serviceName}" deleted.`);
            }
        });
    });

    // Form submission
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        alert('Service saved successfully!');
        closeModal();
        // In a real app, this would refresh the table or add a new row
    });

    // --- Custom Dropdown Logic ---
    const filterDropdowns = document.querySelectorAll('.filter-dropdown');
    
    filterDropdowns.forEach(dropdown => {
        const btn = dropdown.querySelector('.filter-btn');
        const menu = dropdown.querySelector('.filter-menu');
        const label = dropdown.querySelector('.filter-label strong');
        const inputId = dropdown.dataset.input;
        const hiddenInput = document.getElementById(inputId);

        if (btn) {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                // close all others
                filterDropdowns.forEach(other => {
                    if (other !== dropdown) other.classList.remove('active');
                });
                dropdown.classList.toggle('active');
            });
        }

        if (menu) {
            const options = menu.querySelectorAll('button');
            options.forEach(opt => {
                opt.addEventListener('click', () => {
                    const value = opt.dataset.value;
                    const text = opt.textContent.trim();
                    
                    label.textContent = text;
                    if (hiddenInput) hiddenInput.value = value;
                    
                    dropdown.classList.remove('active');
                });
            });
        }
    });

    document.addEventListener('click', () => {
        filterDropdowns.forEach(dropdown => {
            dropdown.classList.remove('active');
        });
    });

    // Helper to set dropdown value programmatically
    window.setDropdownValue = function(inputId, value) {
        const hiddenInput = document.getElementById(inputId);
        const dropdown = document.querySelector(`.filter-dropdown[data-input="${inputId}"]`);
        if (hiddenInput && dropdown) {
            hiddenInput.value = value;
            const option = dropdown.querySelector(`.filter-menu button[data-value="${value}"]`);
            if (option) {
                dropdown.querySelector('.filter-label strong').textContent = option.textContent.trim();
            } else if (value === '') {
                dropdown.querySelector('.filter-label strong').textContent = 'Select...';
            }
        }
    };
});
