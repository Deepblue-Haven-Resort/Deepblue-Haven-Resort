/**
 * Room Rack Grid Interactive Logic - DeepBlue Haven PMS
 */
document.addEventListener('DOMContentLoaded', () => {
    const getContextPath = () => {
        const path = window.location.pathname;
        if (path.startsWith('/deepbluehaven/')) return '/deepbluehaven';
        return '';
    };
    const ctx = getContextPath();

    // 1. Filter Rooms by Status
    const filterBtns = document.querySelectorAll('[data-grid-filter]');
    const roomCards = document.querySelectorAll('.room-card');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            filterBtns.forEach(b => {
                b.classList.remove('active', 'btn-cta');
                b.classList.add('btn-outline');
            });
            btn.classList.remove('btn-outline');
            btn.classList.add('active', 'btn-cta');

            const filter = btn.dataset.gridFilter;
            roomCards.forEach(card => {
                const status = (card.dataset.status || '').toUpperCase();
                if (filter === 'ALL' || status === filter) {
                    card.style.display = 'flex';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });

    // 2. Modals Management
    const roomDetailModal = document.getElementById('roomDetailModal');
    const walkInModal = document.getElementById('walkInModal');
    const roomMoveModal = document.getElementById('roomMoveModal');

    const openModal = (modal) => {
        if (modal) modal.classList.add('active');
    };

    const closeModal = (modal) => {
        if (modal) modal.classList.remove('active');
    };

    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const modal = e.target.closest('.modal-overlay');
            closeModal(modal);
        });
    });

    // Close on overlay background click
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                closeModal(overlay);
            }
        });
    });

    // 3. Room Card Click -> Open Detail Modal
    roomCards.forEach(card => {
        card.addEventListener('click', () => {
            const roomId = card.dataset.roomId;
            const roomNumber = card.dataset.roomNumber;
            const roomType = card.dataset.roomType;
            const status = (card.dataset.status || '').toUpperCase();
            const price = card.dataset.price;
            const capacity = card.dataset.capacity;
            const area = card.dataset.area;
            const guestName = card.dataset.guestName;
            const checkOutDate = card.dataset.checkout;
            const bookingId = card.dataset.bookingId;

            // Populate Modal
            const modalRoomNumber = document.getElementById('modalRoomNumber');
            const modalRoomType = document.getElementById('modalRoomType');
            const modalRoomStatus = document.getElementById('modalRoomStatus');
            const modalRoomPrice = document.getElementById('modalRoomPrice');
            const modalRoomCapacity = document.getElementById('modalRoomCapacity');
            const modalOccupantBox = document.getElementById('modalOccupantBox');
            const modalGuestName = document.getElementById('modalGuestName');
            const modalCheckOutDate = document.getElementById('modalCheckOutDate');
            const modalBookBtn = document.getElementById('modalBookBtn');
            const modalRoomMoveBtn = document.getElementById('modalRoomMoveBtn');

            if (modalRoomNumber) modalRoomNumber.innerHTML = `<i class="fa-solid fa-door-open" style="color:#3b5bdb;"></i> Room ${roomNumber}`;
            if (modalRoomType) modalRoomType.textContent = `${roomType} • ${area || '35m²'}`;
            if (modalRoomStatus) {
                modalRoomStatus.textContent = status;
                modalRoomStatus.className = `badge-status ${status.toLowerCase()}`;
            }
            if (modalRoomPrice) modalRoomPrice.textContent = `${price} VND`;
            if (modalRoomCapacity) modalRoomCapacity.textContent = `${capacity || 2} Guests max`;

            if (status === 'OCCUPIED' && guestName && guestName !== '-') {
                if (modalOccupantBox) modalOccupantBox.style.display = 'block';
                if (modalGuestName) modalGuestName.textContent = guestName;
                if (modalCheckOutDate) modalCheckOutDate.textContent = checkOutDate || 'Tomorrow';
                if (modalBookBtn) modalBookBtn.style.display = 'none';
                
                // Show Switch Room Button if bookingId exists
                if (modalRoomMoveBtn) {
                    modalRoomMoveBtn.style.display = 'inline-flex';
                    modalRoomMoveBtn.dataset.bookingId = bookingId || '';
                    modalRoomMoveBtn.dataset.currentRoomNumber = roomNumber;
                    modalRoomMoveBtn.dataset.currentRoomId = roomId;
                    modalRoomMoveBtn.dataset.roomType = roomType;
                }
            } else {
                if (modalOccupantBox) modalOccupantBox.style.display = 'none';
                if (modalBookBtn) {
                    modalBookBtn.style.display = status === 'AVAILABLE' ? 'inline-flex' : 'none';
                }
                if (modalRoomMoveBtn) modalRoomMoveBtn.style.display = 'none';
            }

            openModal(roomDetailModal);
        });
    });

    // 4. Open Walk-in Modal
    document.querySelectorAll('[data-open-walkin]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            closeModal(roomDetailModal);
            openModal(walkInModal);
        });
    });

    // 5. Open Room Move Modal
    const modalRoomMoveBtn = document.getElementById('modalRoomMoveBtn');
    if (modalRoomMoveBtn) {
        modalRoomMoveBtn.addEventListener('click', async () => {
            const bookingId = modalRoomMoveBtn.dataset.bookingId;
            const currentRoomNumber = modalRoomMoveBtn.dataset.currentRoomNumber;
            const currentRoomId = modalRoomMoveBtn.dataset.currentRoomId;
            const roomType = modalRoomMoveBtn.dataset.roomType || '';

            closeModal(roomDetailModal);

            const moveBookingIdInput = document.getElementById('moveBookingId');
            const moveCurrentRoomDisplay = document.getElementById('moveCurrentRoomDisplay');
            const moveDestinationSelect = document.getElementById('moveDestinationSelect');
            const moveReasonTextarea = document.getElementById('moveReasonTextarea');

            if (moveBookingIdInput) moveBookingIdInput.value = bookingId;
            if (moveCurrentRoomDisplay) {
                moveCurrentRoomDisplay.textContent = `Room ${currentRoomNumber} ${roomType ? '• ' + roomType : ''}`;
            }
            if (moveReasonTextarea) moveReasonTextarea.value = '';

            // Fetch available rooms
            if (moveDestinationSelect) {
                moveDestinationSelect.innerHTML = '<option value="" disabled selected>Loading available ready rooms...</option>';
                try {
                    const res = await fetch(`${ctx}/receptionist/api/available-rooms-for-move`);
                    if (res.ok) {
                        const rooms = await res.json();
                        moveDestinationSelect.innerHTML = '<option value="" disabled selected>-- Select Destination Ready Room --</option>';
                        let count = 0;
                        rooms.forEach(r => {
                            if (String(r.roomId) !== String(currentRoomId)) {
                                const opt = document.createElement('option');
                                opt.value = r.roomId;
                                opt.textContent = `Room ${r.roomNumber} (${r.roomTypeName} - ${r.basePriceStr})`;
                                moveDestinationSelect.appendChild(opt);
                                count++;
                            }
                        });
                        if (count === 0) {
                            moveDestinationSelect.innerHTML = '<option value="" disabled selected>No ready vacant rooms available for move</option>';
                        }
                    } else {
                        moveDestinationSelect.innerHTML = '<option value="" disabled selected>Failed to load rooms</option>';
                    }
                } catch (err) {
                    console.error('Error fetching rooms for move:', err);
                    moveDestinationSelect.innerHTML = '<option value="" disabled selected>Error loading available rooms</option>';
                }
            }

            openModal(roomMoveModal);
        });
    }

    // 6. Handle Room Move Form Submission Feedback
    const roomMoveForm = document.querySelector('#roomMoveModal form');
    if (roomMoveForm) {
        roomMoveForm.addEventListener('submit', function (e) {
            const submitBtn = roomMoveForm.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Moving Room...';
            }
        });
    }
});
