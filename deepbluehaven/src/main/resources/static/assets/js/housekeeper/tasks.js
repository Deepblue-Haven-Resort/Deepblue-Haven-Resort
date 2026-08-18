/**
 * Deep Blue Haven - Housekeeper Tasks Script
 */
(function () {
    'use strict';

    function getAppUrl(path) {
        let contextPath = document.querySelector('meta[name="_context_path"]')?.content;
        if (!contextPath || contextPath === "/") {
            const match = window.location.pathname.match(/^\/([^\/]+)/);
            if (match && match[1] === "deepbluehaven") {
                contextPath = "/" + match[1];
            } else {
                contextPath = "";
            }
        }
        return contextPath.replace(/\/$/, "") + (path.startsWith("/") ? path : "/" + path);
    }

    document.addEventListener("DOMContentLoaded", function () {
        // Status filter dropdown
        const statusFilterBtns = document.querySelectorAll(".filter-menu button");
        statusFilterBtns.forEach(btn => {
            btn.addEventListener("click", function () {
                const label = this.textContent.trim();
                const filterLabel = document.querySelector(".filter-label strong");
                if (filterLabel) filterLabel.textContent = label;
                const dropdown = this.closest(".filter-dropdown");
                if (dropdown) dropdown.classList.remove("active");
            });
        });

        // Complete Task Modal Elements
        const completeModal = document.getElementById('completeTaskModal');
        const completeForm = document.getElementById('completeTaskForm');
        const proofFileInput = document.getElementById('proofFileInput');
        const triggerUploadBtn = document.getElementById('triggerProofUploadBtn');
        const proofFileName = document.getElementById('proofFileName');
        const proofImageUrlInput = document.getElementById('proofImageUrlInput');
        const submitBtn = document.getElementById('submitCompleteTaskBtn');
        const previewBox = document.getElementById('proofImagePreviewContainer');
        const progressBox = document.getElementById('proofUploadProgress');

        // Open Complete Modal
        document.addEventListener('click', function (e) {
            const btn = e.target.closest('.open-complete-modal-btn');
            if (btn) {
                e.preventDefault();
                const taskId = btn.getAttribute('data-task-id');
                const roomNum = btn.getAttribute('data-room-number') || '';
                
                if (completeForm && taskId) {
                    completeForm.action = getAppUrl('/housekeeper/tasks/' + taskId + '/complete');
                }
                
                const modalRoomDisplay = document.getElementById('modalRoomNumberDisplay');
                if (modalRoomDisplay) {
                    modalRoomDisplay.textContent = roomNum ? 'Room ' + roomNum : 'Assigned Room';
                }
                
                if (proofImageUrlInput) proofImageUrlInput.value = '';
                if (proofFileInput) proofFileInput.value = '';
                if (proofFileName) proofFileName.textContent = 'No file chosen';
                if (previewBox) {
                    previewBox.innerHTML = '';
                    previewBox.style.display = 'none';
                }
                if (progressBox) progressBox.style.display = 'none';
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i> Submit Inspection';
                }
                if (completeModal) completeModal.classList.add('active');
            }
        });

        if (triggerUploadBtn && proofFileInput) {
            triggerUploadBtn.addEventListener('click', () => proofFileInput.click());
        }

        if (proofFileInput) {
            proofFileInput.addEventListener('change', function () {
                if (this.files && this.files.length > 0) {
                    let files = Array.from(this.files);
                    if (files.length > 5) {
                        if (typeof showToast === 'function') {
                            showToast('warning', 'Photo Limit Exceeded', 'You can upload a maximum of 5 evidence photos.');
                        }
                        files = files.slice(0, 5);
                    }
                    if (proofFileName) {
                        proofFileName.textContent = files.length + '/5 photo(s) chosen';
                    }

                    // Immediate thumbnail preview
                    if (previewBox) {
                        previewBox.innerHTML = '';
                        previewBox.style.display = 'flex';
                        previewBox.style.gap = '8px';
                        previewBox.style.justifyContent = 'center';
                        previewBox.style.flexWrap = 'wrap';

                        files.forEach(file => {
                            const img = document.createElement('img');
                            img.src = URL.createObjectURL(file);
                            img.style.cssText = 'max-height: 90px; max-width: 110px; border-radius: 6px; border: 1px solid #cbd5e1; object-fit: cover;';
                            previewBox.appendChild(img);
                        });
                    }

                    // Asynchronous upload in background
                    if (progressBox) progressBox.style.display = 'block';

                    const uploadPromises = files.map(file => {
                        const formData = new FormData();
                        formData.append('file', file);
                        formData.append('folder', 'deepbluehaven/housekeeping_proofs');
                        return fetch(getAppUrl('/api/upload/image'), {
                            method: 'POST',
                            body: formData
                        })
                        .then(res => res.ok ? res.json() : null)
                        .catch(() => null);
                    });

                    Promise.all(uploadPromises)
                        .then(results => {
                            if (progressBox) progressBox.style.display = 'none';
                            const validUrls = (results || []).filter(r => r && r.success && r.url).map(r => r.url);
                            if (validUrls.length > 0 && proofImageUrlInput) {
                                proofImageUrlInput.value = validUrls.join(',');
                            }
                        })
                        .catch(() => {
                            if (progressBox) progressBox.style.display = 'none';
                        });
                }
            });
        }

        const closeBtn = document.getElementById('closeCompleteModalBtn');
        const cancelBtn = document.getElementById('cancelCompleteModalBtn');
        [closeBtn, cancelBtn].forEach(b => {
            if (b) {
                b.addEventListener('click', (e) => {
                    e.preventDefault();
                    if (completeModal) completeModal.classList.remove('active');
                });
            }
        });

        // Minibar Logging Modal
        const minibarModal = document.getElementById('minibarLogModal');
        const minibarForm = document.getElementById('minibarLogForm');
        let currentMinibarTaskId = null;

        document.addEventListener('click', function (e) {
            const btn = e.target.closest('.open-minibar-modal-btn');
            if (btn) {
                e.preventDefault();
                currentMinibarTaskId = btn.getAttribute('data-task-id');
                const roomNum = btn.getAttribute('data-room-number');
                const display = document.getElementById('minibarRoomDisplay');
                if (display) display.textContent = roomNum || 'Selected Room';
                if (minibarModal) minibarModal.classList.add('active');
            }
        });

        const closeMinibarBtn = document.getElementById('closeMinibarModalBtn');
        const cancelMinibarBtn = document.getElementById('cancelMinibarModalBtn');
        [closeMinibarBtn, cancelMinibarBtn].forEach(b => {
            if (b) {
                b.addEventListener('click', (e) => {
                    e.preventDefault();
                    if (minibarModal) minibarModal.classList.remove('active');
                });
            }
        });

        if (minibarForm) {
            minibarForm.addEventListener('submit', function (e) {
                e.preventDefault();
                if (!currentMinibarTaskId) return;

                const itemName = document.getElementById('minibarItemSelect')?.value || '';
                const quantity = document.getElementById('minibarQtyInput')?.value || '1';

                const formData = new URLSearchParams();
                formData.append('itemName', itemName);
                formData.append('quantity', quantity);

                fetch(getAppUrl('/housekeeper/tasks/' + currentMinibarTaskId + '/minibar-log'), {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData.toString()
                })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        if (typeof showToast === 'function') {
                            showToast('success', 'Minibar Logged', data.message || 'Consumption added to guest bill.');
                        }
                        if (minibarModal) minibarModal.classList.remove('active');
                    } else {
                        if (typeof showToast === 'function') {
                            showToast('error', 'Error', data.message || 'Could not log minibar item.');
                        }
                    }
                })
                .catch(err => {
                    if (typeof showToast === 'function') {
                        showToast('error', 'Error', 'Failed to communicate with server: ' + err.message);
                    }
                });
            });
        }

        document.addEventListener('click', function (e) {
            if (e.target.classList.contains('modal-overlay')) {
                document.querySelectorAll('.modal-overlay.active').forEach(m => m.classList.remove('active'));
            }
        });

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') {
                document.querySelectorAll('.modal-overlay.active').forEach(m => m.classList.remove('active'));
            }
        });
    });
})();
