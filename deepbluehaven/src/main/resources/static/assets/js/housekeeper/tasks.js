(function () {
    // Housekeeper tasks event listeners
    document.addEventListener("DOMContentLoaded", function () {
        // Dropdown status filters if any
        const statusFilterBtns = document.querySelectorAll(".filter-menu button");
        statusFilterBtns.forEach(btn => {
            btn.addEventListener("click", function () {
                const label = this.textContent.trim();
                const filterLabel = document.querySelector(".filter-label strong");
                if (filterLabel) filterLabel.textContent = label;
                const dropdown = this.closest(".filter-dropdown");
                if (dropdown) dropdown.classList.remove("active");
            });
        });
    });
})();
