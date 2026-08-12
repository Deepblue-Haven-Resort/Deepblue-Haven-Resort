// table-pagination.js
document.addEventListener("DOMContentLoaded", function () {
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
            'STANDARD': 'Standard Room'
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
            td.querySelectorAll(".status, .activity-badge, .badge, .status-badge, .badge-status, small, div.filter-options > span").forEach(el => {
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

    const tableWrappers = document.querySelectorAll(".manager-panel, .reception-panel, .account-panel");

    tableWrappers.forEach(panel => {
        const table = panel.querySelector("table.activity-table, table.account-table");
        if (!table) return;

        const tbody = table.querySelector("tbody");
        if (!tbody) return;

        const allRows = Array.from(tbody.querySelectorAll("tr")).filter(r => !r.hasAttribute("th:if") && !r.querySelector("td[colspan]"));
        if (allRows.length === 0) return;

        const itemsPerPage = 8;
        let currentPage = 1;
        const totalPages = Math.ceil(allRows.length / itemsPerPage);

        const summaryText = panel.querySelector(".table-footer p");
        const paginationContainer = panel.querySelector(".table-footer .pagination");

        function renderTablePage(page) {
            currentPage = page;
            const start = (page - 1) * itemsPerPage;
            const end = start + itemsPerPage;

            allRows.forEach((row, index) => {
                if (index >= start && index < end) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });

            formatAllTableCells();

            // Update footer summary text
            if (summaryText) {
                const currentShowingEnd = Math.min(end, allRows.length);
                const currentShowingStart = allRows.length > 0 ? start + 1 : 0;
                summaryText.innerHTML = `Hiển thị <strong>${currentShowingStart}-${currentShowingEnd}</strong> trong số <strong>${allRows.length}</strong> kết quả`;
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

        renderTablePage(1);
    });
});
