(function () {
    document.addEventListener("DOMContentLoaded", function () {
        const statusFilterBtns = document.querySelectorAll(".filter-menu button");
        statusFilterBtns.forEach(btn => {
            btn.addEventListener("click", function () {
                const label = this.textContent.trim();
                const filterLabel = document.querySelector(".filter-label strong");
                if (filterLabel) 
                    filterLabel.textContent = label;
                const dropdown = this.closest(".filter-dropdown");
                if (dropdown) dropdown.classList.remove("active");
            });
        });

        const completeModal = document.getElementById('completeTaskModal');
        const completeForm = document.getElementById('completeTaskForm');
        const proofFileInput = document.getElementById('proofFileInput');
        const triggerUploadBtn = document.getElementById('triggerProofUploadBtn');
        const proofFileName = document.getElementById('proofFileName');
        const proofImageUrlInput = document.getElementById('proofImageUrlInput');
        const submitBtn = document.getElementById('submitCompleteTaskBtn');
        const previewBox = document.getElementById('proofImagePreviewContainer');
        const progressBox = document.getElementById('proofUploadProgress');

        document.addEventListener('click', function(e) {
            const btn = e.target.closest('.open-complete-modal-btn');
            if (btn) {
                e.preventDefault();
                const taskId = btn.getAttribute('data-task-id');
                const roomNum = btn.getAttribute('data-room-number');
                if (completeForm) {
                    completeForm.action = '/deepbluehaven/housekeeper/tasks/' + taskId + '/complete';
                }
                const modalRoomDisplay = document.getElementById('modalRoomNumberDisplay');
                if (modalRoomDisplay) {
                    modalRoomDisplay.textContent = 'Room ' + roomNum;
                }
                if (proofImageUrlInput) 
                    proofImageUrlInput.value = '';
                if (proofFileName) 
                    proofFileName.textContent = 'No file chosen';
                if (previewBox) {
                    previewBox.innerHTML = '';
                    previewBox.style.display = 'none';
                }
                if (submitBtn) 
                    submitBtn.disabled = true;
                if (completeModal) 
                    completeModal.classList.add('active');
            }
        });

        if (triggerUploadBtn) {
            triggerUploadBtn.addEventListener('click', () => proofFileInput.click());
        }

        if (proofFileInput) {
            proofFileInput.addEventListener('change', function() {
                if (this.files && this.files.length > 0) {
                    let files = Array.from(this.files);
                    if (files.length > 5) {
                        showToast('warning', 'Photo Limit Exceeded', 'You can upload a maximum of 5 evidence photos. Only the first 5 photos will be selected.');
                        files = files.slice(0, 5);
                    }
                    if (proofFileName) 
                        proofFileName.textContent = files.length + '/5 photo(s) chosen';
                    if (progressBox) 
                        progressBox.style.display = 'block';
                    if (submitBtn) 
                        submitBtn.disabled = true;

                    const uploadPromises = files.map(file => {
                        const formData = new FormData();
                        formData.append('file', file);
                        formData.append('folder', 'deepbluehaven/housekeeping_proofs');
                        return fetch('/deepbluehaven/api/upload/image', {
                            method: 'POST',
                            body: formData
                        }).then(res => res.json());
                    });

                    Promise.all(uploadPromises)
                        .then(results => {
                            if (progressBox) 
                                progressBox.style.display = 'none';
                            const successfulUrls = results.filter(r => r.success && r.url).map(r => r.url);
                            if (successfulUrls.length > 0) {
                                if (proofImageUrlInput) 
                                    proofImageUrlInput.value = successfulUrls.join(',');
                                if (previewBox) {
                                    previewBox.innerHTML = '';
                                    previewBox.style.display = 'flex';
                                    previewBox.style.gap = '8px';
                                    previewBox.style.justifyContent = 'center';
                                    previewBox.style.flexWrap = 'wrap';
                                    successfulUrls.forEach(url => {
                                        const img = document.createElement('img');
                                        img.src = url;
                                        img.style.cssText = 'max-height: 100px; max-width: 120px; border-radius: 6px; border: 1px solid #cbd5e1; object-fit: cover;';
                                        previewBox.appendChild(img);
                                    });
                                }
                                if (submitBtn) 
                                    submitBtn.disabled = false;
                            } else {
                                showToast('error', 'Upload Failed', 'Upload failed for selected photo(s).');
                            }
                        })
                        .catch(err => {
                            if (progressBox) 
                                progressBox.style.display = 'none';
                            showToast('error', 'Upload Error', 'Upload error: ' + err.message);
                        });
                }
            });
        }

        const closeBtn = document.getElementById('closeCompleteModalBtn');
        const cancelBtn = document.getElementById('cancelCompleteModalBtn');
        [closeBtn, cancelBtn].forEach(b => {
            if (b) b.addEventListener('click', () => {
                if (completeModal) 
                    completeModal.classList.remove('active');
            });
        });

        // Minibar Logging Modal
        const minibarModal = document.getElementById('minibarLogModal');
        const minibarForm = document.getElementById('minibarLogForm');
        let currentMinibarTaskId = null;

        document.addEventListener('click', function(e) {
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
            if (b) b.addEventListener('click', () => {
                if (minibarModal) minibarModal.classList.remove('active');
            });
        });

        if (minibarForm) {
            minibarForm.addEventListener('submit', function(e) {
                e.preventDefault();
                if (!currentMinibarTaskId) return;

                const itemName = document.getElementById('minibarItemSelect').value;
                const quantity = document.getElementById('minibarQtyInput').value;

                const formData = new URLSearchParams();
                formData.append('itemName', itemName);
                formData.append('quantity', quantity);

                fetch('/deepbluehaven/housekeeper/tasks/' + currentMinibarTaskId + '/minibar-log', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: formData.toString()
                })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        showToast('success', 'Minibar Logged', data.message || 'Consumption added to guest bill.');
                        if (minibarModal) minibarModal.classList.remove('active');
                    } else {
                        showToast('error', 'Error', data.message || 'Could not log minibar item.');
                    }
                })
                .catch(err => {
                    showToast('error', 'Error', 'Failed to communicate with server: ' + err.message);
                });
            });
        }
    });
})();

