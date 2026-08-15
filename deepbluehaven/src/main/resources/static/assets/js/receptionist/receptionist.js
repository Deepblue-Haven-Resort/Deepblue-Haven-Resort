document.addEventListener('DOMContentLoaded', () => {

    // 1. Dynamic Table Pagination Engine
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

            // Create Pagination Control Bar
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

            // Search input integration
            const panel = tableWrapper.closest('.reception-panel, .account-panel') || tableWrapper.parentElement;
            const searchInput = panel ? panel.querySelector('#tableSearchInput, .search-box input') : document.getElementById('tableSearchInput');
            if (searchInput && !searchInput.dataset.listening) {
                searchInput.dataset.listening = 'true';
                searchInput.addEventListener('input', (e) => {
                    currentKeyword = e.target.value.toLowerCase().trim();
                    applyFilters();
                });
            }

            // Dropdown status filter integration
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
