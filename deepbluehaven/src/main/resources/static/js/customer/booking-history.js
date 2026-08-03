document.addEventListener("DOMContentLoaded", function () {

    const filterButtons = document.querySelectorAll(
        ".booking-filter-tab"
    );

    const bookingCards = Array.from(
        document.querySelectorAll(".booking-history-card")
    );

    const searchInput = document.getElementById("bookingSearch");
    const emptyState = document.getElementById("bookingHistoryEmpty");

    const pagination = document.getElementById("bookingPagination");

    const paginationNumbers = document.getElementById(
        "bookingPaginationNumbers"
    );

    const previousButton = document.getElementById(
        "bookingPrevPage"
    );

    const nextButton = document.getElementById(
        "bookingNextPage"
    );

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
        const keyword = searchInput
            ? searchInput.value.trim().toLowerCase()
            : "";

        return bookingCards.filter(function (card) {
            const status = card.dataset.status || "";

            const searchContent = (
                card.dataset.search || ""
            ).toLowerCase();

            const matchesFilter =
                currentFilter === "all" ||
                status === currentFilter;

            const matchesSearch =
                searchContent.includes(keyword);

            return matchesFilter && matchesSearch;
        });
    }

    function renderPagination(totalPages) {
        if (!pagination || !paginationNumbers) {
            return;
        }

        paginationNumbers.innerHTML = "";

        pagination.hidden = totalPages <= 1;

        if (totalPages <= 1) {
            return;
        }

        for (let page = 1; page <= totalPages; page++) {
            const pageButton = document.createElement("button");

            pageButton.type = "button";
            pageButton.className =
                "booking-pagination__number";

            pageButton.textContent = page;

            pageButton.setAttribute(
                "aria-label",
                `Go to page ${page}`
            );

            if (page === currentPage) {
                pageButton.classList.add("is-active");

                pageButton.setAttribute(
                    "aria-current",
                    "page"
                );
            }

            pageButton.addEventListener("click", function () {
                currentPage = page;

                updateBookingList();
            });

            paginationNumbers.appendChild(pageButton);
        }

        if (previousButton) {
            previousButton.disabled = currentPage === 1;
        }

        if (nextButton) {
            nextButton.disabled =
                currentPage === totalPages;
        }
    }

    function updateBookingList() {
        const filteredBookings = getFilteredBookings();

        const totalPages = Math.ceil(
            filteredBookings.length / itemsPerPage
        );

        if (totalPages > 0 && currentPage > totalPages) {
            currentPage = totalPages;
        }

        if (totalPages === 0) {
            currentPage = 1;
        }

        const startIndex =
            (currentPage - 1) * itemsPerPage;

        const endIndex =
            startIndex + itemsPerPage;

        bookingCards.forEach(function (card) {
            card.hidden = true;
        });

        filteredBookings
            .slice(startIndex, endIndex)
            .forEach(function (card) {
                card.hidden = false;
            });

        if (emptyState) {
            emptyState.classList.toggle(
                "is-visible",
                filteredBookings.length === 0
            );
        }

        renderPagination(totalPages);
    }

    filterButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            filterButtons.forEach(function (item) {
                item.classList.remove("is-active");

                item.setAttribute(
                    "aria-selected",
                    "false"
                );
            });

            button.classList.add("is-active");

            button.setAttribute(
                "aria-selected",
                "true"
            );

            currentFilter =
                button.dataset.filter || "all";

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
            const filteredBookings =
                getFilteredBookings();

            const totalPages = Math.ceil(
                filteredBookings.length / itemsPerPage
            );

            if (currentPage < totalPages) {
                currentPage++;

                updateBookingList();
            }
        });
    }

    const bookingModal = document.getElementById(
        "bookingDetailModal"
    );

    const openDetailButtons = document.querySelectorAll(
        ".js-open-booking-detail"
    );

    const closeDetailButtons = document.querySelectorAll(
        "[data-close-booking-detail]"
    );

    const cancelBookingButton = document.getElementById(
        "cancelBookingBtn"
    );

    const bookServiceButton = document.getElementById(
        "bookServiceBtn"
    );

    const invoiceButtonText = document.getElementById(
        "invoiceButtonText"
    );

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
        if (!statusElement) {
            return;
        }

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

    function openBookingModal(button) {
        if (!bookingModal) {
            return;
        }

        activeBookingButton = button;

        const bookingCode =
            button.dataset.bookingCode || "";

        const bookingStatus =
            button.dataset.bookingStatus || "";

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

        const statusElement = document.getElementById("modalBookingStatus");
        updateStatusClass(statusElement, bookingStatus);

        if (cancelBookingButton) {
            cancelBookingButton.hidden = bookingStatus !== "PENDING";
            cancelBookingButton.dataset.bookingCode = bookingCode;
        }

        const canBookService = [
            "PENDING",
            "CONFIRMED",
            "CHECKED_IN"
        ].includes(bookingStatus);

        if (bookServiceButton) {
            bookServiceButton.hidden = !canBookService;

            if (canBookService) {
                const serviceUrl = new URL(
                    bookServiceButton.href,
                    window.location.origin
                );

                serviceUrl.searchParams.set(
                    "bookingCode",
                    bookingCode
                );

                bookServiceButton.href = serviceUrl.toString();
            }
        }

        if (invoiceButtonText) {
            const isPastBooking = [
                "COMPLETED",
                "CHECKED_OUT",
                "CANCELLED"
            ].includes(bookingStatus);

            invoiceButtonText.textContent = isPastBooking
                ? "View Invoice"
                : "Preview Invoice";
        }

        bookingModal.classList.add("is-open");
        bookingModal.setAttribute("aria-hidden", "false");
        document.body.classList.add("booking-modal-open");

        const dialog = bookingModal.querySelector(".booking-detail-modal__dialog");
        if (dialog) {
            dialog.focus();
        }
    }

    function closeBookingModal() {
        if (!bookingModal) {
            return;
        }

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
        if (
            event.key === "Escape" &&
            bookingModal &&
            bookingModal.classList.contains("is-open")
        ) {
            closeBookingModal();
        }
    });

    if (cancelBookingButton) {
        cancelBookingButton.addEventListener("click", function () {
            const bookingCode = cancelBookingButton.dataset.bookingCode;
            const confirmed = window.confirm(
                `Are you sure you want to cancel booking ${bookingCode}?`
            );

            if (!confirmed) {
                return;
            }

            window.alert(`Booking ${bookingCode} has been cancelled.`);
            closeBookingModal();
        });
    }

    updateFilterBadges(); 
    updateBookingList();  
});