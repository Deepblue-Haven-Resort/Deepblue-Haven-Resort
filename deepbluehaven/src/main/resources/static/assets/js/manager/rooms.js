document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('roomModal');
    const addBtn = document.getElementById('addRoomBtn');
    const closeBtn = document.getElementById('closeRoomModalBtn');
    const cancelBtn = document.getElementById('cancelRoomModalBtn');
    const form = document.getElementById('roomForm');
    const title = document.getElementById('roomModalTitle');

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

    const closeModal = () => {
        modal.classList.remove('active');
    };

    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            closeModal();
        }
    });

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

    const assignModal = document.getElementById('assignTaskModal');
    const closeAssignModalBtn = document.getElementById('closeAssignModalBtn');
    const cancelAssignModalBtn = document.getElementById('cancelAssignModalBtn');
    const assignForm = document.getElementById('assignTaskForm');

    const closeAssignModal = () => { if (assignModal) assignModal.classList.remove('active'); };
    if (closeAssignModalBtn) closeAssignModalBtn.addEventListener('click', closeAssignModal);
    if (cancelAssignModalBtn) cancelAssignModalBtn.addEventListener('click', closeAssignModal);

    document.addEventListener('click', (e) => {
        const btn = e.target.closest('.assign-btn');
        if (btn) {
            e.preventDefault();
            const row = btn.closest('tr');
            const roomId = btn.dataset.roomId || (row ? row.dataset.id : '') || '';
            const roomNum = btn.dataset.roomNumber || (row ? row.dataset.number : '') || '';

            if (assignForm) assignForm.reset();

            if (document.getElementById('assignRoomId')) {
                document.getElementById('assignRoomId').value = roomId;
            }
            if (document.getElementById('taskRoomNumber')) {
                document.getElementById('taskRoomNumber').value = 'Room ' + roomNum;
            }

            window.setDropdownValue('taskType', 'Cleaning & Prep');
            window.setDropdownValue('taskPriority', 'NORMAL');
            window.setDropdownValue('taskAssignee', '');

            if (assignModal) assignModal.classList.add('active');
        }
    });

    if (assignModal) {
        assignModal.addEventListener('click', (e) => {
            if (e.target === assignModal) {
                closeAssignModal();
            }
        });
    }

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
                    showToast('success', 'Success', 'Room image uploaded to Cloudinary successfully!');
                } else {
                    showToast('error', 'Upload Failed', data.message || 'Upload failed');
                }
            } catch (err) {
                showToast('error', 'Upload Error', 'Upload request failed.');
            }
        });
    }

    // ---------- Tab Navigation Handler ----------
    const tabBtns = document.querySelectorAll('.panel-tab-btn');
    const tabPanels = document.querySelectorAll('.tab-content-panel');

    function switchTab(tabId) {
        tabBtns.forEach(b => b.classList.remove('active'));
        tabPanels.forEach(p => {
            p.classList.remove('active');
            p.style.display = 'none';
        });

        const activeBtn = document.querySelector(`.panel-tab-btn[data-tab="${tabId}"]`);
        const activePanel = document.getElementById(tabId);

        if (activePanel) {
            if (activeBtn) activeBtn.classList.add('active');
            activePanel.classList.add('active');
            activePanel.style.display = 'block';
            localStorage.setItem('manager_rooms_active_tab', tabId);
            if (typeof window.initTablePagination === 'function') {
                window.initTablePagination();
            }
        }
    }

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            const tabId = btn.getAttribute('data-tab');
            switchTab(tabId);
        });
    });

    const urlParams = new URLSearchParams(window.location.search);
    const paramTab = urlParams.get('tab');
    if (paramTab === 'housekeeping' || paramTab === 'tab-housekeeping') {
        switchTab('tab-housekeeping');
    } else if (paramTab === 'inventory' || paramTab === 'tab-inventory') {
        switchTab('tab-inventory');
    } else {
        const savedTab = localStorage.getItem('manager_rooms_active_tab');
        if (savedTab && document.getElementById(savedTab)) {
            switchTab(savedTab);
        } else {
            switchTab('tab-inventory');
        }
    }

    // ---------- View Proof Modal Lightbox Handler & Interactive Zoom/Pan ----------
    let currentZoom = 1.0;
    let panX = 0;
    let panY = 0;
    let isDragging = false;
    let startX = 0;
    let startY = 0;

    const mainImg = document.getElementById('modalProofMainImage');
    const zoomLevelDisplay = document.getElementById('zoomLevelDisplay');
    const openOriginalBtn = document.getElementById('openOriginalImageBtn');
    const counterDisplay = document.getElementById('proofImageCounter');

    function applyTransform() {
        if (mainImg) {
            mainImg.style.transform = `translate(${panX}px, ${panY}px) scale(${currentZoom})`;
            if (currentZoom > 1) {
                mainImg.style.cursor = isDragging ? 'grabbing' : 'grab';
            } else {
                mainImg.style.cursor = 'zoom-in';
            }
        }
        if (zoomLevelDisplay) {
            zoomLevelDisplay.textContent = Math.round(currentZoom * 100) + '%';
        }
    }

    function setZoom(zoomVal) {
        currentZoom = Math.min(Math.max(0.5, zoomVal), 4.0);
        if (currentZoom <= 1) {
            panX = 0;
            panY = 0;
        }
        applyTransform();
    }

    if (mainImg) {
        // Toggle Zoom on click if not dragged
        let clickStartPos = { x: 0, y: 0 };
        mainImg.addEventListener('mousedown', function(e) {
            clickStartPos = { x: e.clientX, y: e.clientY };
            if (currentZoom > 1) {
                e.preventDefault();
                isDragging = true;
                startX = e.clientX - panX;
                startY = e.clientY - panY;
                applyTransform();
            }
        });

        window.addEventListener('mousemove', function(e) {
            if (isDragging && currentZoom > 1) {
                e.preventDefault();
                panX = e.clientX - startX;
                panY = e.clientY - startY;
                applyTransform();
            }
        });

        window.addEventListener('mouseup', function(e) {
            if (isDragging) {
                isDragging = false;
                applyTransform();
            } else {
                const dist = Math.hypot(e.clientX - clickStartPos.x, e.clientY - clickStartPos.y);
                if (dist < 5 && e.target === mainImg) {
                    setZoom(currentZoom === 1.0 ? 1.8 : 1.0);
                }
            }
        });

        // Mouse Wheel Zoom
        mainImg.addEventListener('wheel', function(e) {
            e.preventDefault();
            const zoomDelta = e.deltaY < 0 ? 0.2 : -0.2;
            setZoom(currentZoom + zoomDelta);
        }, { passive: false });
    }

    document.getElementById('zoomInBtn')?.addEventListener('click', () => setZoom(currentZoom + 0.25));
    document.getElementById('zoomOutBtn')?.addEventListener('click', () => setZoom(currentZoom - 0.25));
    document.getElementById('resetZoomBtn')?.addEventListener('click', () => setZoom(1.0));

    // Event Delegation for View Proof Button
    document.addEventListener('click', function(e) {
        const btn = e.target.closest('.view-proof-btn');
        if (btn) {
            e.preventDefault();
            const urlString = btn.getAttribute('data-proof-url');
            const roomNum = btn.getAttribute('data-room-number');
            if (urlString) {
                const urls = urlString.split(',').map(u => u.trim()).filter(u => u.length > 0);
                const header = document.getElementById('proofRoomNumberHeader');
                const gallery = document.getElementById('proofThumbnailsGallery');
                const proofModal = document.getElementById('viewProofModal');

                if (header) header.textContent = 'Room ' + roomNum;
                setZoom(1.0);

                if (urls.length > 0) {
                    if (mainImg) mainImg.src = urls[0];
                    if (openOriginalBtn) openOriginalBtn.href = urls[0];
                    if (counterDisplay) counterDisplay.textContent = 'Image 1 of ' + urls.length;
                }

                if (gallery) {
                    gallery.innerHTML = '';
                    if (urls.length > 1) {
                        urls.forEach((url, idx) => {
                            const thumb = document.createElement('img');
                            thumb.src = url;
                            if (idx === 0) thumb.classList.add('active-thumb');
                            thumb.addEventListener('click', function() {
                                mainImg.src = url;
                                if (openOriginalBtn) openOriginalBtn.href = url;
                                if (counterDisplay) counterDisplay.textContent = 'Image ' + (idx + 1) + ' of ' + urls.length;
                                setZoom(1.0);
                                Array.from(gallery.children).forEach(c => c.classList.remove('active-thumb'));
                                thumb.classList.add('active-thumb');
                            });
                            gallery.appendChild(thumb);
                        });
                    }
                }

                if (proofModal) proofModal.classList.add('active');
            }
        }
    });

    const closeProofBtn = document.getElementById('closeProofModalBtn');
    const closeProofFooterBtn = document.getElementById('closeProofModalFooterBtn');
    [closeProofBtn, closeProofFooterBtn].forEach(b => {
        if (b) b.addEventListener('click', () => {
            const proofModal = document.getElementById('viewProofModal');
            if (proofModal) proofModal.classList.remove('active');
        });
    });
});

