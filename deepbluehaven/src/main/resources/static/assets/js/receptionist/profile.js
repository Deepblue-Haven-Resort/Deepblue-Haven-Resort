/**
 * Receptionist Profile & Password Management
 */
document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('changePasswordModal');
    const openBtn = document.getElementById('openChangePasswordBtn');
    const closeBtn = document.getElementById('closeChangePasswordBtn');
    const cancelBtn = document.getElementById('cancelChangePasswordBtn');
    const form = document.getElementById('changePasswordForm');

    function resetPasswordToggles() {
        document.querySelectorAll('.toggle-password-visibility-btn').forEach(btn => {
            const targetId = btn.getAttribute('data-target');
            const input = document.getElementById(targetId);
            const icon = btn.querySelector('i');
            if (input) input.type = 'password';
            if (icon) {
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            }
            btn.style.color = '#94a3b8';
        });
    }

    const openModal = () => {
        if (!modal) return;
        modal.classList.add('active');
        if (form) form.reset();
        resetPasswordToggles();
    };

    const closeModal = () => {
        if (!modal) return;
        modal.classList.remove('active');
        resetPasswordToggles();
    };

    if (openBtn) openBtn.addEventListener('click', openModal);
    if (closeBtn) closeBtn.addEventListener('click', closeModal);
    if (cancelBtn) cancelBtn.addEventListener('click', closeModal);

    // Close on backdrop click
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeModal();
            }
        });
    }

    // Toggle password visibility
    document.querySelectorAll('.toggle-password-visibility-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-target');
            const input = document.getElementById(targetId);
            const icon = btn.querySelector('i');
            if (input && icon) {
                if (input.type === 'password') {
                    input.type = 'text';
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                    btn.style.color = '#0284c7';
                } else {
                    input.type = 'password';
                    icon.classList.remove('fa-eye');
                    icon.classList.add('fa-eye-slash');
                    btn.style.color = '#94a3b8';
                }
            }
        });
    });

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const currentPassword = document.getElementById('currentPassword').value;
            const newPassword = document.getElementById('newPassword').value;
            const confirmNewPassword = document.getElementById('confirmNewPassword').value;

            if (newPassword !== confirmNewPassword) {
                alert('New passwords do not match!');
                return;
            }

            const submitBtn = document.getElementById('submitChangePasswordBtn');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Updating...';
            }

            try {
                const basePath = window.location.pathname.startsWith('/deepbluehaven') ? '/deepbluehaven' : '';
                const res = await fetch(basePath + '/api/auth/change-password', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ currentPassword, newPassword })
                });
                const data = await res.json();
                if (res.ok && data.success) {
                    alert(data.message || 'Password updated successfully!');
                    closeModal();
                } else {
                    alert(data.message || 'Failed to change password.');
                }
            } catch (err) {
                console.error('Change password error:', err);
                alert('Network error while changing password.');
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fa-solid fa-key"></i> Update Password';
                }
            }
        });
    }
});
