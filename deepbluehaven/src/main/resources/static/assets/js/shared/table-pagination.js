// table-pagination.js
document.addEventListener("DOMContentLoaded", function () {
    window.initTablePagination = function() {
        function formatEnumText(text) {
            if (!text || typeof text !== 'string') return text;
            const trimmed = text.trim();
            if (!/^[A-Z0-9_]+$/.test(trimmed)) return text;

            const customMappings = {
                'CHECKED_IN': 'Checked In',
                'CHECKED_OUT': 'Checked Out',
                'OUT_OF_STOCK': 'Out of Stock',
                'FOOD_BEVERAGE': 'Food & Beverage',
                'ON_LEAVE': 'On Leave',
                'IN_PROGRESS': 'In Progress',
                'MINI_BAR': 'Mini Bar',
                'FIXED_AMOUNT': 'Fixed Amount',
                'PRESIDENT': 'President Suite',
                'PREMIUM': 'Premium Room',
                'SUITE': 'Deluxe Suite',
                'STANDARD': 'Standard Room',
                'INSPECTED': 'Inspected',
                'CLEANING': 'Cleaning',
                'MAINTENANCE': 'Maintenance',
                'AVAILABLE': 'Available',
                'OCCUPIED': 'Occupied'
            };

            if (customMappings[trimmed]) {
                return customMappings[trimmed];
            }

            return trimmed
                .toLowerCase()
                .split('_')
                .map(w => w.charAt(0).toUpperCase() + w.slice(1))
                .join(' ');
        }

        function formatAllTableCells() {
            document.querySelectorAll("table td").forEach(td => {
                // Format badges, status spans, small tags, and action spans
                td.querySelectorAll(".status, .activity-badge, .badge, .status-badge, .badge-status, small, div.filter-options > span, .chip").forEach(el => {
                    if (el.children.length === 0 || (el.children.length === 1 && el.querySelector("i"))) {
                        const icon = el.querySelector("i");
                        const iconHtml = icon ? icon.outerHTML + ' ' : '';
                        let rawText = Array.from(el.childNodes).filter(n => n.nodeType === 3).map(n => n.textContent).join(' ').trim();
                        if (!rawText) rawText = el.textContent.trim();
                        if (/^[A-Z0-9_]{3,}$/.test(rawText)) {
                            el.innerHTML = iconHtml + formatEnumText(rawText);
                        }
                    }
                });

                // Direct td text without children
                if (td.children.length === 0) {
                    const txt = td.textContent.trim();
                    if (/^[A-Z0-9_]{3,}$/.test(txt)) {
                        td.textContent = formatEnumText(txt);
                    }
                }
            });
        }

        formatAllTableCells();

        const tableWrappers = document.querySelectorAll(".manager-panel, .reception-panel, .account-panel, .tab-content, .tab-content-panel");

        tableWrappers.forEach(panel => {
            const table = panel.querySelector("table.activity-table, table.account-table");
            if (!table) return;

            const tbody = table.querySelector("tbody");
            if (!tbody) return;

            const allRows = Array.from(tbody.querySelectorAll("tr")).filter(r => !r.hasAttribute("th:if") && !r.querySelector("td[colspan]"));
            if (allRows.length === 0) return;

            let filteredRows = [...allRows];
            const pageSizeAttr = table.getAttribute("data-page-size");
            const itemsPerPage = pageSizeAttr ? parseInt(pageSizeAttr, 10) : 5;
            let currentPage = 1;
            let currentKeyword = "";
            let currentStatus = "ALL";

            const summaryText = panel.querySelector(".table-footer p");
            const paginationContainer = panel.querySelector(".table-footer .pagination");
            const searchInput = panel.querySelector(".filter-bar .search-box input, .search-box input");
            const dropdown = panel.querySelector(".filter-bar .filter-dropdown, .filter-dropdown");

            function applyFilters() {
                filteredRows = allRows.filter(row => {
                    const textContent = row.textContent.toLowerCase();
                    const matchesSearch = !currentKeyword || textContent.includes(currentKeyword);

                    const rowStatus = (row.getAttribute("data-status") || "").toUpperCase();
                    const statusCellText = (row.querySelector(".status, .badge, .status-badge")?.textContent || "").toUpperCase();
                    
                    let matchesStatus = true;
                    if (currentStatus !== "ALL") {
                        matchesStatus = rowStatus.includes(currentStatus) || statusCellText.includes(currentStatus);
                    }

                    return matchesSearch && matchesStatus;
                });
                renderTablePage(1);
            }

            function renderTablePage(page) {
                const totalItems = filteredRows.length;
                const totalPages = Math.ceil(totalItems / itemsPerPage) || 1;

                if (page > totalPages) page = totalPages;
                if (page < 1) page = 1;
                currentPage = page;

                const start = (currentPage - 1) * itemsPerPage;
                const end = start + itemsPerPage;

                // Hide all rows first, show filtered + paginated slice
                allRows.forEach(r => r.style.display = "none");
                filteredRows.forEach((row, idx) => {
                    if (idx >= start && idx < end) {
                        row.style.display = "";
                    } else {
                        row.style.display = "none";
                    }
                });

                formatAllTableCells();

                // Update footer summary text
                if (summaryText) {
                    const currentShowingEnd = Math.min(end, totalItems);
                    const currentShowingStart = totalItems > 0 ? start + 1 : 0;
                    summaryText.innerHTML = `Hiển thị <strong>${currentShowingStart}-${currentShowingEnd}</strong> trong số <strong>${totalItems}</strong> kết quả`;
                }

                // Render pagination buttons
                if (paginationContainer) {
                    paginationContainer.innerHTML = "";

                    if (totalPages <= 1) return;

                    // Prev button
                    const prevBtn = document.createElement("button");
                    prevBtn.type = "button";
                    prevBtn.className = `page-btn ${currentPage === 1 ? "disabled" : ""}`;
                    prevBtn.innerHTML = '<i class="fa-solid fa-chevron-left"></i>';
                    prevBtn.disabled = currentPage === 1;
                    prevBtn.addEventListener("click", () => {
                        if (currentPage > 1) renderTablePage(currentPage - 1);
                    });
                    paginationContainer.appendChild(prevBtn);

                    // Number buttons
                    for (let i = 1; i <= totalPages; i++) {
                        const numBtn = document.createElement("button");
                        numBtn.type = "button";
                        numBtn.className = `page-btn ${i === currentPage ? "active" : ""}`;
                        numBtn.textContent = i;
                        numBtn.addEventListener("click", () => renderTablePage(i));
                        paginationContainer.appendChild(numBtn);
                    }

                    // Next button
                    const nextBtn = document.createElement("button");
                    nextBtn.type = "button";
                    nextBtn.className = `page-btn ${currentPage === totalPages ? "disabled" : ""}`;
                    nextBtn.innerHTML = '<i class="fa-solid fa-chevron-right"></i>';
                    nextBtn.disabled = currentPage === totalPages;
                    nextBtn.addEventListener("click", () => {
                        if (currentPage < totalPages) renderTablePage(currentPage + 1);
                    });
                    paginationContainer.appendChild(nextBtn);
                }
            }

            // Search Input listener
            if (searchInput && !searchInput.dataset.paginated) {
                searchInput.dataset.paginated = "true";
                searchInput.addEventListener("input", function () {
                    currentKeyword = this.value.toLowerCase().trim();
                    applyFilters();
                });
            }

            // Dropdown Status Filter listener
            if (dropdown && !dropdown.dataset.paginated) {
                dropdown.dataset.paginated = "true";
                const filterBtn = dropdown.querySelector(".filter-btn");
                const menu = dropdown.querySelector(".filter-menu");
                const label = dropdown.querySelector(".filter-label strong");

                if (filterBtn) {
                    filterBtn.addEventListener("click", function (e) {
                        e.stopPropagation();
                        dropdown.classList.toggle("active");
                    });
                }

                if (menu) {
                    menu.querySelectorAll("button").forEach(btn => {
                        btn.addEventListener("click", function () {
                            const text = this.textContent.trim();
                            const statusVal = this.getAttribute("data-status") || text.toUpperCase();
                            if (label) label.textContent = text;
                            dropdown.classList.remove("active");

                            if (statusVal.includes("ALL")) {
                                currentStatus = "ALL";
                            } else if (statusVal.includes("PENDING")) {
                                currentStatus = "PENDING";
                            } else if (statusVal.includes("CLEANING")) {
                                currentStatus = "CLEANING";
                            } else if (statusVal.includes("MAINTENANCE")) {
                                currentStatus = "MAINTENANCE";
                            } else if (statusVal.includes("COMPLETED") || statusVal.includes("INSPECTED") || statusVal.includes("APPROVED")) {
                                currentStatus = "INSPECTED";
                            } else if (statusVal.includes("AVAILABLE")) {
                                currentStatus = "AVAILABLE";
                            } else if (statusVal.includes("OCCUPIED")) {
                                currentStatus = "OCCUPIED";
                            } else {
                                currentStatus = statusVal;
                            }
                            applyFilters();
                        });
                    });
                }

                document.addEventListener("click", function () {
                    dropdown.classList.remove("active");
                });
            }

            renderTablePage(1);
        });
    };

    window.initTablePagination();
});
