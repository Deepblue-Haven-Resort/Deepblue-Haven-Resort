document.addEventListener("DOMContentLoaded", function () {
    const filterButtons = document.querySelectorAll(".offers-filter-tab");

    const offerCards = Array.from(document.querySelectorAll(".offer-card"));

    const searchInput = document.getElementById("offersSearch");
    const emptyState = document.getElementById("offersEmpty");
    const resultCount = document.getElementById("offersResultCount");
    const favoriteButtons = document.querySelectorAll(".offer-card__favorite");
    const accordionItems = document.querySelectorAll(".offer-accordion__item");

    let currentFilter = "all";

    function updateCategoryCounts() {
        filterButtons.forEach(function (button) {
            const filter = button.dataset.filter || "all";
            const span = button.querySelector("span");
            if (span) {
                if (filter === "all") {
                    span.textContent = offerCards.length;
                } else {
                    const count = offerCards.filter(c => (c.dataset.category || "") === filter).length;
                    span.textContent = count;
                }
            }
        });
    }

    updateCategoryCounts();


    function updateOffers() {
        const keyword = searchInput
            ? searchInput.value.trim().toLowerCase()
            : "";

        let visibleCount = 0;

        offerCards.forEach(function (card) {
            const category = card.dataset.category || "";

            const searchContent = (card.dataset.search || "").toLowerCase();

            const matchesCategory = currentFilter === "all" || category === currentFilter;

            const matchesSearch = searchContent.includes(keyword);

            const shouldShow = matchesCategory && matchesSearch;

            card.hidden = !shouldShow;

            if (shouldShow) {
                visibleCount++;
            }
        });

        if (emptyState) {
            emptyState.classList.toggle(
                "is-visible",
                visibleCount === 0
            );
        }

        if (resultCount) {
            resultCount.textContent =
                visibleCount +
                (visibleCount === 1
                    ? " offer available"
                    : " offers available");
        }
    }

    /**
     * Filter button.
     */
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

            updateOffers();
        });
    });

    /**
     * Search.
     */
    if (searchInput) {
        searchInput.addEventListener("input", updateOffers);
    }

    /**
     * Favorite icon frontend tĩnh.
     */
    favoriteButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            const icon = button.querySelector("i");
            button.classList.toggle("is-favorite");
            const isFavorite =
                button.classList.contains("is-favorite");

            if (icon) {
                icon.classList.toggle("fa-regular", !isFavorite);

                icon.classList.toggle("fa-solid", isFavorite);
            }

            button.setAttribute(
                "aria-pressed",
                String(isFavorite)
            );
        });
    });

    /**
     * Accordion terms.
     */
    accordionItems.forEach(function (item) {
        const button = item.querySelector(".offer-accordion__button");

        if (!button) {
            return;
        }

        button.addEventListener("click", function () {
            const isOpen = item.classList.contains("is-open");

            accordionItems.forEach(function (otherItem) {
                otherItem.classList.remove("is-open");
                const otherButton = otherItem.querySelector(".offer-accordion__button");
                if (otherButton) {
                    otherButton.setAttribute(
                        "aria-expanded",
                        "false"
                    );
                }
            });

            if (!isOpen) {
                item.classList.add("is-open");

                button.setAttribute("aria-expanded", "true");
            }
        });
    });

    const offerModal = document.getElementById("offerDetailModal");
    const viewDetailButtons = document.querySelectorAll(".btn-view-offer-detail");
    const closeModalButtons = document.querySelectorAll("[data-close-offer-detail]");

    function openOfferModal(btn) {
        if (!offerModal) return;

        const code = btn.dataset.code || "";
        const description = btn.dataset.description || "";
        const badge = btn.dataset.badge || "";
        const category = btn.dataset.category || "Special Offer";
        const validity = btn.dataset.validity || "";
        const price = btn.dataset.price || "";
        const roomType = btn.dataset.roomtype || "All Rooms";

        const titleEl = document.getElementById("modalOfferTitle");
        const categoryEl = document.getElementById("modalOfferCategory");
        const badgeEl = document.getElementById("modalOfferBadge");
        const descEl = document.getElementById("modalOfferDescription");
        const validityEl = document.getElementById("modalOfferValidity");
        const priceEl = document.getElementById("modalOfferPrice");
        const roomTypeEl = document.getElementById("modalOfferRoomType");

        if (titleEl) titleEl.textContent = code;
        if (categoryEl) categoryEl.textContent = category;
        if (badgeEl) badgeEl.textContent = badge;
        if (descEl) descEl.textContent = description;
        if (validityEl) validityEl.textContent = validity;
        if (priceEl) priceEl.textContent = price;
        if (roomTypeEl) roomTypeEl.textContent = roomType && roomType !== "null" ? roomType : "All Rooms";

        offerModal.setAttribute("aria-hidden", "false");
        offerModal.classList.add("is-visible");
        document.body.style.overflow = "hidden";
    }

    function closeOfferModal() {
        if (!offerModal) return;
        offerModal.setAttribute("aria-hidden", "true");
        offerModal.classList.remove("is-visible");
        document.body.style.overflow = "";
    }

    viewDetailButtons.forEach(function (btn) {
        btn.addEventListener("click", function () {
            openOfferModal(btn);
        });
    });

    closeModalButtons.forEach(function (btn) {
        btn.addEventListener("click", closeOfferModal);
    });

    updateOffers();
});
