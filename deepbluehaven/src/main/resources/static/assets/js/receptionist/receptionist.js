document.addEventListener('DOMContentLoaded', () => {

    function setupTablePagination() {
        const tables = document.querySelectorAll('.activity-table, .account-table');
        tables.forEach((table, index) => {
            const tbody = table.querySelector('tbody');
            if (!tbody) return;

            const allRows = Array.from(tbody.querySelectorAll('tr')).filter(r => !r.querySelector('td[colspan]'));
            if (allRows.length === 0) return;

            const pageSizeAttr = table.getAttribute('data-page-size');
            const rowsPerPage = pageSizeAttr ? parseInt(pageSizeAttr, 10) : 6;
            let currentPage = 1;
            let currentKeyword = '';
            let currentStatus = 'ALL';

            const tableWrapper = table.closest('.table-wrapper') || table.parentElement;
            let paginationBar = tableWrapper.parentElement.querySelector('.pagination-bar');
            if (!paginationBar) {
                paginationBar = document.createElement('div');
                paginationBar.className = 'pagination-bar';
                paginationBar.style.cssText = 'display: flex; align-items: center; justify-content: space-between; margin-top: 18px; padding-top: 14px; border-top: 1px solid #f1f5f9; font-size: 0.875rem; color: #64748b;';
                tableWrapper.parentElement.appendChild(paginationBar);
            }

            function applyFilters() {
                allRows.forEach(row => {
                    const text = row.innerText.toLowerCase();
                    const matchesSearch = !currentKeyword || text.includes(currentKeyword);
                    const rowStatus = (row.getAttribute('data-status') || '').toUpperCase();
                    const statusText = (row.querySelector('.status, .badge, .status-badge, .badge-status')?.textContent || '').toUpperCase();

                    let matchesStatus = true;
                    if (currentStatus !== 'ALL') {
                        matchesStatus = rowStatus.includes(currentStatus) || statusText.includes(currentStatus);
                    }

                    if (matchesSearch && matchesStatus) {
                        delete row.dataset.filteredOut;
                    } else {
                        row.dataset.filteredOut = 'true';
                    }
                });
                renderPage(1);
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

                allRows.forEach(r => r.style.display = 'none');
                visibleRows.forEach((row, i) => {
                    if (i >= startIdx && i < endIdx) {
                        row.style.display = '';
                    } else {
                        row.style.display = 'none';
                    }
                });

                const startNum = totalRows === 0 ? 0 : startIdx + 1;
                const endNum = Math.min(endIdx, totalRows);

                function getPageItems(total, current) {
                    if (total <= 7) {
                        const arr = [];
                        for (let i = 1; i <= total; i++) arr.push(i);
                        return arr;
                    }
                    const pages = new Set();
                    pages.add(1);
                    pages.add(2);
                    pages.add(3);
                    for (let i = current - 1; i <= current + 1; i++) {
                        if (i >= 1 && i <= total) {
                            pages.add(i);
                        }
                    }
                    pages.add(total - 2);
                    pages.add(total - 1);
                    pages.add(total);

                    const sorted = Array.from(pages).sort((a, b) => a - b);
                    const result = [];
                    let prev = 0;
                    for (const p of sorted) {
                        if (prev > 0) {
                            if (p - prev === 2) {
                                result.push(prev + 1);
                            } else if (p - prev > 2) {
                                result.push('...');
                            }
                        }
                        result.push(p);
                        prev = p;
                    }
                    return result;
                }

                let buttonsHtml = '';
                const pageItems = getPageItems(totalPages, currentPage);
                pageItems.forEach(item => {
                    if (item === '...') {
                        buttonsHtml += `<span class="page-dots" style="display: inline-flex; align-items: center; justify-content: center; min-width: 24px; padding: 0 4px; color: #94a3b8; font-weight: bold;">...</span>`;
                    } else {
                        buttonsHtml += `<button type="button" class="btn ${item === currentPage ? 'btn-cta' : 'btn-outline'} btn-small page-num-btn" data-page="${item}">${item}</button>`;
                    }
                });

                let jumpHtml = '';
                if (totalPages > 7) {
                    jumpHtml = `
                        <div class="pagination-jump" style="display: flex; align-items: center; gap: 6px; margin-left: 10px;">
                            <span style="font-size: 0.8125rem; color: #64748b; white-space: nowrap;">Go to:</span>
                            <input type="number" min="1" max="${totalPages}" class="form-input page-jump-input" style="width: 52px; height: 32px; text-align: center; font-size: 0.8125rem; border-radius: 6px; padding: 2px 4px; border: 1px solid #cbd5e1;" placeholder="${currentPage}" />
                            <button type="button" class="btn btn-outline btn-small page-jump-btn" style="height: 32px; padding: 0 10px; font-size: 0.8125rem;">Go</button>
                        </div>
                    `;
                }

                paginationBar.innerHTML = `
                    <div>Showing <strong>${startNum}-${endNum}</strong> of <strong>${totalRows}</strong> entries</div>
                    <div style="display: flex; gap: 8px; align-items: center; flex-wrap: wrap;">
                        <button type="button" class="btn btn-outline btn-small page-prev-btn" ${currentPage === 1 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}><i class="fa-solid fa-chevron-left"></i> Prev</button>
                        <div style="display: flex; gap: 4px; align-items: center;">${buttonsHtml}</div>
                        <button type="button" class="btn btn-outline btn-small page-next-btn" ${currentPage === totalPages || totalPages === 0 ? 'disabled style="opacity: 0.5; cursor: not-allowed;"' : ''}>Next <i class="fa-solid fa-chevron-right"></i></button>
                        ${jumpHtml}
                    </div>
                `;

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

                const jumpInput = paginationBar.querySelector('.page-jump-input');
                const jumpBtn = paginationBar.querySelector('.page-jump-btn');
                if (jumpInput && jumpBtn) {
                    const doJump = () => {
                        const val = parseInt(jumpInput.value, 10);
                        if (!isNaN(val) && val >= 1 && val <= totalPages) {
                            renderPage(val);
                        } else {
                            jumpInput.value = '';
                        }
                    };
                    jumpBtn.addEventListener('click', doJump);
                    jumpInput.addEventListener('keydown', (e) => {
                        if (e.key === 'Enter') {
                            e.preventDefault();
                            doJump();
                        }
                    });
                }
            }

            renderPage(1);

            const panel = tableWrapper.closest('.reception-panel, .account-panel') || tableWrapper.parentElement;
            const searchInput = panel ? panel.querySelector('#tableSearchInput, .search-box input') : document.getElementById('tableSearchInput');
            if (searchInput && !searchInput.dataset.listening) {
                searchInput.dataset.listening = 'true';
                searchInput.addEventListener('input', (e) => {
                    currentKeyword = e.target.value.toLowerCase().trim();
                    applyFilters();
                });
            }

            const dropdown = panel ? panel.querySelector('.filter-dropdown') : null;
            if (dropdown && !dropdown.dataset.listening) {
                dropdown.dataset.listening = 'true';
                const filterBtn = dropdown.querySelector('.filter-btn');
                const menu = dropdown.querySelector('.filter-menu');
                const label = dropdown.querySelector('.filter-label strong');

                if (filterBtn) {
                    filterBtn.addEventListener('click', (e) => {
                        e.stopPropagation();
                        dropdown.classList.toggle('active');
                    });
                }

                if (menu) {
                    menu.querySelectorAll('button').forEach(btn => {
                        btn.addEventListener('click', () => {
                            const text = btn.textContent.trim();
                            const statusVal = btn.getAttribute('data-status') || text.toUpperCase();
                            if (label) label.textContent = text;
                            dropdown.classList.remove('active');
                            currentStatus = statusVal;
                            applyFilters();
                        });
                    });
                }

                document.addEventListener('click', () => {
                    dropdown.classList.remove('active');
                });
            }
        });
    }

    setupTablePagination();

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

    const walkInModal = document.getElementById('walkInModal');
    const roomDetailModal = document.getElementById('roomDetailModal');
    const checkoutConfirmModal = document.getElementById('checkoutConfirmModal');

    const openWalkInBtns = document.querySelectorAll('[data-open-walkin]');
    const openCheckoutBtns = document.querySelectorAll('[data-open-checkout-modal]');
    const closeModalBtns = document.querySelectorAll('[data-close-modal]');

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
            const depositPaid = tr.getAttribute('data-deposit-paid');
            const remainingPayable = tr.getAttribute('data-remaining-payable');
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
            if (discountRow) {
                if (discountCode && discountCode !== 'None' && discountAmount !== '0 VND') {
                    discountRow.style.display = 'table-row';
                    document.getElementById('coModalDiscountCode').innerText = discountCode;
                    const formattedDiscount = discountAmount.startsWith('-') ? discountAmount : '-' + discountAmount;
                    document.getElementById('coModalDiscountAmount').innerText = formattedDiscount;
                } else {
                    discountRow.style.display = 'none';
                }
            }

            const depositRow = document.getElementById('coModalDepositRow');
            if (depositRow) {
                if (depositPaid && depositPaid !== '0 VND' && depositPaid !== 'null') {
                    depositRow.style.display = 'table-row';
                    const formattedDeposit = depositPaid.startsWith('-') ? depositPaid : '-' + depositPaid;
                    document.getElementById('coModalDepositPaid').innerText = formattedDeposit;
                } else {
                    depositRow.style.display = 'none';
                }
            }

            const taxRow = document.getElementById('coModalTaxRow');
            if (taxRow) {
                if (tax && tax !== '0 VND' && tax !== '0.00 VND') {
                    taxRow.style.display = 'table-row';
                    document.getElementById('coModalTax').innerText = tax;
                } else {
                    taxRow.style.display = 'none';
                }
            }

            document.getElementById('coModalTotalFolio').innerText = remainingPayable || totalFolio;

            checkoutConfirmModal.classList.add('active');
        });
    });

    const reservationDetailModal = document.getElementById('reservationDetailModal');
    document.addEventListener('click', (e) => {
        const viewTrigger = e.target.closest('.btn-view-reservation, .booking-ref-link');
        if (!viewTrigger || !reservationDetailModal) return;

        const row = viewTrigger.closest('tr');
        if (!row) return;

        const bookingId = row.getAttribute('data-booking-id') || '';
        const code = row.getAttribute('data-booking-code') || '';
        const guestName = row.getAttribute('data-guest-name') || '';
        const guestEmail = row.getAttribute('data-guest-email') || '';
        const guestPhone = row.getAttribute('data-guest-phone') || 'N/A';
        const roomType = row.getAttribute('data-room-type') || '';
        const roomNumber = row.getAttribute('data-room-number') || '';
        const checkIn = row.getAttribute('data-check-in') || '';
        const checkOut = row.getAttribute('data-check-out') || '';
        const nights = row.getAttribute('data-nights') || '1';
        const status = (row.getAttribute('data-status') || 'PENDING').toUpperCase();
        const statusText = row.getAttribute('data-status-text') || 'Pending';
        const roomCharge = row.getAttribute('data-room-charge') || '—';
        const serviceCharge = row.getAttribute('data-service-charge') || '—';
        const totalAmount = row.getAttribute('data-total-amount') || '0 VND';
        const specialRequest = row.getAttribute('data-special-request') || '';

        const elCode = document.getElementById('modalBookingRef') || document.getElementById('modalBookingCode');
        if (elCode) elCode.innerText = code;

        const elGuestName = document.getElementById('modalGuestName');
        if (elGuestName) elGuestName.innerText = guestName;

        const elGuestEmail = document.getElementById('modalGuestEmail');
        if (elGuestEmail) elGuestEmail.innerText = guestEmail;

        const elGuestPhone = document.getElementById('modalGuestPhone');
        if (elGuestPhone) elGuestPhone.innerText = guestPhone;

        const elRoomInfo = document.getElementById('modalRoomInfo');
        if (elRoomInfo) elRoomInfo.innerText = roomType + (roomNumber ? ' (' + roomNumber + ')' : '');

        const elStayDates = document.getElementById('modalStayDates');
        if (elStayDates) elStayDates.innerText = checkIn + ' → ' + checkOut + ' (' + nights + ' nights)';

        const statusBadge = document.getElementById('modalBookingStatus');
        if (statusBadge) {
            statusBadge.innerText = statusText;
            statusBadge.className = 'status-badge status-' + status.toLowerCase().replace('_', '-');
        }

        const elRoomCharge = document.getElementById('modalRoomCharge');
        if (elRoomCharge) elRoomCharge.innerText = roomCharge;

        const elServiceCharge = document.getElementById('modalServiceCharge');
        if (elServiceCharge) elServiceCharge.innerText = serviceCharge;

        const roomChargeNum = roomCharge ? parseInt(roomCharge.replace(/[^0-9]/g, '') || '0', 10) : 0;
        const serviceChargeNum = serviceCharge ? parseInt(serviceCharge.replace(/[^0-9]/g, '') || '0', 10) : 0;
        const subtotal = roomChargeNum + serviceChargeNum;
        const vatTaxNum = Math.round(subtotal * 0.08);
        const totalNum = subtotal + vatTaxNum;

        const elVatTax = document.getElementById('modalVatTax');
        if (elVatTax) elVatTax.innerText = vatTaxNum.toLocaleString('vi-VN') + ' VND';

        const elTotalAmount = document.getElementById('modalTotalAmount');
        if (elTotalAmount) elTotalAmount.innerText = totalNum.toLocaleString('vi-VN') + ' VND';

        const depositNum = Math.round(totalNum * 0.3);
        const remainingNum = totalNum - depositNum;

        const elDepositLabel = document.getElementById('modalDepositLabel');
        const elDeposit = document.getElementById('modalDepositAmount');
        const elRemainingLabel = document.getElementById('modalRemainingLabel');
        const elRemaining = document.getElementById('modalRemainingAmount');

        if (status === 'PENDING') {
            if (elDepositLabel) elDepositLabel.textContent = 'Deposit Required (30%):';
            if (elDeposit) elDeposit.innerHTML = `${depositNum.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--pending">Unpaid</small>`;
            if (elRemainingLabel) elRemainingLabel.textContent = 'Remaining Balance (70%):';
            if (elRemaining) elRemaining.textContent = remainingNum.toLocaleString('vi-VN') + ' VND';
        } else if (status === 'CONFIRMED' || status === 'CHECKED_IN') {
            if (elDepositLabel) elDepositLabel.textContent = 'Deposit (30% - VNPay):';
            if (elDeposit) elDeposit.innerHTML = `${depositNum.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--confirmed"><i class="fa-solid fa-check"></i> Paid</small>`;
            if (elRemainingLabel) elRemainingLabel.textContent = 'Remaining Balance at Check-out (70%):';
            if (elRemaining) elRemaining.innerHTML = `${remainingNum.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--due">Due at Check-out</small>`;
        } else {
            if (elDepositLabel) elDepositLabel.textContent = 'Deposit (30%):';
            if (elDeposit) elDeposit.innerHTML = `${depositNum.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--confirmed"><i class="fa-solid fa-check"></i> Paid</small>`;
            if (elRemainingLabel) elRemainingLabel.textContent = 'Remaining Balance (70%):';
            if (elRemaining) elRemaining.innerHTML = `${remainingNum.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--confirmed"><i class="fa-solid fa-check"></i> Settled</small>`;
        }

        const reqBox = document.getElementById('modalSpecialRequestBox');
        const reqText = document.getElementById('modalSpecialRequestText');
        if (reqBox && reqText) {
            if (specialRequest && specialRequest.trim() !== '' && specialRequest !== 'null') {
                reqText.innerText = specialRequest;
                reqBox.style.display = 'block';
            } else {
                reqBox.style.display = 'none';
            }
        }

        const confirmForm = document.getElementById('modalConfirmForm');
        if (confirmForm) {
            if (status === 'PENDING' && bookingId) {
                const isManager = window.location.pathname.includes('/manager');
                const basePath = isManager ? '/deepbluehaven/manager' : '/deepbluehaven/receptionist';
                confirmForm.action = basePath + '/confirm-booking/' + bookingId;
                confirmForm.style.display = 'block';
            } else {
                confirmForm.style.display = 'none';
            }
        }

        reservationDetailModal.classList.add('active');
    });

    document.addEventListener('click', (e) => {
        if (e.target.closest('.modal-close-btn, .modal-close, [data-close-modal], .btn-close')) {
            document.querySelectorAll('.modal-backdrop.active').forEach(m => m.classList.remove('active'));
            return;
        }
        if (e.target.classList.contains('modal-backdrop')) {
            e.target.classList.remove('active');
        }
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-backdrop.active').forEach(m => m.classList.remove('active'));
        }
    });

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
