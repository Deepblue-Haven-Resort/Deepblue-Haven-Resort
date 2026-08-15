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
                            <i class="fa-solid fa-trash-can"></i> Hủy
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

                if (!confirm(`Bạn có chắc chắn muốn hủy dịch vụ "${serviceName}"?`)) {
                    return;
                }

                try {
                    const res = await fetch(getApiUrl(`/api/booking/service-order/${orderId}/cancel`), {
                        method: "POST"
                    });
                    const data = await res.json();
                    if (data.success) {
                        showToast("success", "Thành công", `Đã hủy dịch vụ "${serviceName}" thành công.`);
                        setTimeout(() => window.location.reload(), 1200);
                    } else {
                        showToast("error", "Lỗi hủy dịch vụ", data.message || "Không thể hủy dịch vụ.");
                    }
                } catch (err) {
                    showToast("error", "Lỗi kết nối", "Đã xảy ra lỗi kết nối.");
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
        setElementText("modalPaymentStatus", button.dataset.paymentStatus);
        setElementText("modalSummaryTotal", button.dataset.total);
        setElementText("modalTotal", button.dataset.total);

        setElementText("modalRoomUnitPrice", button.dataset.unitPrice || "—");
        setElementText("modalRoomAmount", button.dataset.roomAmount || "—");
        setElementText("modalSpecialRequest", button.dataset.specialRequest || "No special requests were submitted for this booking.");
        setElementText("modalRoomCharge", button.dataset.roomCharge || "—");
        setElementText("modalServiceCharge", button.dataset.serviceCharge || "0 VND");
        setElementText("modalTax", button.dataset.tax || "0 VND");
        setElementText("modalDiscount", button.dataset.discount || "0 VND");

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
                    showToast("success", "Thành công", `Đơn đặt phòng ${bookingCode} đã được hủy thành công.`);
                    setTimeout(() => window.location.reload(), 1200);
                } else {
                    showToast("error", "Lỗi hủy đơn", data.message || "Không thể hủy đơn đặt phòng.");
                }
            } catch (e) {
                showToast("error", "Lỗi kết nối", "Đã xảy ra lỗi khi kết nối máy chủ.");
            }
        });
    }

    if (viewInvoiceBtn && invoicePreviewModal) {
        viewInvoiceBtn.addEventListener("click", function () {
            if (!activeBookingButton) return;

            const bookingCode = activeBookingButton.dataset.bookingCode || "DBH-2026-001";
            const roomName = activeBookingButton.dataset.roomName || "Room";
            const checkIn = activeBookingButton.dataset.checkIn || "";
            const checkOut = activeBookingButton.dataset.checkOut || "";
            const nights = activeBookingButton.dataset.nights || "1 Night";
            const total = activeBookingButton.dataset.total || "0 VND";
            const roomCharge = activeBookingButton.dataset.roomCharge || total;
            const tax = activeBookingButton.dataset.tax || "0 VND";

            document.getElementById("invNumber").textContent = "INV-" + bookingCode;
            document.getElementById("invBookingCode").textContent = bookingCode;
            document.getElementById("invDate").textContent = new Date().toLocaleDateString('en-GB');
            document.getElementById("invRoomName").textContent = roomName;
            document.getElementById("invStayDates").textContent = checkIn + " — " + checkOut;
            document.getElementById("invNights").textContent = nights;
            document.getElementById("invRoomCharge").textContent = roomCharge;
            document.getElementById("invSubtotal").textContent = roomCharge;
            document.getElementById("invTax").textContent = tax;
            document.getElementById("invGrandTotal").textContent = total;

            const guestName = document.getElementById("modalGuestName")?.textContent || "Guest";
            document.getElementById("invCustomerName").textContent = guestName;

            const invTableBody = document.getElementById("invTableBody");
            if (invTableBody) {
                let serviceItems = [];
                try {
                    serviceItems = JSON.parse(activeBookingButton.dataset.serviceItems || "[]");
                } catch (e) {
                    serviceItems = [];
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
        btn.addEventListener("click", function () {
            if (invoicePreviewModal) {
                invoicePreviewModal.classList.remove("is-open");
                invoicePreviewModal.setAttribute("aria-hidden", "true");
            }
        });
    });

    const printInvoiceBtn = document.getElementById("printInvoiceBtn") || document.querySelector("[data-print-invoice]");
    if (printInvoiceBtn) {
        printInvoiceBtn.addEventListener("click", function () {
            window.print();
        });
    }

    updateFilterBadges();
    updateBookingList();
});