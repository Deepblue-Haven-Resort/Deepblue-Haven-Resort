(() => {
    function formatConfirmationVnd(value) {
        return `${Number(value || 0).toLocaleString("vi-VN")} VND`;
    }

    function setDefaultServiceDate() {
        const dateInput = document.getElementById("serviceDate");

        if (!dateInput) {
            return;
        }

        const today = new Date();

        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, "0");
        const day = String(today.getDate()).padStart(2, "0");

        const todayValue = `${year}-${month}-${day}`;

        dateInput.min = todayValue;

        if (!dateInput.value) {
            dateInput.value = todayValue;
        }
    }

    function createConfirmationServiceItem(service) {
        const item = document.createElement("article");
        item.className = "confirmation-service-item";

        const main = document.createElement("div");
        main.className = "confirmation-service-item__main";

        const icon = document.createElement("span");
        icon.className = "confirmation-service-item__icon";
        icon.innerHTML =
            '<i class="fa-solid fa-bell-concierge"></i>';

        const copy = document.createElement("div");
        copy.className = "confirmation-service-item__copy";

        const name = document.createElement("h3");
        name.textContent = service.name || "Service";

        const description = document.createElement("p");
        description.textContent = service.unit
            ? `Per ${service.unit}`
            : "Service";

        copy.appendChild(name);
        copy.appendChild(description);

        main.appendChild(icon);
        main.appendChild(copy);

        const priceWrapper = document.createElement("div");
        priceWrapper.className =
            "confirmation-service-item__price";

        const price = Number(service.price || 0);

        const priceText = document.createElement("strong");
        priceText.textContent = formatConfirmationVnd(price);

        const unitPrice = document.createElement("small");
        unitPrice.textContent =
            service.priceText || formatConfirmationVnd(price);

        priceWrapper.appendChild(priceText);
        priceWrapper.appendChild(unitPrice);

        item.appendChild(main);
        item.appendChild(priceWrapper);

        return item;
    }

    function closeServiceConfirmationModal() {
        const modal = document.getElementById(
            "serviceConfirmationModal"
        );

        if (!modal) {
            return;
        }

        modal.classList.remove("is-open");
        modal.setAttribute("aria-hidden", "true");

        document.body.classList.remove(
            "service-confirmation-modal-open"
        );
    }

    /*
     * Hàm global được service.js gọi.
     * Phải khai báo bằng window để service.js có thể truy cập.
     */
    window.openServiceConfirmationModal = function (
        services = [],
        bookingInformation = {}
    ) {
        const modal = document.getElementById(
            "serviceConfirmationModal"
        );

        const guestNameInput = document.getElementById(
            "confirmationGuestName"
        );

        const roomInput = document.getElementById(
            "confirmationRoom"
        );

        const serviceList = document.getElementById(
            "confirmationServiceList"
        );

        const serviceCount = document.getElementById(
            "confirmationServiceCount"
        );

        const totalElement = document.getElementById(
            "confirmationTotal"
        );

        const selectedServicesJsonInput =
            document.getElementById(
                "selectedServicesJson"
            );

        if (!modal) {
            console.error(
                "Element #serviceConfirmationModal was not found."
            );
            return;
        }

        if (!serviceList) {
            console.error(
                "Element #confirmationServiceList was not found."
            );
            return;
        }

        if (guestNameInput) {
            guestNameInput.value =
                bookingInformation.guestName || "";
        }

        if (roomInput) {
            roomInput.value =
                bookingInformation.room || "";
        }

        serviceList.innerHTML = "";

        services.forEach((service) => {
            const serviceItem =
                createConfirmationServiceItem(service);

            serviceList.appendChild(serviceItem);
        });

        const total = services.reduce((sum, service) => {
            return sum + Number(service.price || 0);
        }, 0);

        if (serviceCount) {
            serviceCount.textContent =
                String(services.length);
        }

        if (totalElement) {
            totalElement.textContent =
                formatConfirmationVnd(total);
        }

        if (selectedServicesJsonInput) {
            selectedServicesJsonInput.value =
                JSON.stringify(services);
        }

        setDefaultServiceDate();

        modal.classList.add("is-open");
        modal.setAttribute("aria-hidden", "false");

        document.body.classList.add(
            "service-confirmation-modal-open"
        );

        const closeButton = modal.querySelector(
            ".service-confirmation-modal__close"
        );

        if (closeButton) {
            closeButton.focus();
        }
    };

    document.addEventListener("DOMContentLoaded", () => {
        const modal = document.getElementById(
            "serviceConfirmationModal"
        );

        if (!modal) {
            console.error(
                "Service confirmation modal fragment was not rendered."
            );
            return;
        }

        const closeElements = modal.querySelectorAll(
            "[data-confirmation-close]"
        );

        closeElements.forEach((element) => {
            element.addEventListener(
                "click",
                closeServiceConfirmationModal
            );
        });

        document.addEventListener("keydown", (event) => {
            if (
                event.key === "Escape" &&
                modal.classList.contains("is-open")
            ) {
                closeServiceConfirmationModal();
            }
        });

        console.log(
            "service-confirmation.js loaded"
        );
    });
})();