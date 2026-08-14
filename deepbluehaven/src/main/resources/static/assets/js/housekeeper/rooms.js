(function () {
    document.addEventListener("DOMContentLoaded", function () {
        const searchInput = document.querySelector(".filter-bar .search-box input");
        const dropdown = document.querySelector(".filter-bar .filter-dropdown");
        const cards = Array.from(document.querySelectorAll(".rooms-status-grid article.chart-card"));
        let currentStatus = "ALL";
        let currentKeyword = "";

        function filterRooms() {
            let visibleCount = 0;
            cards.forEach(card => {
                const cardStatus = (card.getAttribute("data-status") || "").toUpperCase();
                const statusText = (card.querySelector(".status")?.textContent || "").toUpperCase();
                const textContent = card.textContent.toLowerCase();

                const matchesSearch = !currentKeyword || textContent.includes(currentKeyword);
                const matchesStatus = (currentStatus === "ALL") ||
                    cardStatus.includes(currentStatus) ||
                    statusText.includes(currentStatus);

                if (matchesSearch && matchesStatus) {
                    card.style.display = "";
                    visibleCount++;
                } else {
                    card.style.display = "none";
                }
            });

            let emptyMsg = document.getElementById("emptyRoomsMsg");
            if (visibleCount === 0) {
                if (!emptyMsg) {
                    emptyMsg = document.createElement("div");
                    emptyMsg.id = "emptyRoomsMsg";
                    emptyMsg.style.gridColumn = "1 / -1";
                    emptyMsg.style.textAlign = "center";
                    emptyMsg.style.padding = "32px";
                    emptyMsg.style.color = "var(--gray-500)";
                    emptyMsg.innerHTML = '<i class="fa-solid fa-circle-question"></i> No rooms match the selected filter.';
                    document.querySelector(".rooms-status-grid").appendChild(emptyMsg);
                }
                emptyMsg.style.display = "";
            } else if (emptyMsg) {
                emptyMsg.style.display = "none";
            }
        }

        if (dropdown) {
            const btn = dropdown.querySelector(".filter-btn");
            const menu = dropdown.querySelector(".filter-menu");
            const label = dropdown.querySelector(".filter-label strong");

            if (btn) {
                btn.addEventListener("click", e => {
                    e.stopPropagation();
                    dropdown.classList.toggle("active");
                });
            }

            if (menu) {
                menu.querySelectorAll("button").forEach(optBtn => {
                    optBtn.addEventListener("click", () => {
                        const statusVal = optBtn.getAttribute("data-status") || optBtn.textContent.trim().toUpperCase();
                        if (label) label.textContent = optBtn.textContent.trim();
                        dropdown.classList.remove("active");

                        if (statusVal.includes("ALL")) currentStatus = "ALL";
                        else if (statusVal.includes("CLEANING")) currentStatus = "CLEANING";
                        else if (statusVal.includes("AVAILABLE")) currentStatus = "AVAILABLE";
                        else if (statusVal.includes("OCCUPIED")) currentStatus = "OCCUPIED";
                        else if (statusVal.includes("MAINTENANCE")) currentStatus = "MAINTENANCE";
                        else currentStatus = statusVal;

                        filterRooms();
                    });
                });
            }

            document.addEventListener("click", () => dropdown.classList.remove("active"));
        }

        if (searchInput) {
            searchInput.addEventListener("input", function () {
                currentKeyword = this.value.toLowerCase().trim();
                filterRooms();
            });
        }
    });
})();
