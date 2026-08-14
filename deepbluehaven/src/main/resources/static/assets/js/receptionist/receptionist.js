document.addEventListener('DOMContentLoaded', () => {

    // 1. Dynamic Table Pagination Engine
    function setupTablePagination() {
        const tables = document.querySelectorAll('.activity-table');
        tables.forEach((table, index) => {
            const tbody = table.querySelector('tbody');
            if (!tbody) return;

            const allRows = Array.from(tbody.querySelectorAll('tr'));
            if (allRows.length === 0) return;

            const rowsPerPage = 6;
            let currentPage = 1;

            // Create Pagination Control Bar
            const tableWrapper = table.closest('.table-wrapper') || table.parentElement;
            let paginationBar = tableWrapper.parentElement.querySelector('.pagination-bar');
            if (!paginationBar) {
                paginationBar = document.createElement('div');
                paginationBar.className = 'pagination-bar';
                paginationBar.style.cssText = 'display: flex; align-items: center; justify-content: space-between; margin-top: 18px; padding-top: 14px; border-top: 1px solid #f1f5f9; font-size: 0.875rem; color: #64748b;';
                tableWrapper.parentElement.appendChild(paginationBar);
            }

            function renderPage(page) {
                currentPage = page;
                const visibleRows = allRows.filter(r => r.dataset.filteredOut !== 'true');
                const totalRows = visibleRows.length;
                const totalPages = Math.ceil(totalRows / rowsPerPage) || 1;

                if (currentPage > totalPages) currentPage = totalPages;
                if (currentPage < 1) currentPage = 1;

                const startIdx = (currentPage - 1) * rowsPerPage;
                const endIdx = startIdx + rowsPerPage;

                visibleRows.forEach((row, i) => {
                    if (i >= startIdx && i < endIdx) {
                        row.style.display = '';
                    } else {
                        row.style.display = 'none';
                    }
                });

                // Hide rows that were filtered out by search
                allRows.forEach(row => {
                    if (row.dataset.filteredOut === 'true') {
                        row.style.display = 'none';
                    }
                });

                const startNum = totalRows === 0 ? 0 : startIdx + 1;
                const endNum = Math.min(endIdx, totalRows);

                let buttonsHtml = '';
                for (let p = 1; p <= totalPages; p++) {
                    buttonsHtml += `<button type="button" class="btn ${p === currentPage ? 'btn-cta' : 'btn-outline'} btn-small page-num-btn" data-page="${p}">${p}</button>`;
                }

                paginationBar.innerHTML = `
                    <div>Showing <strong>${startNum}-${endNum}</strong> of <strong>${totalRows}</strong> entries</div>
                    <div style="display: flex; gap: 8px; align-items: center;">
                        <button type="button" class="btn btn-outline btn-small page-prev-btn" ${currentPage === 1 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}><i class="fa-solid fa-chevron-left"></i> Prev</button>
                        <div style="display: flex; gap: 4px;">${buttonsHtml}</div>
                        <button type="button" class="btn btn-outline btn-small page-next-btn" ${currentPage === totalPages || totalPages === 0 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>Next <i class="fa-solid fa-chevron-right"></i></button>
                    </div>
                `;

                // Wire Button Events
                const prevBtn = paginationBar.querySelector('.page-prev-btn');
                if (prevBtn && currentPage > 1) {
                    prevBtn.addEventListener('click', () => renderPage(currentPage - 1));
                }

                const nextBtn = paginationBar.querySelector('.page-next-btn');
                if (nextBtn && currentPage < totalPages) {
                    nextBtn.addEventListener('click', () => renderPage(currentPage + 1));
                }

                paginationBar.querySelectorAll('.page-num-btn').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const targetPage = parseInt(btn.getAttribute('data-page'), 10);
                        renderPage(targetPage);
                    });
                });
            }

            renderPage(1);

            // Integrate with Search Filter Input
            const searchInput = document.getElementById('tableSearchInput');
            if (searchInput) {
                searchInput.addEventListener('input', (e) => {
                    const query = e.target.value.toLowerCase().trim();
                    allRows.forEach(row => {
                        const text = row.innerText.toLowerCase();
                        if (query === '' || text.includes(query)) {
                            delete row.dataset.filteredOut;
                        } else {
                            row.dataset.filteredOut = 'true';
                        }
                    });
                    renderPage(1);
                });
            }
        });
    }

    setupTablePagination();

    // 2. Room Rack Grid Status Filter Buttons
    const filterButtons = document.querySelectorAll('[data-grid-filter]');
    const roomCards = document.querySelectorAll('.room-card');

    if (filterButtons.length > 0 && roomCards.length > 0) {
        filterButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const status = btn.getAttribute('data-grid-filter');

                filterButtons.forEach(b => {
                    b.classList.remove('active', 'btn-cta');
                    b.classList.add('btn-outline');
                });
                btn.classList.remove('btn-outline');
                btn.classList.add('active', 'btn-cta');

                roomCards.forEach(card => {
                    if (status === 'ALL' || card.classList.contains(status.toLowerCase())) {
                        card.style.display = 'flex';
                    } else {
                        card.style.display = 'none';
                    }
                });
            });
        });
    }

    // 3. Modal Controls
    const walkInModal = document.getElementById('walkInModal');
    const roomDetailModal = document.getElementById('roomDetailModal');
    const checkoutConfirmModal = document.getElementById('checkoutConfirmModal');

    const openWalkInBtns = document.querySelectorAll('[data-open-walkin]');
    const openCheckoutBtns = document.querySelectorAll('[data-open-checkout-modal]');
    const closeModalBtns = document.querySelectorAll('[data-close-modal]');

    // Open Walk-In Modal
    openWalkInBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const preselectedRoomId = btn.getAttribute('data-room-id');
            if (preselectedRoomId && walkInModal) {
                const selectRoom = walkInModal.querySelector('select[name="roomId"]');
                if (selectRoom) selectRoom.value = preselectedRoomId;
            }
            if (roomDetailModal) roomDetailModal.classList.remove('active');
            if (walkInModal) walkInModal.classList.add('active');
        });
    });

    // Open Check-Out Confirmation Modal
    openCheckoutBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const tr = btn.closest('tr');
            if (!tr || !checkoutConfirmModal) return;

            const bookingId = tr.getAttribute('data-booking-id');
            const bookingCode = tr.getAttribute('data-booking-code');
            const guestName = tr.getAttribute('data-customer-name');
            const roomNum = tr.getAttribute('data-room-number');
            const stayDates = tr.getAttribute('data-stay-dates');
            const roomCharge = tr.getAttribute('data-room-charge');
            const pricingNote = tr.getAttribute('data-pricing-note');
            const serviceCharge = tr.getAttribute('data-service-charge');
            const discountCode = tr.getAttribute('data-discount-code');
            const discountAmount = tr.getAttribute('data-discount-amount');
            const tax = tr.getAttribute('data-tax');
            const totalFolio = tr.getAttribute('data-total-folio');

            document.getElementById('coModalBookingId').value = bookingId;
            document.getElementById('coModalBookingRef').innerText = bookingCode;
            document.getElementById('coModalGuestName').innerText = guestName;
            document.getElementById('coModalRoomNum').innerText = 'Room ' + roomNum;
            document.getElementById('coModalStayDates').innerText = stayDates;
            document.getElementById('coModalRoomCharge').innerText = roomCharge;
            document.getElementById('coModalPricingNote').innerText = pricingNote;
            document.getElementById('coModalServiceCharge').innerText = serviceCharge;

            const discountRow = document.getElementById('coModalDiscountRow');
            if (discountCode && discountCode !== 'None' && discountAmount !== '0 VND') {
                discountRow.style.display = 'table-row';
                document.getElementById('coModalDiscountCode').innerText = discountCode;
                const formattedDiscount = discountAmount.startsWith('-') ? discountAmount : '-' + discountAmount;
                document.getElementById('coModalDiscountAmount').innerText = formattedDiscount;
            } else {
                discountRow.style.display = 'none';
            }

            document.getElementById('coModalTax').innerText = tax;
            document.getElementById('coModalTotalFolio').innerText = totalFolio;

            checkoutConfirmModal.classList.add('active');
        });
    });

    // Close Modals
    closeModalBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            if (walkInModal) walkInModal.classList.remove('active');
            if (roomDetailModal) roomDetailModal.classList.remove('active');
            if (checkoutConfirmModal) checkoutConfirmModal.classList.remove('active');
        });
    });

    // Close on backdrop click
    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
        backdrop.addEventListener('click', (e) => {
            if (e.target === backdrop) {
                backdrop.classList.remove('active');
            }
        });
    });

    // Room Card Click -> Open Room Details Popup
    roomCards.forEach(card => {
        card.addEventListener('click', () => {
            const roomId = card.getAttribute('data-room-id');
            const roomNum = card.getAttribute('data-room-number');
            const roomType = card.getAttribute('data-room-type');
            const status = card.getAttribute('data-status');
            const price = card.getAttribute('data-price');
            const capacity = card.getAttribute('data-capacity');
            const area = card.getAttribute('data-area');
            const guestName = card.getAttribute('data-guest-name');
            const checkOut = card.getAttribute('data-checkout');

            if (!roomDetailModal) return;

            document.getElementById('modalRoomNumber').innerText = 'Room ' + roomNum;
            document.getElementById('modalRoomType').innerText = roomType + ' • ' + area;
            document.getElementById('modalRoomPrice').innerText = price + ' VND / night';
            document.getElementById('modalRoomCapacity').innerText = capacity + ' Guests max';
            
            const statusBadge = document.getElementById('modalRoomStatus');
            statusBadge.innerText = status;
            statusBadge.className = 'badge-status ' + status.toLowerCase();

            const occupantBox = document.getElementById('modalOccupantBox');
            if (status === 'OCCUPIED' && guestName && guestName !== '-') {
                occupantBox.style.display = 'block';
                document.getElementById('modalGuestName').innerText = guestName;
                document.getElementById('modalCheckOutDate').innerText = checkOut;
            } else {
                occupantBox.style.display = 'none';
            }

            const bookBtn = document.getElementById('modalBookBtn');
            if (status === 'AVAILABLE') {
                bookBtn.style.display = 'inline-flex';
                bookBtn.setAttribute('data-room-id', roomId);
            } else {
                bookBtn.style.display = 'none';
            }

            roomDetailModal.classList.add('active');
        });
    });
});
