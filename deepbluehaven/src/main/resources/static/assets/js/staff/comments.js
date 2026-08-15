/**
 * Staff Feedback & Inquiries Controller
 */
document.addEventListener('DOMContentLoaded', () => {
    'use strict';

    // Tabs navigation
    const tabButtons = document.querySelectorAll('.tab-nav-btn');
    const tabPanes = document.querySelectorAll('.feedback-tab-pane');

    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.dataset.tab;
            tabButtons.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(p => p.classList.remove('active'));

            btn.classList.add('active');
            const targetPane = document.getElementById(targetId);
            if (targetPane) {
                targetPane.classList.add('active');
            }
        });
    });

    // Detect Context Path & Base Route
    const path = window.location.pathname;
    const isManager = path.includes('/manager');
    const baseRoute = isManager ? '/manager' : '/receptionist';
    const contextPrefix = path.startsWith('/deepbluehaven') ? '/deepbluehaven' : '';

    // Reply to Comment Modal
    const replyModal = document.getElementById('replyCommentModal');
    const replyForm = document.getElementById('replyCommentForm');
    const modalCustomerName = document.getElementById('modalCustomerName');
    const modalCustomerContent = document.getElementById('modalCustomerContent');
    const commentReplyText = document.getElementById('commentReplyText');

    document.querySelectorAll('.btn-reply-feedback').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.id;
            const name = btn.dataset.name || 'Guest';
            const content = btn.dataset.content || '';
            const response = btn.dataset.response || '';

            if (modalCustomerName) modalCustomerName.textContent = name;
            if (modalCustomerContent) modalCustomerContent.textContent = content;
            if (commentReplyText) commentReplyText.value = response;

            if (replyForm) {
                replyForm.action = `${contextPrefix}${baseRoute}/comments/${id}/reply`;
            }

            if (replyModal) replyModal.classList.add('active');
        });
    });

    // Follow up Contact Inquiry Modal
    const inqModal = document.getElementById('followUpInquiryModal');
    const inqForm = document.getElementById('followUpInquiryForm');
    const inqModalName = document.getElementById('inqModalName');
    const inqModalContact = document.getElementById('inqModalContact');
    const inqModalSubject = document.getElementById('inqModalSubject');
    const inqModalMessage = document.getElementById('inqModalMessage');
    const inqModalStatus = document.getElementById('inqModalStatus');
    const inqModalNotes = document.getElementById('inqModalNotes');

    document.querySelectorAll('.btn-action-inquiry').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.id;
            const name = btn.dataset.name || '-';
            const email = btn.dataset.email || '';
            const phone = btn.dataset.phone || '';
            const location = btn.dataset.location || 'General';
            const type = btn.dataset.type || 'Inquiry';
            const message = btn.dataset.message || '';
            const status = btn.dataset.status || 'PENDING';
            const notes = btn.dataset.notes || '';

            if (inqModalName) inqModalName.textContent = name;
            if (inqModalContact) inqModalContact.textContent = `${email} ${phone ? `| ${phone}` : ''}`;
            if (inqModalSubject) inqModalSubject.textContent = `${type} (${location})`;
            if (inqModalMessage) inqModalMessage.textContent = message;
            if (inqModalStatus) inqModalStatus.value = status;
            if (inqModalNotes) inqModalNotes.value = notes;

            if (inqForm) {
                inqForm.action = `${contextPrefix}${baseRoute}/inquiries/${id}/update`;
            }

            if (inqModal) inqModal.classList.add('active');
        });
    });

    // Close Modals
    document.querySelectorAll('.modal-close-btn, .btn-modal-cancel').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.modal-backdrop').forEach(m => m.classList.remove('active'));
        });
    });

    // Click outside modal dialog to close
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.addEventListener('click', (e) => {
            if (e.target === backdrop) {
                backdrop.classList.remove('active');
            }
        });
    });
});
