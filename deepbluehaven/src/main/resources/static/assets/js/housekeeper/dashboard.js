/**
 * Deep Blue Haven - Housekeeper Dashboard Script
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
        const btnRefresh = document.querySelector(".panel-header button");
        if (btnRefresh) {
            btnRefresh.addEventListener("click", function () {
                window.location.reload();
            });
        }

        const completeModal = document.getElementById('completeTaskModal');
        const completeForm = document.getElementById('completeTaskForm');
        const proofFileInput = document.getElementById('proofFileInput');
        const triggerUploadBtn = document.getElementById('triggerProofUploadBtn');
        const proofFileName = document.getElementById('proofFileName');
        const proofImageUrlInput = document.getElementById('proofImageUrlInput');
        const submitBtn = document.getElementById('submitCompleteTaskBtn');
        const previewBox = document.getElementById('proofImagePreviewContainer');
        const progressBox = document.getElementById('proofUploadProgress');

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
                            showToast('warning', 'Photo Limit Exceeded', 'You can upload a maximum of 5 evidence photos. First 5 photos selected.');
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

                    // Asynchronous upload to Cloudinary/local fallback
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

        document.addEventListener('click', function (e) {
            if (e.target === completeModal || e.target.classList.contains('modal-overlay')) {
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
