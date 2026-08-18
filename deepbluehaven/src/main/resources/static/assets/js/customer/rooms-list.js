/**
 * Customer Rooms List - Interactive Filter, Sort & Pagination Controller
 */
const ROOMS_PER_PAGE = 8;
let currentRoomsPage = 1;
let matchingCardsList = [];

const roomsFilterState = {
    resort: "all",
    type: "all",
    guests: "any",
    view: "any",
    sort: "recommended"
};

function getRoomCards() {
    return Array.from(document.querySelectorAll(".room-card"));
}

function toVnd(millionValue) {
    return Math.round(Number(millionValue || 0) * 1000000);
}

function formatMillion(millionValue) {
    const num = Number(millionValue || 0);
    return Number.isInteger(num) ? `${num} million` : `${parseFloat(num.toFixed(2))} million`;
}

function initRoomsDropdowns() {
    const dropdowns = document.querySelectorAll(".rooms-dropdown");
    if (!dropdowns.length) return;

    dropdowns.forEach((dropdown) => {
        dropdown.addEventListener("toggle", () => {
            if (!dropdown.open) return;
            dropdowns.forEach((item) => {
                if (item !== dropdown) {
                    item.open = false;
                    item.removeAttribute("open");
                }
            });
        });

        const options = dropdown.querySelectorAll(".rooms-dropdown__option");
        options.forEach((option) => {
            option.addEventListener("click", (event) => {
                event.preventDefault();
                event.stopPropagation();

                const filterName = dropdown.dataset.filter;
                const valueText = dropdown.querySelector(".rooms-dropdown__value");

                if (filterName) {
                    roomsFilterState[filterName] = (option.dataset.value || "all").toLowerCase().trim();
                }

                if (valueText) {
                    valueText.textContent = option.textContent.trim();
                }

                options.forEach((item) => item.classList.remove("is-active"));
                option.classList.add("is-active");

                dropdown.open = false;
                dropdown.removeAttribute("open");

                applyRoomsFilters(true);
            });
        });
    });

    document.addEventListener("click", (event) => {
        if (event.target.closest(".rooms-dropdown")) return;
        dropdowns.forEach((dropdown) => {
            dropdown.open = false;
            dropdown.removeAttribute("open");
        });
    });
}

function initRoomsPriceFilter() {
    const priceMin = document.getElementById("priceMin");
    const priceMax = document.getElementById("priceMax");
    const priceSlider = document.querySelector(".rooms-price-slider");
    const priceFill = document.getElementById("sliderFill");
    const priceMinLabel = document.getElementById("priceMinLabel");
    const priceMaxLabel = document.getElementById("priceMaxLabel");
    const priceValue = document.querySelector(".rooms-price-filter__value");

    if (!priceMin || !priceMax) return;

    const cards = getRoomCards();
    const highestCardPrice = cards.reduce((max, c) => Math.max(max, Number(c.dataset.price || 0)), 0);
    const maxMillion = Math.max(10, Math.ceil(highestCardPrice / 1000000));

    priceMin.max = String(maxMillion);
    priceMax.max = String(maxMillion);
    if (Number(priceMax.value) < maxMillion && Number(priceMax.value) === 10) {
        priceMax.value = String(maxMillion);
    }

    const getConfig = () => ({
        min: Number(priceMin.min || 0),
        max: Number(priceMax.max || maxMillion),
        step: Number(priceMin.step || 0.5)
    });

    const clamp = (value, min, max) => Math.min(Math.max(value, min), max);

    const updatePriceUI = () => {
        const config = getConfig();

        let minValue = Number(priceMin.value || config.min);
        let maxValue = Number(priceMax.value || config.max);

        minValue = clamp(minValue, config.min, config.max - config.step);
        maxValue = clamp(maxValue, config.min + config.step, config.max);

        if (minValue >= maxValue) {
            minValue = maxValue - config.step;
        }

        priceMin.value = String(minValue);
        priceMax.value = String(maxValue);

        const minPercent = ((minValue - config.min) / (config.max - config.min)) * 100;
        const maxPercent = ((maxValue - config.min) / (config.max - config.min)) * 100;

        if (priceFill) {
            priceFill.style.left = `${minPercent}%`;
            priceFill.style.width = `${maxPercent - minPercent}%`;
        }

        if (priceSlider) {
            priceSlider.style.setProperty("--price-min-percent", `${minPercent}%`);
            priceSlider.style.setProperty("--price-max-percent", `${maxPercent}%`);
        }

        if (priceMinLabel) {
            priceMinLabel.textContent = formatMillion(minValue);
        }

        if (priceMaxLabel) {
            priceMaxLabel.textContent = formatMillion(maxValue);
        }

        if (priceValue) {
            priceValue.textContent = `${formatMillion(minValue)} – ${formatMillion(maxValue)} / night`;
        }
    };

    priceMin.addEventListener("input", () => {
        const config = getConfig();
        const minValue = Number(priceMin.value);
        const maxValue = Number(priceMax.value);

        if (minValue >= maxValue) {
            priceMin.value = String(maxValue - config.step);
        }

        updatePriceUI();
        applyRoomsFilters(true);
    });

    priceMax.addEventListener("input", () => {
        const config = getConfig();
        const minValue = Number(priceMin.value);
        const maxValue = Number(priceMax.value);

        if (maxValue <= minValue) {
            priceMax.value = String(minValue + config.step);
        }

        updatePriceUI();
        applyRoomsFilters(true);
    });

    updatePriceUI();
}

function initRoomsFiltering() {
    const applyButton = document.querySelector(".rooms-filter-apply");

    getRoomCards().forEach((card, index) => {
        if (!card.dataset.order) {
            card.dataset.order = String(index + 1);
        }
    });

    if (applyButton) {
        applyButton.addEventListener("click", (event) => {
            event.preventDefault();
            applyRoomsFilters(true);
        });
    }

    initRoomsPagination();
    applyRoomsFilters(true);
}

function initRoomsPagination() {
    const prevBtn = document.getElementById("roomsPrevPage");
    const nextBtn = document.getElementById("roomsNextPage");

    if (prevBtn) {
        prevBtn.addEventListener("click", () => {
            if (currentRoomsPage > 1) {
                currentRoomsPage -= 1;
                renderRoomsPagination();
                scrollToResults();
            }
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", () => {
            const totalPages = Math.ceil(matchingCardsList.length / ROOMS_PER_PAGE);
            if (currentRoomsPage < totalPages) {
                currentRoomsPage += 1;
                renderRoomsPagination();
                scrollToResults();
            }
        });
    }
}

function scrollToResults() {
    const resultsSection = document.querySelector(".rooms-results");
    if (resultsSection) {
        resultsSection.scrollIntoView({ behavior: "smooth", block: "start" });
    }
}

function getSelectedPriceRange() {
    const priceMin = document.getElementById("priceMin");
    const priceMax = document.getElementById("priceMax");

    if (!priceMin || !priceMax) {
        return {
            minPrice: 0,
            maxPrice: Number.MAX_SAFE_INTEGER
        };
    }

    const minVal = Number(priceMin.value);
    const maxVal = Number(priceMax.value);
    const maxLimit = Number(priceMax.max || 10);

    return {
        minPrice: toVnd(minVal),
        maxPrice: maxVal >= maxLimit ? Number.MAX_SAFE_INTEGER : toVnd(maxVal)
    };
}

function roomMatchesGuests(cardGuests) {
    if (!roomsFilterState.guests || roomsFilterState.guests === "any") {
        return true;
    }

    const capacity = Number(cardGuests || 0);
    if (roomsFilterState.guests === "4+") {
        return capacity >= 4;
    }

    const requested = Number(roomsFilterState.guests);
    return capacity >= requested;
}

function roomMatchesView(cardViews) {
    if (!roomsFilterState.view || roomsFilterState.view === "any") {
        return true;
    }

    const targetView = roomsFilterState.view.toLowerCase().trim();
    const views = String(cardViews || "")
        .toLowerCase()
        .split(",")
        .map((v) => v.trim())
        .filter(Boolean);

    return views.some((v) => v === targetView || v.replace(/_/g, "-") === targetView.replace(/_/g, "-") || v.includes(targetView) || targetView.includes(v));
}

function sortRooms() {
    const grid = document.getElementById("roomsGrid") || document.querySelector(".rooms-grid");
    if (!grid) return;

    matchingCardsList.sort((a, b) => {
        const priceA = Number(a.dataset.price || 0);
        const priceB = Number(b.dataset.price || 0);
        const ratingA = Number(a.dataset.rating || 0);
        const ratingB = Number(b.dataset.rating || 0);
        const orderA = Number(a.dataset.order || 0);
        const orderB = Number(b.dataset.order || 0);

        if (roomsFilterState.sort === "lowest-price") return priceA - priceB;
        if (roomsFilterState.sort === "highest-price") return priceB - priceA;
        if (roomsFilterState.sort === "highest-rating") return ratingB - ratingA;

        return orderA - orderB;
    });

    matchingCardsList.forEach((card) => grid.appendChild(card));
}

function applyRoomsFilters(resetPage = true) {
    const resultCount = document.getElementById("roomsResultCount");
    const emptyMessage = document.getElementById("roomsEmptyMessage");
    const cards = getRoomCards();
    const { minPrice, maxPrice } = getSelectedPriceRange();

    if (resetPage) {
        currentRoomsPage = 1;
    }

    matchingCardsList = [];

    cards.forEach((card) => {
        const roomPrice = Number(card.dataset.price || 0);
        const cardResort = (card.dataset.resort || "").toLowerCase().trim();
        const cardType = (card.dataset.type || "").toLowerCase().trim();
        const filterResort = (roomsFilterState.resort || "all").toLowerCase().trim();
        const filterType = (roomsFilterState.type || "all").toLowerCase().trim();

        const matchResort = filterResort === "all" ||
            cardResort === filterResort ||
            cardResort.includes(filterResort) ||
            filterResort.includes(cardResort);

        const matchType = filterType === "all" ||
            cardType === filterType ||
            cardType.replace(/_/g, "-") === filterType.replace(/_/g, "-") ||
            cardType.includes(filterType) ||
            filterType.includes(cardType);

        const matchGuests = roomMatchesGuests(card.dataset.guests);
        const matchView = roomMatchesView(card.dataset.view);
        const matchPrice = roomPrice >= minPrice && roomPrice <= maxPrice;

        const isMatch = matchResort && matchType && matchGuests && matchView && matchPrice;

        if (isMatch) {
            matchingCardsList.push(card);
        } else {
            card.hidden = true;
            card.style.display = "none";
        }
    });

    sortRooms();

    if (resultCount) {
        resultCount.textContent = String(matchingCardsList.length);
    }

    if (emptyMessage) {
        emptyMessage.hidden = matchingCardsList.length !== 0;
        emptyMessage.style.display = matchingCardsList.length === 0 ? "block" : "none";
    }

    renderRoomsPagination();
}

function renderRoomsPagination() {
    const paginationEl = document.getElementById("roomsPagination");
    const numbersEl = document.getElementById("roomsPaginationNumbers");
    const prevBtn = document.getElementById("roomsPrevPage");
    const nextBtn = document.getElementById("roomsNextPage");

    const total = matchingCardsList.length;
    const totalPages = Math.ceil(total / ROOMS_PER_PAGE);

    currentRoomsPage = Math.min(Math.max(1, currentRoomsPage), totalPages || 1);

    if (total <= ROOMS_PER_PAGE) {
        if (paginationEl) {
            paginationEl.hidden = true;
            paginationEl.style.display = "none";
        }

        matchingCardsList.forEach((card) => {
            card.hidden = false;
            card.style.display = "";
        });
        return;
    }

    if (paginationEl) {
        paginationEl.hidden = false;
        paginationEl.style.display = "flex";
    }

    const startIndex = (currentRoomsPage - 1) * ROOMS_PER_PAGE;
    const endIndex = startIndex + ROOMS_PER_PAGE;

    matchingCardsList.forEach((card, index) => {
        const isVisible = index >= startIndex && index < endIndex;
        card.hidden = !isVisible;
        card.style.display = isVisible ? "" : "none";
    });

    if (prevBtn) {
        prevBtn.disabled = currentRoomsPage === 1;
    }

    if (nextBtn) {
        nextBtn.disabled = currentRoomsPage === totalPages;
    }

    if (!numbersEl) return;

    numbersEl.innerHTML = "";

    const pages = getPaginationPageList(currentRoomsPage, totalPages);

    pages.forEach((p) => {
        if (p === "...") {
            const ellipsis = document.createElement("span");
            ellipsis.className = "rooms-pagination__ellipsis";
            ellipsis.textContent = "...";
            numbersEl.appendChild(ellipsis);
        } else {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = `rooms-pagination__number ${p === currentRoomsPage ? "is-active" : ""}`;
            btn.textContent = String(p);
            btn.setAttribute("aria-label", `Page ${p}`);
            btn.addEventListener("click", () => {
                if (currentRoomsPage !== p) {
                    currentRoomsPage = p;
                    renderRoomsPagination();
                    scrollToResults();
                }
            });
            numbersEl.appendChild(btn);
        }
    });
}

function getPaginationPageList(current, total) {
    if (total <= 7) {
        const list = [];
        for (let i = 1; i <= total; i += 1) list.push(i);
        return list;
    }

    if (current <= 4) {
        return [1, 2, 3, 4, 5, "...", total];
    }

    if (current >= total - 3) {
        return [1, "...", total - 4, total - 3, total - 2, total - 1, total];
    }

    return [1, "...", current - 1, current, current + 1, "...", total];
}

function initRoomsPage() {
    initRoomsDropdowns();
    initRoomsPriceFilter();
    initRoomsFiltering();
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initRoomsPage);
} else {
    initRoomsPage();
}