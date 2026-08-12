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
            if (document.getElementById('roomId')) document.getElementById('roomId').value = '';
            if (document.getElementById('roomImageUrl')) document.getElementById('roomImageUrl').value = '';
            if (document.getElementById('roomImagePreviewBox')) document.getElementById('roomImagePreviewBox').style.display = 'none';
            window.setDropdownValue('roomType', 'STANDARD');
            window.setDropdownValue('roomStatus', 'AVAILABLE');
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
            const id = row.dataset.id || '';
            const roomNum = row.dataset.number || row.querySelector('td:nth-child(1)').textContent.replace(/\D/g, '');
            const roomType = row.dataset.type || 'STANDARD';
            const capacity = row.dataset.capacity || '2';
            const price = row.dataset.price || '1000000';
            const area = row.dataset.area || '';
            const status = row.dataset.status || 'AVAILABLE';
            const existingImg = row.dataset.image || '';
            
            title.textContent = 'Edit Room ' + roomNum;
            if (document.getElementById('roomId')) document.getElementById('roomId').value = id;
            document.getElementById('roomNumber').value = roomNum;
            window.setDropdownValue('roomType', roomType);
            document.getElementById('capacity').value = capacity;
            document.getElementById('basePrice').value = price;
            if (document.getElementById('area')) document.getElementById('area').value = area;
            window.setDropdownValue('roomStatus', status);
            
            if (document.getElementById('roomImageUrl')) document.getElementById('roomImageUrl').value = existingImg;
            if (document.getElementById('roomImagePreview') && existingImg) {
                document.getElementById('roomImagePreview').src = existingImg;
                document.getElementById('roomImagePreviewBox').style.display = 'block';
            } else if (document.getElementById('roomImagePreviewBox')) {
                document.getElementById('roomImagePreviewBox').style.display = 'none';
            }

            modal.classList.add('active');
        });
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
            const roomId = btn.dataset.roomId || row.dataset.id || '';
            const roomNum = btn.dataset.roomNumber || row.dataset.number || '';
            if (document.getElementById('assignRoomId')) {
                document.getElementById('assignRoomId').value = roomId;
            }
            if (document.getElementById('taskRoomNumber')) {
                document.getElementById('taskRoomNumber').value = 'Room ' + roomNum;
            }
            if (assignForm) assignForm.reset();
            window.setDropdownValue('taskType', 'Cleaning & Prep');
            if (assignModal) assignModal.classList.add('active');
        });
    });

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

    // Cloudinary Room Image Upload
    const uploadRoomImageBtn = document.getElementById('uploadRoomImageBtn');
    const roomImageFileInput = document.getElementById('roomImageFileInput');
    const roomImageUrlInput = document.getElementById('roomImageUrl');
    const roomImagePreview = document.getElementById('roomImagePreview');
    const roomImagePreviewBox = document.getElementById('roomImagePreviewBox');

    if (uploadRoomImageBtn && roomImageFileInput) {
        uploadRoomImageBtn.addEventListener('click', () => {
            roomImageFileInput.click();
        });

        roomImageFileInput.addEventListener('change', async () => {
            const file = roomImageFileInput.files[0];
            if (!file) return;

            const roomId = document.getElementById('roomId')?.value?.trim();
            const rawRoomNum = document.getElementById('roomNumber')?.value?.trim() || 'new-room';
            const slug = rawRoomNum.toLowerCase().replace(/[^a-z0-9]+/g, '-');
            const roomFolder = 'deepbluehaven/rooms/room-' + (roomId ? roomId : slug);
            const formData = new FormData();
            formData.append('file', file);
            formData.append('folder', roomFolder);

            try {
                let ctx = document.querySelector('meta[name="_context_path"]')?.content;
                if (!ctx || ctx === "/") {
                    ctx = window.location.pathname.startsWith('/deepbluehaven') ? '/deepbluehaven' : '';
                }
                const apiUrl = ctx.replace(/\/$/, '') + '/api/upload/image';
                const res = await fetch(apiUrl, {
                    method: 'POST',
                    body: formData
                });
                const data = await res.json();
                if (data.success && data.url) {
                    if (roomImageUrlInput) roomImageUrlInput.value = data.url;
                    if (roomImagePreview) roomImagePreview.src = data.url;
                    if (roomImagePreviewBox) roomImagePreviewBox.style.display = 'block';
                    alert('Room image uploaded to Cloudinary successfully!');
                } else {
                    alert(data.message || 'Upload failed');
                }
            } catch (err) {
                alert('Upload request failed.');
            }
        });
    }
});
