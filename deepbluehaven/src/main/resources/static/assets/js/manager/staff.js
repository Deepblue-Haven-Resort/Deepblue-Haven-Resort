/**
 * Deep Blue Haven - Manager Staff Management Script
 */
document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    const registerStaffModal = document.getElementById('registerStaffModal');
    const openRegisterStaffBtn = document.querySelector('[data-open-staff-modal]');

    if (openRegisterStaffBtn && registerStaffModal) {
        openRegisterStaffBtn.addEventListener('click', () => {
            registerStaffModal.classList.add('active');
        });
    }

    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.modal-overlay.active').forEach(m => m.classList.remove('active'));
        });
    });

    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) {
            e.target.classList.remove('active');
        }
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.active').forEach(m => m.classList.remove('active'));
        }
    });
});
