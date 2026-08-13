(function () {
    function initDashboard() {
        const searchInput = document.querySelector(".filter-bar .search-box input");
        const tableRows = Array.from(document.querySelectorAll(".account-table tbody tr"));
        const paginationContainer = document.querySelector(".pagination");
        const summaryText = document.querySelector(".table-footer p strong");

        if (!tableRows.length) return;

        let filteredRows = [...tableRows];
        let currentPage = 1;
        const pageSize = 5;

        function updateTable() {
            const totalItems = filteredRows.length;
            const totalPages = Math.ceil(totalItems / pageSize) || 1;

            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            // Show/Hide rows based on current page
            const startIndex = (currentPage - 1) * pageSize;
            const endIndex = startIndex + pageSize;

            tableRows.forEach(row => row.style.display = "none");
            filteredRows.slice(startIndex, endIndex).forEach(row => row.style.display = "");

            // Update footer summary text
            if (summaryText) {
                const summaryParent = summaryText.parentNode;
                const from = totalItems === 0 ? 0 : startIndex + 1;
                const to = Math.min(endIndex, totalItems);
                summaryParent.innerHTML = `Hiển thị <strong>${from}-${to}</strong> trong số <strong>${totalItems}</strong> công việc`;
            }

            // Render Pagination Buttons
            if (paginationContainer) {
                paginationContainer.innerHTML = "";

                // Prev Button
                const prevBtn = document.createElement("button");
                prevBtn.type = "button";
                prevBtn.className = `page-btn${currentPage === 1 ? " disabled" : ""}`;
                prevBtn.innerHTML = `<i class="fa-solid fa-chevron-left"></i>`;
                prevBtn.addEventListener("click", () => {
                    if (currentPage > 1) {
                        currentPage--;
                        updateTable();
                    }
                });
                paginationContainer.appendChild(prevBtn);

                // Page Number Buttons
                for (let i = 1; i <= totalPages; i++) {
                    const pageBtn = document.createElement("button");
                    pageBtn.type = "button";
                    pageBtn.className = `page-btn${i === currentPage ? " active" : ""}`;
                    pageBtn.textContent = i;
                    pageBtn.addEventListener("click", () => {
                        currentPage = i;
                        updateTable();
                    });
                    paginationContainer.appendChild(pageBtn);
                }

                // Next Button
                const nextBtn = document.createElement("button");
                nextBtn.type = "button";
                nextBtn.className = `page-btn${currentPage === totalPages ? " disabled" : ""}`;
                nextBtn.innerHTML = `<i class="fa-solid fa-chevron-right"></i>`;
                nextBtn.addEventListener("click", () => {
                    if (currentPage < totalPages) {
                        currentPage++;
                        updateTable();
                    }
                });
                paginationContainer.appendChild(nextBtn);
            }
        }

        // Search Filter Integration
        if (searchInput) {
            searchInput.addEventListener("input", function () {
                const keyword = this.value.toLowerCase().trim();
                filteredRows = tableRows.filter(row => {
                    return row.textContent.toLowerCase().includes(keyword);
                });
                currentPage = 1;
                updateTable();
            });
        }

        const btnRefresh = document.querySelector(".panel-header button");
        if (btnRefresh) {
            btnRefresh.addEventListener("click", function () {
                window.location.reload();
            });
        }

        // Initialize First Render
        updateTable();
    }

    document.addEventListener("DOMContentLoaded", initDashboard);
})();
