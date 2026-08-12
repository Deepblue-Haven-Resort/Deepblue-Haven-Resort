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
            if (document.getElementById('serviceId')) document.getElementById('serviceId').value = '';
            if (document.getElementById('serviceImageUrl')) document.getElementById('serviceImageUrl').value = '';
            if (document.getElementById('serviceImagePreviewBox')) document.getElementById('serviceImagePreviewBox').style.display = 'none';
            window.setDropdownValue('serviceCategory', 'FOOD_BEVERAGE');
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
            const id = row.dataset.id || '';
            const serviceName = row.dataset.name || row.querySelector('td:nth-child(1)').textContent.trim();
            const category = row.dataset.category || 'FOOD_BEVERAGE';
            const price = row.dataset.price || '100000';
            const status = row.dataset.status || 'ACTIVE';
            const existingImg = row.dataset.image || '';
            
            title.innerHTML = '<i class="fa-solid fa-bell-concierge"></i> Edit Service';
            if (document.getElementById('serviceId')) document.getElementById('serviceId').value = id;
            document.getElementById('serviceName').value = serviceName;
            window.setDropdownValue('serviceCategory', category);
            document.getElementById('servicePrice').value = price;
            window.setDropdownValue('serviceStatus', status);

            if (document.getElementById('serviceImageUrl')) document.getElementById('serviceImageUrl').value = existingImg;
            if (document.getElementById('serviceImagePreview') && existingImg) {
                document.getElementById('serviceImagePreview').src = existingImg;
                document.getElementById('serviceImagePreviewBox').style.display = 'block';
            } else if (document.getElementById('serviceImagePreviewBox')) {
                document.getElementById('serviceImagePreviewBox').style.display = 'none';
            }
            
            modal.classList.add('active');
        });
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

    // Cloudinary Service Image Upload
    const uploadServiceImageBtn = document.getElementById('uploadServiceImageBtn');
    const serviceImageFileInput = document.getElementById('serviceImageFileInput');
    const serviceImageUrlInput = document.getElementById('serviceImageUrl');
    const serviceImagePreview = document.getElementById('serviceImagePreview');
    const serviceImagePreviewBox = document.getElementById('serviceImagePreviewBox');

    if (uploadServiceImageBtn && serviceImageFileInput) {
        uploadServiceImageBtn.addEventListener('click', () => {
            serviceImageFileInput.click();
        });

        serviceImageFileInput.addEventListener('change', async () => {
            const file = serviceImageFileInput.files[0];
            if (!file) return;

            const serviceId = document.getElementById('serviceId')?.value?.trim();
            const rawServiceName = document.getElementById('serviceName')?.value?.trim() || 'new-service';
            const slug = rawServiceName.toLowerCase().replace(/[^a-z0-9]+/g, '-');
            const serviceFolder = 'deepbluehaven/services/service-' + (serviceId ? serviceId : slug);
            const formData = new FormData();
            formData.append('file', file);
            formData.append('folder', serviceFolder);

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
                    if (serviceImageUrlInput) serviceImageUrlInput.value = data.url;
                    if (serviceImagePreview) serviceImagePreview.src = data.url;
                    if (serviceImagePreviewBox) serviceImagePreviewBox.style.display = 'block';
                    alert('Service image uploaded to Cloudinary successfully!');
                } else {
                    alert(data.message || 'Upload failed');
                }
            } catch (err) {
                alert('Upload request failed.');
            }
        });
    }
});
