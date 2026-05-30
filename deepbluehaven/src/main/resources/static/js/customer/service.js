document.addEventListener("DOMContentLoaded", () => {
    initServiceDropdowns();
    initServicePriceFilter();
    initServiceFiltering();
    initServiceSelection();

    console.log("service.js loaded");
});

const serviceFilterState = {
    category: "all",
    type: "all",
    status: "active",
    sort: "recommended"
};

const selectedServices = new Map();

const serviceCategoryAliases = {
    "food-beverage": ["food-beverage", "food_beverage", "food", "beverage", "fnb"],
    laundry: ["laundry"],
    spa: ["spa"],
    transport: ["transport", "transfer"],
    "mini-bar": ["mini-bar", "mini_bar", "minibar"],
    sport: ["sport", "sports"],
    other: ["other"]
};

const serviceTypeAliases = {
    "per-person": ["per-person", "per_person", "person", "per pax", "per-pax"],
    "per-room": ["per-room", "per_room", "room"],
    "per-trip": ["per-trip", "per_trip", "trip"],
    "per-order": ["per-order", "per_order", "order"]
};

const serviceStatusAliases = {
    active: ["active"],
    "out-of-stock": ["out-of-stock", "out_of_stock"],
    hidden: ["hidden"]
};

function getServiceCards() {
    return Array.from(document.querySelectorAll(".service-card"));
}

function normalizeFilterValue(value) {
    return String(value || "")
        .toLowerCase()
        .trim()
        .replace(/_/g, "-")
        .replace(/\s+/g, "-");
}

function toVnd(millionValue) {
    return Number(millionValue || 0) * 1000000;
}

function formatMillion(millionValue) {
    return `${Number(millionValue || 0)} triệu`;
}

function formatVnd(value) {
    return `${Number(value || 0).toLocaleString("vi-VN")} VND`;
}

function parseVnd(text) {
    return Number(String(text || "").replace(/[^0-9]/g, "")) || 0;
}

function initServiceDropdowns() {
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
                const optionValue = normalizeFilterValue(option.dataset.value || "all");

                if (filterName && Object.prototype.hasOwnProperty.call(serviceFilterState, filterName)) {
                    serviceFilterState[filterName] = optionValue;
                }

                if (valueText) {
                    valueText.textContent = option.textContent.trim();
                }

                options.forEach((item) => item.classList.remove("is-active"));
                option.classList.add("is-active");

                dropdown.removeAttribute("open");
                applyServiceFilters();
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

function initServicePriceFilter() {
    const priceMin = document.getElementById("servicePriceMin");
    const priceMax = document.getElementById("servicePriceMax");
    const priceSlider = document.querySelector(".rooms-price-slider");
    const priceFill = document.getElementById("serviceSliderFill");
    const priceMinLabel = document.getElementById("servicePriceMinLabel");
    const priceMaxLabel = document.getElementById("servicePriceMaxLabel");
    const priceValue = document.querySelector(".rooms-price-filter__value");

    if (!priceMin || !priceMax) {
        return;
    }

    const getConfig = () => ({
        min: Number(priceMin.min || 0),
        max: Number(priceMin.max || 10),
        step: Number(priceMin.step || 1)
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
            priceValue.textContent = `${formatMillion(minValue)} – ${formatMillion(maxValue)}`;
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
        applyServiceFilters();
    });

    priceMax.addEventListener("input", () => {
        const config = getConfig();
        const minValue = Number(priceMin.value);
        const maxValue = Number(priceMax.value);

        if (maxValue <= minValue) {
            priceMax.value = String(minValue + config.step);
        }

        updatePriceUI();
        applyServiceFilters();
    });

    updatePriceUI();
}

function initServiceFiltering() {
    const applyButton = document.querySelector(".rooms-filter-apply");

    getServiceCards().forEach((card, index) => {
        if (!card.dataset.order) {
            card.dataset.order = String(index + 1);
        }
    });

    if (applyButton) {
        applyButton.addEventListener("click", (event) => {
            event.preventDefault();
            applyServiceFilters();
            showServiceToast("info", "Filters applied", "The service list has been updated.");
        });
    }

    applyServiceFilters();
}

function getSelectedServicePriceRange() {
    const priceMin = document.getElementById("servicePriceMin");
    const priceMax = document.getElementById("servicePriceMax");

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

function matchesWithAliases(cardValue, selectedValue, aliases = {}) {
    if (selectedValue === "all" || selectedValue === "any") {
        return true;
    }

    const normalizedCardValue = normalizeFilterValue(cardValue);
    const normalizedSelectedValue = normalizeFilterValue(selectedValue);

    if (normalizedCardValue === normalizedSelectedValue) {
        return true;
    }

    const aliasList = aliases[normalizedSelectedValue] || [];

    return aliasList.some((alias) => {
        const normalizedAlias = normalizeFilterValue(alias);
        return normalizedCardValue === normalizedAlias || normalizedCardValue.includes(normalizedAlias);
    });
}

function sortServices() {
    const grid = document.getElementById("servicesGrid") || document.querySelector(".services-grid");

    if (!grid) {
        return;
    }

    const cards = getServiceCards();

    cards.sort((a, b) => {
        const priceA = Number(a.dataset.price || 0);
        const priceB = Number(b.dataset.price || 0);
        const orderA = Number(a.dataset.order || 0);
        const orderB = Number(b.dataset.order || 0);
        const nameA = getServiceNameFromCard(a).toLowerCase();
        const nameB = getServiceNameFromCard(b).toLowerCase();

        if (serviceFilterState.sort === "lowest-price") return priceA - priceB;
        if (serviceFilterState.sort === "highest-price") return priceB - priceA;
        if (serviceFilterState.sort === "name-az") return nameA.localeCompare(nameB);

        return orderA - orderB;
    });

    cards.forEach((card) => grid.appendChild(card));
}

function applyServiceFilters() {
    const resultCount = document.getElementById("servicesResultCount");
    const emptyMessage = document.getElementById("servicesEmptyMessage");
    const cards = getServiceCards();
    const { minPrice, maxPrice } = getSelectedServicePriceRange();

    let visibleServices = 0;

    cards.forEach((card) => {
        const servicePrice = Number(card.dataset.price || 0);

        const isMatch =
            matchesWithAliases(card.dataset.category, serviceFilterState.category, serviceCategoryAliases) &&
            matchesWithAliases(card.dataset.type, serviceFilterState.type, serviceTypeAliases) &&
            matchesWithAliases(card.dataset.status, serviceFilterState.status, serviceStatusAliases) &&
            servicePrice >= minPrice &&
            servicePrice <= maxPrice;

        card.hidden = !isMatch;

        if (isMatch) {
            visibleServices += 1;
        }
    });

    sortServices();

    if (resultCount) {
        resultCount.textContent = String(visibleServices);
    }

    if (emptyMessage) {
        emptyMessage.hidden = visibleServices !== 0;
    }
}

function initServiceSelection() {
    const servicesGrid = document.getElementById("servicesGrid") || document.querySelector(".services-grid");
    const selectedList = document.getElementById("selectedServiceList");
    const confirmButton = document.querySelector(".booking-summary__action");

    if (selectedList) {
        selectedList.innerHTML = "";
    }

    renderSelectedServices();

    if (servicesGrid) {
        servicesGrid.addEventListener("click", (event) => {
            const button = event.target.closest(".service-card__action");

            if (!button) {
                return;
            }

            event.preventDefault();

            const card = button.closest(".service-card");

            if (!card) {
                return;
            }

            addSelectedService(card);
        });
    }

    if (selectedList) {
        selectedList.addEventListener("click", (event) => {
            const removeButton = event.target.closest(".selected-service-item button");

            if (!removeButton) {
                return;
            }

            event.preventDefault();

            const item = removeButton.closest(".selected-service-item");
            const serviceId = item?.dataset.id;

            if (serviceId) {
                removeSelectedService(serviceId);
            }
        });
    }

    if (confirmButton) {
        confirmButton.addEventListener("click", (event) => {
            event.preventDefault();

            if (!selectedServices.size) {
                showServiceToast("warning", "No service selected", "Please select at least one service before confirming.");
                return;
            }

            showServiceToast("success", "Services confirmed", `${selectedServices.size} service(s) have been added to this booking.`);
        });
    }
}

function getServiceNameFromCard(card) {
    return card.querySelector(".service-card__name")?.textContent.trim() || "Service";
}

function getServiceUnitFromCard(card) {
    const unitText = card.querySelector(".service-card__price small")?.textContent || "";
    return unitText.replace("/", "").trim() || "service";
}

function getServicePriceTextFromCard(card) {
    return card.querySelector(".service-card__price strong")?.textContent.trim() || formatVnd(card.dataset.price);
}

function getServicePriceValueFromCard(card) {
    const dataPrice = Number(card.dataset.price || 0);

    if (dataPrice > 0) {
        return dataPrice;
    }

    return parseVnd(getServicePriceTextFromCard(card));
}

function getServiceIdFromCard(card) {
    const dataId = card.dataset.id;

    if (dataId) {
        return String(dataId);
    }

    return normalizeFilterValue(getServiceNameFromCard(card));
}

function getSelectedBookingId() {
    const bookingSummary = document.querySelector(".booking-summary[data-booking-id]");
    return bookingSummary?.dataset.bookingId || "";
}

function addSelectedService(card) {
    const status = normalizeFilterValue(card.dataset.status);

    if (status !== "active") {
        showServiceToast(
            "warning",
            "Service unavailable",
            "This service is not available right now."
        );
        return;
    }

    const serviceId = getServiceIdFromCard(card);

    if (selectedServices.has(serviceId)) {
        showServiceToast("info", "Already selected", "This service is already in your booking summary.");
        return;
    }

    selectedServices.set(serviceId, {
        id: serviceId,
        name: getServiceNameFromCard(card),
        price: getServicePriceValueFromCard(card),
        priceText: getServicePriceTextFromCard(card),
        unit: getServiceUnitFromCard(card)
    });

    updateCardButton(card, true);
    renderSelectedServices();
    showServiceToast("success", "Service added", `${getServiceNameFromCard(card)} has been added to your booking.`);
}

function removeSelectedService(serviceId) {
    selectedServices.delete(String(serviceId));

    const card = getServiceCards().find((item) => getServiceIdFromCard(item) === String(serviceId));

    if (card) {
        updateCardButton(card, false);
    }

    renderSelectedServices();
    showServiceToast("info", "Service removed", "The service has been removed from your booking summary.");
}

function updateCardButton(card, isSelected) {
    const button = card.querySelector(".service-card__action");

    if (!button) {
        return;
    }

    if (isSelected) {
        button.classList.remove("btn-cta");
        button.classList.add("btn-success");
        button.innerHTML = 'Selected <i class="fa-solid fa-check"></i>';
        return;
    }

    button.classList.remove("btn-success");
    button.classList.add("btn-cta");
    button.innerHTML = 'Add Service <i class="fa-solid fa-plus"></i>';
}

function renderSelectedServices() {
    const selectedList = document.getElementById("selectedServiceList");
    const selectedCount = document.getElementById("selectedServiceCount");
    const selectedTotal = document.getElementById("selectedServiceTotal");
    const selectedEmpty = document.getElementById("selectedServiceEmpty");

    if (!selectedList) {
        return;
    }

    selectedList.innerHTML = "";

    selectedServices.forEach((service) => {
        selectedList.appendChild(createSelectedServiceItem(service));
    });

    const total = Array.from(selectedServices.values()).reduce((sum, service) => {
        return sum + Number(service.price || 0);
    }, 0);

    if (selectedCount) {
        selectedCount.textContent = String(selectedServices.size);
    }

    if (selectedTotal) {
        selectedTotal.textContent = formatVnd(total);
    }

    if (selectedEmpty) {
        selectedEmpty.hidden = selectedServices.size !== 0;
    }
}

function createSelectedServiceItem(service) {
    const item = document.createElement("article");
    item.className = "selected-service-item";
    item.dataset.id = service.id;

    const content = document.createElement("div");

    const name = document.createElement("strong");
    name.textContent = service.name;

    const price = document.createElement("span");
    price.textContent = `${service.priceText} / ${service.unit}`;

    const hiddenInput = document.createElement("input");
    hiddenInput.type = "hidden";
    hiddenInput.name = "serviceIds";
    hiddenInput.value = service.id;

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.setAttribute("aria-label", `Remove ${service.name}`);
    removeButton.innerHTML = '<i class="fa-solid fa-xmark"></i>';

    content.appendChild(name);
    content.appendChild(price);
    content.appendChild(hiddenInput);

    item.appendChild(content);
    item.appendChild(removeButton);

    return item;
}

function showServiceToast(type = "info", title = "Notification", message = "") {
    const container = document.getElementById("toastContainer") || document.querySelector(".toast-container");

    if (!container) {
        return;
    }

    const iconMap = {
        success: "fa-circle-check",
        info: "fa-circle-info",
        warning: "fa-triangle-exclamation",
        error: "fa-circle-xmark"
    };

    const toast = document.createElement("div");
    toast.className = `toast toast-${type}`;

    toast.innerHTML = `
        <div class="toast-icon">
            <i class="fa-solid ${iconMap[type] || iconMap.info}"></i>
        </div>
        <div class="toast-body">
            <strong class="toast-title"></strong>
            <p class="toast-message"></p>
        </div>
        <button type="button" class="toast-close" aria-label="Close notification">
            <i class="fa-solid fa-xmark"></i>
        </button>
    `;

    toast.querySelector(".toast-title").textContent = title;
    toast.querySelector(".toast-message").textContent = message;

    const closeButton = toast.querySelector(".toast-close");

    const closeToast = () => {
        toast.classList.add("hide");
        setTimeout(() => toast.remove(), 300);
    };

    closeButton.addEventListener("click", closeToast);
    container.appendChild(toast);

    setTimeout(closeToast, 3200);
}