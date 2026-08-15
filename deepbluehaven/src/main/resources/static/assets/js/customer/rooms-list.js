document.addEventListener("DOMContentLoaded", () => {
    initRoomsDropdowns();
    initRoomsPriceFilter();
    initRoomsFiltering();

    console.log("rooms-list.js loaded");
});

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

    if (!dropdowns.length) {
        return;
    }

    dropdowns.forEach((dropdown) => {
        dropdown.addEventListener("toggle", () => {
            if (!dropdown.open) {
                return;
            }

            dropdowns.forEach((item) => {
                if (item !== dropdown) {
                    item.removeAttribute("open");
                }
            });
        });

        const options = dropdown.querySelectorAll(".rooms-dropdown__option");

        options.forEach((option) => {
            option.addEventListener("click", (event) => {
                event.preventDefault();

                const filterName = dropdown.dataset.filter;
                const valueText = dropdown.querySelector(".rooms-dropdown__value");

                if (filterName) {
                    roomsFilterState[filterName] = option.dataset.value || "all";
                }

                if (valueText) {
                    valueText.textContent = option.textContent.trim();
                }

                options.forEach((item) => item.classList.remove("is-active"));
                option.classList.add("is-active");

                dropdown.removeAttribute("open");
                applyRoomsFilters();
            });
        });
    });

    document.addEventListener("click", (event) => {
        if (event.target.closest(".rooms-dropdown")) {
            return;
        }

        dropdowns.forEach((dropdown) => dropdown.removeAttribute("open"));
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

    if (!priceMin || !priceMax) {
        return;
    }

    const getConfig = () => ({
        min: Number(priceMin.min || 0),
        max: Number(priceMin.max || 10),
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
        applyRoomsFilters();
    });

    priceMax.addEventListener("input", () => {
        const config = getConfig();
        const minValue = Number(priceMin.value);
        const maxValue = Number(priceMax.value);

        if (maxValue <= minValue) {
            priceMax.value = String(minValue + config.step);
        }

        updatePriceUI();
        applyRoomsFilters();
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
            applyRoomsFilters();
        });
    }

    applyRoomsFilters();
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

    return {
        minPrice: toVnd(priceMin.value),
        maxPrice: toVnd(priceMax.value)
    };
}

function roomMatchesGuests(cardGuests) {
    if (roomsFilterState.guests === "any") {
        return true;
    }

    const capacity = Number(cardGuests || 0);
    const requestedGuests = roomsFilterState.guests === "4+"
        ? 4
        : Number(roomsFilterState.guests);

    return capacity >= requestedGuests;
}

function roomMatchesView(cardViews) {
    if (roomsFilterState.view === "any") {
        return true;
    }

    const views = String(cardViews || "")
        .split(",")
        .map((view) => view.trim())
        .filter(Boolean);

    return views.includes(roomsFilterState.view);
}

function sortRooms() {
    const grid = document.getElementById("roomsGrid") || document.querySelector(".rooms-grid");

    if (!grid) {
        return;
    }

    const cards = getRoomCards();

    cards.sort((a, b) => {
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

    cards.forEach((card) => grid.appendChild(card));
}

function applyRoomsFilters() {
    const resultCount = document.getElementById("roomsResultCount");
    const emptyMessage = document.getElementById("roomsEmptyMessage");
    const cards = getRoomCards();
    const { minPrice, maxPrice } = getSelectedPriceRange();

    let visibleRooms = 0;

    cards.forEach((card) => {
        const roomPrice = Number(card.dataset.price || 0);

        const isMatch =
            (roomsFilterState.resort === "all" || card.dataset.resort === roomsFilterState.resort) &&
            (roomsFilterState.type === "all" || card.dataset.type === roomsFilterState.type) &&
            roomMatchesGuests(card.dataset.guests) &&
            roomMatchesView(card.dataset.view) &&
            roomPrice >= minPrice &&
            roomPrice <= maxPrice;

        card.hidden = !isMatch;

        if (isMatch) {
            visibleRooms += 1;
        }
    });

    sortRooms();

    if (resultCount) {
        resultCount.textContent = String(visibleRooms);
    }

    if (emptyMessage) {
        emptyMessage.hidden = visibleRooms !== 0;
    }
}