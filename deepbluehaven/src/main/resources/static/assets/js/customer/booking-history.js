document.addEventListener("DOMContentLoaded", function () {

    function getApiUrl(path) {
        let ctx = document.querySelector('meta[name="_context_path"]')?.content;
        if (!ctx || ctx === "/") {
            const match = window.location.pathname.match(/^\/([^\/]+)/);
            ctx = (match && match[1] === "deepbluehaven") ? "/" + match[1] : "";
        }
        return ctx.replace(/\/$/, "") + (path.startsWith("/") ? path : "/" + path);
    }

    function escapeHtml(str) {
        if (!str) return "";
        return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
    }

    function notifyToast(type, title, message) {
        if (typeof showToast === "function") {
            showToast(type, title, message);
        } else {
            console.log(`[${type.toUpperCase()}] ${title}: ${message}`);
        }
    }

    const filterButtons = document.querySelectorAll(".booking-filter-tab");
    const bookingCards = Array.from(document.querySelectorAll(".booking-history-card"));
    const searchInput = document.getElementById("bookingSearch");
    const emptyState = document.getElementById("bookingHistoryEmpty");
    const pagination = document.getElementById("bookingPagination");
    const paginationNumbers = document.getElementById("bookingPaginationNumbers");
    const previousButton = document.getElementById("bookingPrevPage");
    const nextButton = document.getElementById("bookingNextPage");

    let currentFilter = "all";
    let currentPage = 1;
    const itemsPerPage = 5;

    function updateFilterBadges() {
        const counts = {
            all: bookingCards.length,
            upcoming: 0,
            current: 0,
            completed: 0,
            cancelled: 0
        };

        bookingCards.forEach(function (card) {
            const status = card.dataset.status || "";
            if (counts.hasOwnProperty(status)) {
                counts[status]++;
            }
        });

        filterButtons.forEach(function (button) {
            const filter = button.dataset.filter || "all";
            const badge = button.querySelector("span");
            if (badge && counts.hasOwnProperty(filter)) {
                badge.textContent = counts[filter];
            }
        });
    }

    function getFilteredBookings() {
        const keyword = searchInput ? searchInput.value.trim().toLowerCase() : "";

        return bookingCards.filter(function (card) {
            const status = card.dataset.status || "";
            const searchContent = (card.dataset.search || "").toLowerCase();
            const matchesFilter = currentFilter === "all" || status === currentFilter;
            const matchesSearch = searchContent.includes(keyword);

            return matchesFilter && matchesSearch;
        });
    }

    function renderPagination(totalPages) {
        if (!pagination || !paginationNumbers) return;

        paginationNumbers.innerHTML = "";
        pagination.hidden = totalPages <= 1;

        if (totalPages <= 1) return;

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

        const items = getPageItems(totalPages, currentPage);
        items.forEach(item => {
            if (item === '...') {
                const dots = document.createElement("span");
                dots.className = "booking-pagination__dots";
                dots.style.cssText = "display: inline-flex; align-items: center; justify-content: center; min-width: 28px; padding: 0 4px; color: #94a3b8; font-weight: bold;";
                dots.textContent = "...";
                paginationNumbers.appendChild(dots);
            } else {
                const pageButton = document.createElement("button");
                pageButton.type = "button";
                pageButton.className = "booking-pagination__number";
                pageButton.textContent = item;
                pageButton.setAttribute("aria-label", `Go to page ${item}`);

                if (item === currentPage) {
                    pageButton.classList.add("is-active");
                    pageButton.setAttribute("aria-current", "page");
                }

                pageButton.addEventListener("click", function () {
                    currentPage = item;
                    updateBookingList();
                });

                paginationNumbers.appendChild(pageButton);
            }
        });

        // Jump to page input if totalPages > 7
        const existingJump = pagination.querySelector('.pagination-jump');
        if (existingJump) existingJump.remove();

        if (totalPages > 7) {
            const jumpWrap = document.createElement("div");
            jumpWrap.className = "pagination-jump";
            jumpWrap.style.cssText = "display: inline-flex; align-items: center; gap: 6px; margin-left: 12px;";
            jumpWrap.innerHTML = `
                <span style="font-size: 0.8125rem; color: #64748b; white-space: nowrap;">Go to:</span>
                <input type="number" min="1" max="${totalPages}" class="form-input page-jump-input" style="width: 52px; height: 32px; text-align: center; font-size: 0.8125rem; border-radius: 6px; padding: 2px 4px; border: 1px solid #cbd5e1;" placeholder="${currentPage}" />
                <button type="button" class="btn btn-outline btn-small page-jump-btn" style="height: 32px; padding: 0 10px; font-size: 0.8125rem;">Go</button>
            `;

            const jumpInput = jumpWrap.querySelector(".page-jump-input");
            const jumpBtn = jumpWrap.querySelector(".page-jump-btn");

            const doJump = () => {
                const val = parseInt(jumpInput.value, 10);
                if (!isNaN(val) && val >= 1 && val <= totalPages) {
                    currentPage = val;
                    updateBookingList();
                } else {
                    jumpInput.value = "";
                }
            };

            jumpBtn.addEventListener("click", doJump);
            jumpInput.addEventListener("keydown", (e) => {
                if (e.key === "Enter") {
                    e.preventDefault();
                    doJump();
                }
            });

            pagination.appendChild(jumpWrap);
        }

        if (previousButton) previousButton.disabled = currentPage === 1;
        if (nextButton) nextButton.disabled = currentPage === totalPages;
    }

    function updateBookingList() {
        const filteredBookings = getFilteredBookings();
        const totalPages = Math.ceil(filteredBookings.length / itemsPerPage);

        if (totalPages > 0 && currentPage > totalPages) {
            currentPage = totalPages;
        }

        if (totalPages === 0) {
            currentPage = 1;
        }

        const startIndex = (currentPage - 1) * itemsPerPage;
        const endIndex = startIndex + itemsPerPage;

        bookingCards.forEach(function (card) {
            card.hidden = true;
        });

        filteredBookings.slice(startIndex, endIndex).forEach(function (card) {
            card.hidden = false;
        });

        if (emptyState) {
            emptyState.classList.toggle("is-visible", filteredBookings.length === 0);
        }

        renderPagination(totalPages);
    }

    filterButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            filterButtons.forEach(function (item) {
                item.classList.remove("is-active");
                item.setAttribute("aria-selected", "false");
            });

            button.classList.add("is-active");
            button.setAttribute("aria-selected", "true");
            currentFilter = button.dataset.filter || "all";
            currentPage = 1;
            updateBookingList();
        });
    });

    if (searchInput) {
        searchInput.addEventListener("input", function () {
            currentPage = 1;
            updateBookingList();
        });
    }

    if (previousButton) {
        previousButton.addEventListener("click", function () {
            if (currentPage > 1) {
                currentPage--;
                updateBookingList();
            }
        });
    }

    if (nextButton) {
        nextButton.addEventListener("click", function () {
            const filteredBookings = getFilteredBookings();
            const totalPages = Math.ceil(filteredBookings.length / itemsPerPage);

            if (currentPage < totalPages) {
                currentPage++;
                updateBookingList();
            }
        });
    }

    const bookingModal = document.getElementById("bookingDetailModal");
    const openDetailButtons = document.querySelectorAll(".js-open-booking-detail");
    const closeDetailButtons = document.querySelectorAll("[data-close-booking-detail]");
    const cancelBookingButton = document.getElementById("cancelBookingBtn");
    const bookServiceButton = document.getElementById("bookServiceBtn");
    const invoiceButtonText = document.getElementById("invoiceButtonText");

    const viewInvoiceBtn = document.getElementById("viewInvoiceBtn");
    const invoicePreviewModal = document.getElementById("invoicePreviewModal");
    const closeInvoiceButtons = document.querySelectorAll("[data-close-invoice-modal]");

    let activeBookingButton = null;

    function setElementText(elementId, value) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = value || "—";
        }
    }

    function formatBookingStatus(status) {
        const statusNames = {
            PENDING: "Pending",
            CONFIRMED: "Confirmed",
            CHECKED_IN: "Checked In",
            CHECKED_OUT: "Completed",
            COMPLETED: "Completed",
            CANCELLED: "Cancelled"
        };
        return statusNames[status] || status;
    }

    function updateStatusClass(statusElement, status) {
        if (!statusElement) return;

        statusElement.classList.remove(
            "booking-detail-status--pending",
            "booking-detail-status--confirmed",
            "booking-detail-status--current",
            "booking-detail-status--completed",
            "booking-detail-status--cancelled"
        );

        const statusClasses = {
            PENDING: "booking-detail-status--pending",
            CONFIRMED: "booking-detail-status--confirmed",
            CHECKED_IN: "booking-detail-status--current",
            CHECKED_OUT: "booking-detail-status--completed",
            COMPLETED: "booking-detail-status--completed",
            CANCELLED: "booking-detail-status--cancelled"
        };

        const statusClass = statusClasses[status];
        if (statusClass) {
            statusElement.classList.add(statusClass);
        }
    }

    function renderModalServiceItems(serviceItems) {
        const modalBookingDetailList = document.getElementById("modalBookingDetailList");
        const modalServiceEmpty = document.getElementById("modalServiceEmpty");

        if (!modalBookingDetailList) return;

        modalBookingDetailList.querySelectorAll(".js-dynamic-service-row").forEach(el => el.remove());

        if (!serviceItems || serviceItems.length === 0) {
            if (modalServiceEmpty) modalServiceEmpty.style.display = "flex";
            return;
        }

        if (modalServiceEmpty) modalServiceEmpty.style.display = "none";

        serviceItems.forEach(item => {
            const row = document.createElement("div");
            row.className = "booking-detail-items__row js-dynamic-service-row";
            row.innerHTML = `
                <div class="booking-detail-items__description">
                    <div class="booking-detail-items__item-icon">
                        <i class="fa-solid fa-bell-concierge"></i>
                    </div>
                    <div>
                        <strong>${escapeHtml(item.serviceName)}</strong>
                        <span style="font-size: 0.75rem; color: #888; display: block;">${escapeHtml(item.status)}</span>
                    </div>
                </div>
                <span>x${item.quantity}</span>
                <span>${escapeHtml(item.unitPriceVnd)}</span>
                <div style="display: flex; align-items: center; justify-content: flex-end; gap: 8px;">
                    <strong>${escapeHtml(item.totalAmountVnd)}</strong>
                    ${item.canCancel ? `
                        <button type="button" class="btn btn-error btn-xs js-cancel-service"
                                data-order-id="${item.orderId}"
                                data-service-name="${escapeHtml(item.serviceName)}"
                                style="padding: 4px 8px; font-size: 0.75rem; border-radius: 6px; cursor: pointer;">
                            <i class="fa-solid fa-trash-can"></i> Cancel
                        </button>
                    ` : ''}
                </div>
            `;
            modalBookingDetailList.appendChild(row);
        });

        modalBookingDetailList.querySelectorAll(".js-cancel-service").forEach(btn => {
            btn.addEventListener("click", async function (e) {
                e.stopPropagation();
                const orderId = btn.dataset.orderId;
                const serviceName = btn.dataset.serviceName;

                if (!confirm(`Are you sure you want to cancel the service "${serviceName}"?`)) {
                    return;
                }

                try {
                    const res = await fetch(getApiUrl(`/api/booking/service-order/${orderId}/cancel`), {
                        method: "POST"
                    });
                    const data = await res.json();
                    if (data.success) {
                        notifyToast("success", "Success", `Service "${serviceName}" cancelled successfully.`);
                        setTimeout(() => window.location.reload(), 1200);
                    } else {
                        notifyToast("error", "Cancellation Error", data.message || "Unable to cancel service.");
                    }
                } catch (err) {
                    notifyToast("error", "Connection Error", "A network connection error occurred.");
                }
            });
        });
    }

    function openBookingModal(button) {
        if (!bookingModal) return;

        activeBookingButton = button;

        const bookingCode = button.dataset.bookingCode || "";
        const bookingStatus = button.dataset.bookingStatus || "";

        setElementText("modalBookingCode", bookingCode);
        setElementText("modalBookingStatus", formatBookingStatus(bookingStatus));
        setElementText("modalRoomName", button.dataset.roomName);
        setElementText("modalBookingDetailRoom", button.dataset.roomName);
        setElementText("modalRoomType", button.dataset.roomType);
        setElementText("modalRoomNumber", button.dataset.roomNumber);
        setElementText("modalCheckIn", button.dataset.checkIn);
        setElementText("modalCheckOut", button.dataset.checkOut);
        setElementText("modalGuests", button.dataset.guests || "2 Guests");
        setElementText("modalNights", button.dataset.nights);
        setElementText("modalRoomQuantity", button.dataset.nights);
        setElementText("modalBookedOn", button.dataset.bookedOn);
        const roomChargeRaw = (button.dataset.roomCharge || "0").replace(/[^0-9]/g, '');
        const roomChargeNum = roomChargeRaw ? parseInt(roomChargeRaw, 10) : 0;
        const serviceChargeRaw = (button.dataset.serviceCharge || "0").replace(/[^0-9]/g, '');
        const serviceChargeNum = serviceChargeRaw ? parseInt(serviceChargeRaw, 10) : 0;
        const discountRaw = (button.dataset.discount || "0").replace(/[^0-9]/g, '');
        const discountNum = discountRaw ? parseInt(discountRaw, 10) : 0;

        const subtotal = roomChargeNum + serviceChargeNum - discountNum;
        const vatNum = Math.round(subtotal * 0.08);
        const totalNum = subtotal + vatNum;
        const depositVal = Math.round(totalNum * 0.3);
        const remainingVal = totalNum - depositVal;

        setElementText("modalPaymentStatus", button.dataset.paymentStatus);
        setElementText("modalSummaryTotal", totalNum.toLocaleString('vi-VN') + " VND");
        setElementText("modalTotal", totalNum.toLocaleString('vi-VN') + " VND");

        setElementText("modalRoomUnitPrice", button.dataset.unitPrice || "—");
        setElementText("modalRoomAmount", button.dataset.roomAmount || "—");
        setElementText("modalSpecialRequest", button.dataset.specialRequest || "No special requests were submitted for this booking.");
        setElementText("modalRoomCharge", button.dataset.roomCharge || "—");
        setElementText("modalServiceCharge", button.dataset.serviceCharge || "0 VND");
        setElementText("modalTax", vatNum.toLocaleString('vi-VN') + " VND");
        setElementText("modalDiscount", button.dataset.discount || "0 VND");

        const depositLabelEl = document.getElementById("modalDepositLabel");
        const depositAmtEl = document.getElementById("modalDepositAmount");
        const remainingLabelEl = document.getElementById("modalRemainingLabel");
        const remainingAmtEl = document.getElementById("modalRemainingAmount");

        if (bookingStatus === "PENDING") {
            if (depositLabelEl) depositLabelEl.textContent = "Deposit Required (30%):";
            if (depositAmtEl) depositAmtEl.innerHTML = `${depositVal.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--pending">Unpaid</small>`;
            if (remainingLabelEl) remainingLabelEl.textContent = "Remaining Balance at Check-in (70%):";
            if (remainingAmtEl) remainingAmtEl.textContent = remainingVal.toLocaleString('vi-VN') + " VND";
        } else if (bookingStatus === "CONFIRMED" || bookingStatus === "CHECKED_IN") {
            if (depositLabelEl) depositLabelEl.textContent = "Deposit (30% - VNPay):";
            if (depositAmtEl) depositAmtEl.innerHTML = `${depositVal.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--confirmed"><i class="fa-solid fa-check"></i> Paid</small>`;
            if (remainingLabelEl) remainingLabelEl.textContent = "Remaining Balance at Check-out (70%):";
            if (remainingAmtEl) remainingAmtEl.innerHTML = `${remainingVal.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--due">Due at Check-out</small>`;
        } else {
            if (depositLabelEl) depositLabelEl.textContent = "Deposit (30%):";
            if (depositAmtEl) depositAmtEl.innerHTML = `${depositVal.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--confirmed"><i class="fa-solid fa-check"></i> Paid</small>`;
            if (remainingLabelEl) remainingLabelEl.textContent = "Remaining Balance (70%):";
            if (remainingAmtEl) remainingAmtEl.innerHTML = `${remainingVal.toLocaleString('vi-VN')} VND <small class="booking-status-tag booking-status-tag--confirmed"><i class="fa-solid fa-check"></i> Settled</small>`;
        }

        setElementText("modalGuestName", button.dataset.guestName || "Guest");
        setElementText("modalGuestEmail", button.dataset.guestEmail || "guest@example.com");
        setElementText("modalGuestPhone", button.dataset.guestPhone || "—");

        const statusElement = document.getElementById("modalBookingStatus");
        updateStatusClass(statusElement, bookingStatus);

        let serviceItems = [];
        try {
            serviceItems = JSON.parse(button.dataset.serviceItems || "[]");
        } catch (e) {
            serviceItems = [];
        }
        renderModalServiceItems(serviceItems);

        const modalPayDepositBtn = document.getElementById("modalPayDepositBtn");
        if (modalPayDepositBtn) {
            const isPending = (bookingStatus === "PENDING");
            modalPayDepositBtn.hidden = !isPending;
            if (isPending) {
                modalPayDepositBtn.dataset.bookingId = button.dataset.bookingId;
                modalPayDepositBtn.dataset.bookingCode = bookingCode;
            }
        }

        if (cancelBookingButton) {
            cancelBookingButton.hidden = (bookingStatus !== "PENDING" && bookingStatus !== "CONFIRMED");
            cancelBookingButton.dataset.bookingCode = bookingCode;
        }

        const canBookService = ["PENDING", "CONFIRMED", "CHECKED_IN"].includes(bookingStatus);

        if (bookServiceButton) {
            bookServiceButton.hidden = !canBookService;
            if (canBookService) {
                bookServiceButton.href = getApiUrl(`/services?bookingCode=${encodeURIComponent(bookingCode)}`);
            }
        }

        if (invoiceButtonText) {
            const isPastBooking = ["COMPLETED", "CHECKED_OUT", "CANCELLED"].includes(bookingStatus);
            invoiceButtonText.textContent = isPastBooking ? "View Invoice" : "Preview Invoice";
        }

        bookingModal.classList.add("is-open");
        bookingModal.setAttribute("aria-hidden", "false");
        document.body.classList.add("booking-modal-open");

        const dialog = bookingModal.querySelector(".booking-detail-modal__dialog");
        if (dialog) dialog.focus();
    }

    async function initiateDepositPayment(bookingId, bookingCode, buttonEl) {
        if (!bookingId) {
            notifyToast("error", "Payment Error", "Booking ID not found.");
            return;
        }
        const originalHtml = buttonEl ? buttonEl.innerHTML : "";
        if (buttonEl) {
            buttonEl.disabled = true;
            buttonEl.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Connecting to VNPay...';
        }

        try {
            const res = await fetch(getApiUrl(`/api/vnpay/create-deposit?bookingId=${encodeURIComponent(bookingId)}`));
            const resData = await res.json();
            if (resData.success && resData.data) {
                notifyToast("success", "VNPay Gateway", "Redirecting to VNPay Sandbox payment gateway...");
                window.location.href = resData.data;
            } else {
                notifyToast("error", "Payment Failed", resData.message || "Could not generate deposit payment link.");
                if (buttonEl) {
                    buttonEl.disabled = false;
                    buttonEl.innerHTML = originalHtml;
                }
            }
        } catch (err) {
            notifyToast("error", "Network Error", "Failed to connect to payment server: " + err);
            if (buttonEl) {
                buttonEl.disabled = false;
                buttonEl.innerHTML = originalHtml;
            }
        }
    }

    document.addEventListener("click", function (e) {
        const depositBtn = e.target.closest(".js-pay-deposit, .js-modal-pay-deposit");
        if (depositBtn) {
            e.preventDefault();
            e.stopPropagation();
            const bookingId = depositBtn.dataset.bookingId;
            const bookingCode = depositBtn.dataset.bookingCode;
            initiateDepositPayment(bookingId, bookingCode, depositBtn);
        }
    });

    function closeBookingModal() {
        if (!bookingModal) return;

        bookingModal.classList.remove("is-open");
        bookingModal.setAttribute("aria-hidden", "true");
        document.body.classList.remove("booking-modal-open");

        if (activeBookingButton) {
            activeBookingButton.focus();
        }

        activeBookingButton = null;
    }

    openDetailButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            openBookingModal(button);
        });
    });

    closeDetailButtons.forEach(function (button) {
        button.addEventListener("click", closeBookingModal);
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && bookingModal && bookingModal.classList.contains("is-open")) {
            closeBookingModal();
        }
    });

    if (cancelBookingButton) {
        cancelBookingButton.addEventListener("click", async function () {
            const bookingCode = cancelBookingButton.dataset.bookingCode;
            const confirmed = window.confirm(`Bạn có chắc chắn muốn hủy đơn đặt phòng ${bookingCode}?`);

            if (!confirmed) return;

            try {
                const res = await fetch(getApiUrl(`/api/booking/${encodeURIComponent(bookingCode)}/cancel`), {
                    method: "POST"
                });
                const data = await res.json();
                if (data.success) {
                    notifyToast("success", "Thành công", `Đơn đặt phòng ${bookingCode} đã được hủy thành công.`);
                    setTimeout(() => window.location.reload(), 1200);
                } else {
                    notifyToast("error", "Lỗi hủy đơn", data.message || "Không thể hủy đơn đặt phòng.");
                }
            } catch (e) {
                notifyToast("error", "Lỗi kết nối", "Đã xảy ra lỗi khi kết nối máy chủ.");
            }
        });
    }

    if (viewInvoiceBtn && invoicePreviewModal) {
        viewInvoiceBtn.addEventListener("click", function (e) {
            e.preventDefault();
            e.stopPropagation();

            const bookingCode = document.getElementById("modalBookingCode")?.textContent?.trim() || activeBookingButton?.dataset?.bookingCode || "DBH-2026-001";
            const roomName = document.getElementById("modalRoomName")?.textContent?.trim() || activeBookingButton?.dataset?.roomName || "Room";
            const checkIn = document.getElementById("modalCheckIn")?.textContent?.trim() || activeBookingButton?.dataset?.checkIn || "";
            const checkOut = document.getElementById("modalCheckOut")?.textContent?.trim() || activeBookingButton?.dataset?.checkOut || "";
            const nights = document.getElementById("modalNights")?.textContent?.trim() || activeBookingButton?.dataset?.nights || "1 Night";
            const roomCharge = document.getElementById("modalRoomCharge")?.textContent?.trim() || "0 VND";
            const serviceCharge = document.getElementById("modalServiceCharge")?.textContent?.trim() || "0 VND";
            const guestName = document.getElementById("modalGuestName")?.textContent?.trim() || "Guest";

            const roomChargeNum = roomCharge ? parseInt(roomCharge.replace(/[^0-9]/g, '') || "0", 10) : 0;
            const serviceChargeNum = serviceCharge ? parseInt(serviceCharge.replace(/[^0-9]/g, '') || "0", 10) : 0;
            const subtotal = roomChargeNum + serviceChargeNum;
            const vatNum = Math.round(subtotal * 0.08);
            const totalNum = subtotal + vatNum;

            const invNumber = document.getElementById("invNumber");
            if (invNumber) invNumber.textContent = "INV-" + bookingCode;

            const invBookingCode = document.getElementById("invBookingCode");
            if (invBookingCode) invBookingCode.textContent = bookingCode;

            const invDate = document.getElementById("invDate");
            if (invDate) invDate.textContent = new Date().toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric' });

            const formatVndText = (num) => Number(num || 0).toLocaleString('en-US') + " VND";

            const invSubtotal = document.getElementById("invSubtotal");
            if (invSubtotal) invSubtotal.textContent = formatVndText(subtotal);

            const invTax = document.getElementById("invTax");
            if (invTax) invTax.textContent = formatVndText(vatNum);

            const invGrandTotal = document.getElementById("invGrandTotal");
            if (invGrandTotal) invGrandTotal.textContent = formatVndText(totalNum);

            const invCustomerName = document.getElementById("invCustomerName");
            if (invCustomerName) invCustomerName.textContent = guestName;

            const invTableBody = document.getElementById("invTableBody");
            if (invTableBody) {
                let serviceItems = [];
                if (activeBookingButton && activeBookingButton.dataset.serviceItems) {
                    try {
                        serviceItems = JSON.parse(activeBookingButton.dataset.serviceItems || "[]");
                    } catch (err) {
                        serviceItems = [];
                    }
                }

                let html = `
                    <tr>
                        <td>
                            <strong>${escapeHtml(roomName)}</strong><br>
                            <small>${escapeHtml(checkIn)} — ${escapeHtml(checkOut)}</small>
                        </td>
                        <td class="text-center">${escapeHtml(nights)}</td>
                        <td class="text-right"><strong>${escapeHtml(roomCharge)}</strong></td>
                    </tr>
                `;

                serviceItems.forEach(item => {
                    html += `
                        <tr>
                            <td>
                                <strong>${escapeHtml(item.serviceName)}</strong><br>
                                <small>Add-on Service</small>
                            </td>
                            <td class="text-center">x${item.quantity}</td>
                            <td class="text-right"><strong>${escapeHtml(item.totalAmountVnd)}</strong></td>
                        </tr>
                    `;
                });

                invTableBody.innerHTML = html;
            }

            invoicePreviewModal.classList.add("is-open");
            invoicePreviewModal.setAttribute("aria-hidden", "false");
        });
    }

    closeInvoiceButtons.forEach(function (btn) {
        btn.addEventListener("click", function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (invoicePreviewModal) {
                invoicePreviewModal.classList.remove("is-open");
                invoicePreviewModal.setAttribute("aria-hidden", "true");
            }
        });
    });

    const printInvoiceBtn = document.getElementById("printInvoiceBtn") || document.querySelector("[data-print-invoice]");
    if (printInvoiceBtn) {
        printInvoiceBtn.addEventListener("click", function () {
            const bookingCode = document.getElementById("invBookingCode")?.textContent?.trim() ||
                                document.getElementById("modalBookingCode")?.textContent?.trim() ||
                                activeBookingButton?.dataset?.bookingCode || "";
            if (bookingCode) {
                window.open(getApiUrl("/customer/booking/" + encodeURIComponent(bookingCode) + "/invoice"), "_blank");
            } else {
                window.print();
            }
        });
    }

    updateFilterBadges();
    updateBookingList();
});