// rooms.js
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('roomModal');
    const addBtn = document.getElementById('addRoomBtn');
    const closeBtn = document.getElementById('closeRoomModalBtn');
    const cancelBtn = document.getElementById('cancelRoomModalBtn');
    const form = document.getElementById('roomForm');
    const title = document.getElementById('roomModalTitle');

    // Open Modal for Add
    if (addBtn) {
        addBtn.addEventListener('click', () => {
            title.textContent = 'Add New Room';
            form.reset();
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
            const roomNum = row.querySelector('td:nth-child(1)').textContent.trim();
            const roomType = row.querySelector('td:nth-child(2)').textContent.trim();
            const capacityStr = row.querySelector('td:nth-child(3)').textContent.trim();
            const priceStr = row.querySelector('td:nth-child(4)').textContent.trim();
            
            // Basic parsing for demo
            title.textContent = 'Edit Room ' + roomNum;
            document.getElementById('roomNumber').value = roomNum;
            
            if (roomType.includes('Premium')) window.setDropdownValue('roomType', 'PREMIUM');
            else if (roomType.includes('Deluxe')) window.setDropdownValue('roomType', 'DELUXE');
            else window.setDropdownValue('roomType', 'STANDARD');
            
            document.getElementById('capacity').value = capacityStr.replace(/\D/g, '');
            document.getElementById('basePrice').value = priceStr.replace(/\D/g, '');
            
            modal.classList.add('active');
        });
    });

    // Handle Maintenance Buttons
    const maintenanceBtns = document.querySelectorAll('.maintenance-btn');
    maintenanceBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const row = e.target.closest('tr');
            const roomNum = row.querySelector('td:nth-child(1)').textContent.trim();
            
            if (confirm(`Are you sure you want to set ${roomNum} to Maintenance?`)) {
                // Change status cell
                const statusCell = row.querySelector('td:nth-child(5)');
                statusCell.innerHTML = '<span class="status status-danger">MAINTENANCE</span>';
                alert(`${roomNum} is now under maintenance.`);
            }
        });
    });

    // Form submission
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        alert('Room saved successfully!');
        closeModal();
        // In a real app, this would refresh the table or add a new row
    });

    // --- Modal Logic (Assign Task) ---
    const assignModal = document.getElementById('assignTaskModal');
    const closeAssignModalBtn = document.getElementById('closeAssignModalBtn');
    const cancelAssignModalBtn = document.getElementById('cancelAssignModalBtn');
    const assignForm = document.getElementById('assignTaskForm');

    const closeAssignModal = () => { if (assignModal) assignModal.classList.remove('active'); };
    if (closeAssignModalBtn) closeAssignModalBtn.addEventListener('click', closeAssignModal);
    if (cancelAssignModalBtn) cancelAssignModalBtn.addEventListener('click', closeAssignModal);

    document.querySelectorAll('.assign-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const row = e.target.closest('tr');
            const roomNum = row.querySelector('td:nth-child(1)').textContent.trim();
            if (document.getElementById('taskRoomNumber')) {
                document.getElementById('taskRoomNumber').value = roomNum;
            }
            if (assignForm) assignForm.reset();
            if (assignModal) assignModal.classList.add('active');
        });
    });

    if (assignForm) {
        assignForm.addEventListener('submit', (e) => {
            e.preventDefault();
            alert('Task assigned successfully!');
            closeAssignModal();
        });
    }

    if (assignModal) {
        assignModal.addEventListener('click', (e) => {
            if (e.target === assignModal) {
                closeAssignModal();
            }
        });
    }

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
            }
        }
    };
});
