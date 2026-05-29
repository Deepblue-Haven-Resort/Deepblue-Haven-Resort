(() => {
    "use strict";

    const initRoomsList = () => {
        const dropdowns = Array.from(document.querySelectorAll(".rooms-dropdown"));
        const grid = document.querySelector("#roomsGrid") || document.querySelector(".rooms-grid");
        const resultCount = document.querySelector("#roomsResultCount");
        const emptyMessage = document.querySelector("#roomsEmptyMessage");
        const applyButton = document.querySelector(".rooms-filter-apply");

        const priceSlider = document.querySelector(".rooms-price-slider");
        const priceMin = document.querySelector("#priceMin");
        const priceMax = document.querySelector("#priceMax");
        const priceMinLabel = document.querySelector("#priceMinLabel");
        const priceMaxLabel = document.querySelector("#priceMaxLabel");
        const priceFill = document.querySelector("#sliderFill");
        const priceValue = document.querySelector(".rooms-price-filter__value");

        const filterState = {
            resort: "all",
            type: "all",
            guests: "any",
            view: "any",
            sort: "recommended"
        };

        let activePriceInput = null;

        const toVnd = (millionValue) => Number(millionValue || 0) * 1000000;
        const formatMillion = (millionValue) => `${Number(millionValue || 0)} triệu`;
        const getCards = () => Array.from(document.querySelectorAll(".room-card"));

        const getSliderConfig = () => {
            if (!priceMin || !priceMax) {
                return { min: 0, max: 100, step: 5 };
            }

            return {
                min: Number(priceMin.min || 0),
                max: Number(priceMin.max || 100),
                step: Number(priceMin.step || 5)
            };
        };

        const clamp = (value, min, max) => Math.min(Math.max(value, min), max);

        const snapToStep = (value) => {
            const config = getSliderConfig();
            const snapped = Math.round(value / config.step) * config.step;
            return clamp(snapped, config.min, config.max);
        };

        const closeOtherDropdowns = (currentDropdown) => {
            dropdowns.forEach((dropdown) => {
                if (dropdown !== currentDropdown) {
                    dropdown.removeAttribute("open");
                }
            });
        };

        const updateDropdownUI = (dropdown, selectedOption) => {
            if (!dropdown || !selectedOption) return;

            const valueText = dropdown.querySelector(".rooms-dropdown__value");
            const options = dropdown.querySelectorAll(".rooms-dropdown__option");

            if (valueText) {
                valueText.textContent = selectedOption.textContent.trim();
            }

            options.forEach((option) => option.classList.remove("is-active"));
            selectedOption.classList.add("is-active");

            dropdown.removeAttribute("open");
        };

        const updatePriceSlider = () => {
            if (!priceMin || !priceMax) return;

            const config = getSliderConfig();

            let minValue = Number(priceMin.value);
            let maxValue = Number(priceMax.value);

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

            cards.sort((a, b) => {
                const priceA = Number(a.dataset.price || 0);
                const priceB = Number(b.dataset.price || 0);
                const ratingA = Number(a.dataset.rating || 0);
                const ratingB = Number(b.dataset.rating || 0);
                const orderA = Number(a.dataset.order || 0);
                const orderB = Number(b.dataset.order || 0);

                if (filterState.sort === "lowest-price") return priceA - priceB;
                if (filterState.sort === "highest-price") return priceB - priceA;
                if (filterState.sort === "highest-rating") return ratingB - ratingA;

                return orderA - orderB;
            });

            cards.forEach((card) => grid.appendChild(card));
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
                resultCount.textContent = String(visibleRooms);
            }

            if (emptyMessage) {
                emptyMessage.hidden = visibleRooms !== 0;
            }
        };

        const setPriceFromPointer = (event) => {
            if (!priceSlider || !priceMin || !priceMax || !activePriceInput) return;

            const config = getSliderConfig();
            const rect = priceSlider.getBoundingClientRect();
            const pointerX = event.clientX ?? rect.left;

            const percent = clamp((pointerX - rect.left) / rect.width, 0, 1);
            const rawValue = config.min + percent * (config.max - config.min);
            const nextValue = snapToStep(rawValue);

            const currentMin = Number(priceMin.value);
            const currentMax = Number(priceMax.value);

            if (activePriceInput === priceMin) {
                priceMin.value = String(clamp(nextValue, config.min, currentMax - config.step));
            }

            if (activePriceInput === priceMax) {
                priceMax.value = String(clamp(nextValue, currentMin + config.step, config.max));
            }

            updatePriceSlider();
            applyFilters();
        };

        const chooseActivePriceInput = (event) => {
            if (!priceSlider || !priceMin || !priceMax) return null;

            const config = getSliderConfig();
            const rect = priceSlider.getBoundingClientRect();
            const pointerX = event.clientX ?? rect.left;

            const percent = clamp((pointerX - rect.left) / rect.width, 0, 1);
            const rawValue = config.min + percent * (config.max - config.min);
            const clickedValue = snapToStep(rawValue);

            const minValue = Number(priceMin.value);
            const maxValue = Number(priceMax.value);

            const distanceToMin = Math.abs(clickedValue - minValue);
            const distanceToMax = Math.abs(clickedValue - maxValue);

            return distanceToMin <= distanceToMax ? priceMin : priceMax;
        };

        dropdowns.forEach((dropdown) => {
            dropdown.addEventListener("toggle", () => {
                if (dropdown.open) {
                    closeOtherDropdowns(dropdown);
                }
            });
        });

        document.addEventListener("click", (event) => {
            const selectedOption = event.target.closest(".rooms-dropdown__option");

            if (selectedOption) {
                event.preventDefault();
                event.stopPropagation();

                const dropdown = selectedOption.closest(".rooms-dropdown");
                const filterName = dropdown?.dataset.filter;

                if (filterName) {
                    filterState[filterName] = selectedOption.dataset.value || "all";
                }

                updateDropdownUI(dropdown, selectedOption);
                applyFilters();
                return;
            }

            if (!event.target.closest(".rooms-dropdown")) {
                dropdowns.forEach((dropdown) => dropdown.removeAttribute("open"));
            }
        });

        if (priceSlider && priceMin && priceMax) {
            priceSlider.addEventListener("pointerdown", (event) => {
                event.preventDefault();

                activePriceInput = chooseActivePriceInput(event);
                priceSlider.setPointerCapture?.(event.pointerId);

                setPriceFromPointer(event);
            });

            priceSlider.addEventListener("pointermove", (event) => {
                if (!activePriceInput) return;

                event.preventDefault();
                setPriceFromPointer(event);
            });

            const stopDragging = (event) => {
                if (!activePriceInput) return;

                priceSlider.releasePointerCapture?.(event.pointerId);
                activePriceInput = null;
            };

            priceSlider.addEventListener("pointerup", stopDragging);
            priceSlider.addEventListener("pointercancel", stopDragging);
            priceSlider.addEventListener("lostpointercapture", () => {
                activePriceInput = null;
            });
        }

        [priceMin, priceMax].forEach((input) => {
            if (!input) return;

            input.addEventListener("input", () => {
                updatePriceSlider();
                applyFilters();
            });

            input.addEventListener("change", () => {
                updatePriceSlider();
                applyFilters();
            });
        });

        if (applyButton) {
            applyButton.addEventListener("click", (event) => {
                event.preventDefault();
                updatePriceSlider();
                applyFilters();
            });
        }

        getCards().forEach((card, index) => {
            if (!card.dataset.order) {
                card.dataset.order = String(index + 1);
            }
        });

        updatePriceSlider();
        applyFilters();

        console.log("rooms-list.js loaded");
    };

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initRoomsList);
    } else {
        initRoomsList();
    }
})();