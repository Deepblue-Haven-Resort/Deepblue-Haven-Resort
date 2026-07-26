document.addEventListener("DOMContentLoaded", function () {
    const filterButtons =
        document.querySelectorAll(".offers-filter-tab");

    const offerCards = Array.from(
        document.querySelectorAll(".offer-card")
    );

    const searchInput =
        document.getElementById("offersSearch");

    const emptyState =
        document.getElementById("offersEmpty");

    const resultCount =
        document.getElementById("offersResultCount");

    const favoriteButtons =
        document.querySelectorAll(".offer-card__favorite");

    const accordionItems =
        document.querySelectorAll(".offer-accordion__item");

    let currentFilter = "all";

    /**
     * Lọc offer theo category và từ khóa.
     */
    function updateOffers() {
        const keyword = searchInput
            ? searchInput.value.trim().toLowerCase()
            : "";

        let visibleCount = 0;

        offerCards.forEach(function (card) {
            const category =
                card.dataset.category || "";

            const searchContent =
                (card.dataset.search || "").toLowerCase();

            const matchesCategory =
                currentFilter === "all" ||
                category === currentFilter;

            const matchesSearch =
                searchContent.includes(keyword);

            const shouldShow =
                matchesCategory && matchesSearch;

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
        searchInput.addEventListener(
            "input",
            updateOffers
        );
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
                icon.classList.toggle(
                    "fa-regular",
                    !isFavorite
                );

                icon.classList.toggle(
                    "fa-solid",
                    isFavorite
                );
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
        const button = item.querySelector(
            ".offer-accordion__button"
        );

        if (!button) {
            return;
        }

        button.addEventListener("click", function () {
            const isOpen =
                item.classList.contains("is-open");

            accordionItems.forEach(function (otherItem) {
                otherItem.classList.remove("is-open");

                const otherButton =
                    otherItem.querySelector(
                        ".offer-accordion__button"
                    );

                if (otherButton) {
                    otherButton.setAttribute(
                        "aria-expanded",
                        "false"
                    );
                }
            });

            if (!isOpen) {
                item.classList.add("is-open");

                button.setAttribute(
                    "aria-expanded",
                    "true"
                );
            }
        });
    });

    updateOffers();
});