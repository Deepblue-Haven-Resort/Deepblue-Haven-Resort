document.addEventListener("DOMContentLoaded", () => {
    const dropdowns = document.querySelectorAll(".rooms-dropdown");
    const grid = document.querySelector("#roomsGrid") || document.querySelector(".rooms-grid");
    const resultCount = document.querySelector("#roomsResultCount");
    const emptyMessage = document.querySelector("#roomsEmptyMessage");
    const applyButton = document.querySelector(".rooms-filter-apply");

    const priceMin = document.querySelector("#priceMin");
    const priceMax = document.querySelector("#priceMax");
    const priceMinLabel = document.querySelector("#priceMinLabel");
    const priceMaxLabel = document.querySelector("#priceMaxLabel");
    const priceFill = document.querySelector("#sliderFill");
    const priceSlider = document.querySelector(".rooms-price-slider");
    const priceValue = document.querySelector(".rooms-price-filter__value");

    const filterState = {
        resort: "all",
        type: "all",
        guests: "any",
        view: "any",
        sort: "recommended"
    };

    const toVnd = (millionValue) => Number(millionValue) * 1000000;

    const formatMillion = (millionValue) => {
        const value = Number(millionValue);
        return `${value} triệu`;
    };

    const getCards = () => Array.from(document.querySelectorAll(".room-card"));

    const closeOtherDropdowns = (currentDropdown) => {
        dropdowns.forEach((dropdown) => {
            if (dropdown !== currentDropdown) {
                dropdown.removeAttribute("open");
            }
        });
    };

    const updateDropdownUI = (dropdown, selectedOption) => {
        const valueText = dropdown.querySelector(".rooms-dropdown__value");
        const options = dropdown.querySelectorAll(".rooms-dropdown__option");

        if (valueText) {
            valueText.textContent = selectedOption.textContent.trim();
        }

        options.forEach((option) => option.classList.remove("is-active"));
        selectedOption.classList.add("is-active");
        dropdown.removeAttribute("open");
    };

    const updatePriceSlider = (event) => {
        if (!priceMin || !priceMax) return;

        const step = Number(priceMin.step || 5);
        const sliderMin = Number(priceMin.min || 0);
        const sliderMax = Number(priceMin.max || 100);

        let minValue = Number(priceMin.value);
        let maxValue = Number(priceMax.value);

        if (minValue >= maxValue) {
            if (event && event.target === priceMin) {
                minValue = maxValue - step;
                priceMin.value = minValue;
            } else {
                maxValue = minValue + step;
                priceMax.value = maxValue;
            }
        }

        minValue = Math.max(sliderMin, minValue);
        maxValue = Math.min(sliderMax, maxValue);

        const minPercent = ((minValue - sliderMin) / (sliderMax - sliderMin)) * 100;
        const maxPercent = ((maxValue - sliderMin) / (sliderMax - sliderMin)) * 100;

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
            priceValue.textContent = `${formatMillion(minValue)} – ${formatMillion(maxValue)} / đêm`;
        }
    };

    const getPriceRange = () => {
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
    };

    const matchesGuests = (cardGuests) => {
        if (filterState.guests === "any") return true;

        const capacity = Number(cardGuests || 0);
        const requestedGuests = filterState.guests === "4+" ? 4 : Number(filterState.guests);

        return capacity >= requestedGuests;
    };

    const matchesView = (cardViews) => {
        if (filterState.view === "any") return true;

        const views = String(cardViews || "")
            .split(",")
            .map((view) => view.trim())
            .filter(Boolean);

        return views.includes(filterState.view);
    };

    const sortRooms = () => {
        if (!grid) return;

        const cards = getCards();

        const sortedCards = cards.sort((a, b) => {
            const priceA = Number(a.dataset.price || 0);
            const priceB = Number(b.dataset.price || 0);
            const ratingA = Number(a.dataset.rating || 0);
            const ratingB = Number(b.dataset.rating || 0);
            const orderA = Number(a.dataset.order || 0);
            const orderB = Number(b.dataset.order || 0);

            if (filterState.sort === "lowest-price") {
                return priceA - priceB;
            }

            if (filterState.sort === "highest-price") {
                return priceB - priceA;
            }

            if (filterState.sort === "highest-rating") {
                return ratingB - ratingA;
            }

            return orderA - orderB;
        });

        sortedCards.forEach((card) => grid.appendChild(card));
    };

    const applyFilters = () => {
        const { minPrice, maxPrice } = getPriceRange();
        let visibleRooms = 0;

        getCards().forEach((card) => {
            const roomPrice = Number(card.dataset.price || 0);

            const isMatch =
                (filterState.resort === "all" || card.dataset.resort === filterState.resort) &&
                (filterState.type === "all" || card.dataset.type === filterState.type) &&
                matchesGuests(card.dataset.guests) &&
                matchesView(card.dataset.view) &&
                roomPrice >= minPrice &&
                roomPrice <= maxPrice;

            card.hidden = !isMatch;

            if (isMatch) {
                visibleRooms += 1;
            }
        });

        sortRooms();

        if (resultCount) {
            resultCount.textContent = visibleRooms;
        }

        if (emptyMessage) {
            emptyMessage.hidden = visibleRooms !== 0;
        }
    };

    dropdowns.forEach((dropdown) => {
        const filterName = dropdown.dataset.filter;
        const options = dropdown.querySelectorAll(".rooms-dropdown__option");

        dropdown.addEventListener("toggle", () => {
            if (dropdown.open) {
                closeOtherDropdowns(dropdown);
            }
        });

        options.forEach((option) => {
            option.addEventListener("click", () => {
                if (filterName) {
                    filterState[filterName] = option.dataset.value || "all";
                }

                updateDropdownUI(dropdown, option);
                applyFilters();
            });
        });
    });

    document.addEventListener("click", (event) => {
        if (!event.target.closest(".rooms-dropdown")) {
            dropdowns.forEach((dropdown) => dropdown.removeAttribute("open"));
        }
    });

    [priceMin, priceMax].forEach((input) => {
        if (!input) return;

        input.addEventListener("input", (event) => {
            updatePriceSlider(event);
            applyFilters();
        });
    });

    if (applyButton) {
        applyButton.addEventListener("click", applyFilters);
    }

    getCards().forEach((card, index) => {
        if (!card.dataset.order) {
            card.dataset.order = String(index + 1);
        }
    });

    updatePriceSlider();
    applyFilters();
});