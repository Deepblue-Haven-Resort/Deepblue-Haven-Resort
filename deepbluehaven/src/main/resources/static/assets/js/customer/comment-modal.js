document.addEventListener('DOMContentLoaded', () => {
    const commentModal = document.getElementById('commentModal');
    const closeCommentModalBtn = document.getElementById('closeCommentModalBtn');
    const cancelCommentModalBtn = document.getElementById('cancelCommentModalBtn');
    const commentForm = document.getElementById('commentForm');
    const starContainer = document.getElementById('starRatingContainer');
    const ratingInput = document.getElementById('commentRatingInput');

    // Close Modal helpers
    const closeCommentModal = () => {
        if (commentModal) {
            commentModal.classList.remove('active');
            commentModal.style.display = 'none';
        }
    };

    if (closeCommentModalBtn) closeCommentModalBtn.addEventListener('click', closeCommentModal);
    if (cancelCommentModalBtn) cancelCommentModalBtn.addEventListener('click', closeCommentModal);

    if (commentModal) {
        commentModal.addEventListener('click', (e) => {
            if (e.target === commentModal) {
                closeCommentModal();
            }
        });
    }

    // Open Comment Modal from Review button
    document.querySelectorAll('.js-open-comment-modal').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const bookingCode = btn.getAttribute('data-booking-code') || '';
            const roomName = btn.getAttribute('data-room-name') || 'Resort Stay';
            const roomId = btn.getAttribute('data-room-id') || '';

            if (commentForm) commentForm.reset();

            const roomIdInput = document.getElementById('commentRoomId');
            if (roomIdInput) roomIdInput.value = roomId;

            const roomNameElem = document.getElementById('commentModalRoomName');
            if (roomNameElem) roomNameElem.textContent = roomName + ' (' + bookingCode + ')';

            // Reset Star Rating to 5
            setRating(5);

            if (commentModal) {
                commentModal.style.display = 'flex';
                commentModal.classList.add('active');
            }
        });
    });

    // Star Rating Selection
    if (starContainer) {
        const stars = starContainer.querySelectorAll('.star-icon');
        stars.forEach(star => {
            star.addEventListener('click', () => {
                const val = parseInt(star.getAttribute('data-rating'), 10) || 5;
                setRating(val);
            });
        });
    }

    function setRating(val) {
        if (ratingInput) ratingInput.value = val;
        if (starContainer) {
            const stars = starContainer.querySelectorAll('.star-icon');
            stars.forEach(star => {
                const starVal = parseInt(star.getAttribute('data-rating'), 10);
                if (starVal <= val) {
                    star.classList.add('selected');
                    star.classList.remove('fa-regular');
                    star.classList.add('fa-solid');
                } else {
                    star.classList.remove('selected');
                    star.classList.remove('fa-solid');
                    star.classList.add('fa-regular');
                }
            });
        }
    }
});
