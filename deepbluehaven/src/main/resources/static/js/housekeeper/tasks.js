(function () {
    function initPagination(tableSelector, rowSelector, itemsPerPage) {
        const tableRows = Array.from(document.querySelectorAll(rowSelector));
        const paginationContainer = document.querySelector(".pagination");
        const summaryText = document.querySelector(".table-footer p strong");

        if (!tableRows.length) return;

        let filteredRows = [...tableRows];
        let currentPage = 1;

        function updateTable() {
            const totalItems = filteredRows.length;
            const totalPages = Math.ceil(totalItems / itemsPerPage) || 1;

            if (currentPage > totalPages) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            const startIndex = (currentPage - 1) * itemsPerPage;
            const endIndex = startIndex + itemsPerPage;

            tableRows.forEach(row => row.style.display = "none");
            filteredRows.slice(startIndex, endIndex).forEach(row => row.style.display = "");

            if (summaryText) {
                const summaryParent = summaryText.parentNode;
                const from = totalItems === 0 ? 0 : startIndex + 1;
                const to = Math.min(endIndex, totalItems);
                
                // Trực quan hóa phần text tiếng Việt tương tự Admin
                if (summaryParent.textContent.includes("hoàn thành")) {
                    summaryParent.innerHTML = `Hiển thị <strong>${from}-${to}</strong> trong số <strong>${totalItems}</strong> công việc hoàn thành`;
                } else {
                    summaryParent.innerHTML = `Hiển thị <strong>${from}-${to}</strong> trong số <strong>${totalItems}</strong> công việc`;
                }
            }

            if (paginationContainer) {
                paginationContainer.innerHTML = "";

                // Prev
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

                // Numbers
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

                // Next
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

        // Search Input
        const searchInput = document.querySelector(".filter-bar .search-box input");
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

        updateTable();
    }

    document.addEventListener("DOMContentLoaded", function () {
        initPagination(".account-table", ".account-table tbody tr", 5);
    });
})();
