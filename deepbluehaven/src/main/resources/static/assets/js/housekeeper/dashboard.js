(function () {
    // Housekeeper dashboard event listeners
    document.addEventListener("DOMContentLoaded", function () {
        const btnRefresh = document.querySelector(".panel-header button");
        if (btnRefresh) {
            btnRefresh.addEventListener("click", function () {
                window.location.reload();
            });
        }
    });
})();
