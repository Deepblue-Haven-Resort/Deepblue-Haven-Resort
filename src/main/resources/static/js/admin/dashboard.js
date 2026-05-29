const dropdowns = document.querySelectorAll(".filter-dropdown");

dropdowns.forEach((dropdown) => {
    const btn = dropdown.querySelector(".filter-btn");
    const label = dropdown.querySelector(".filter-label");
    const items = dropdown.querySelectorAll(".filter-menu button");

    btn.addEventListener("click", (e) => {
        e.stopPropagation();

        dropdowns.forEach((item) => {
            if (item !== dropdown) {
                item.classList.remove("active");
            }
        });

        dropdown.classList.toggle("active");
    });

    items.forEach((item) => {
        item.addEventListener("click", (e) => {
            e.stopPropagation();

            if (label) {
                label.textContent = item.textContent.trim();
            }

            dropdown.classList.remove("active");
        });
    });
});

document.addEventListener("click", () => {
    dropdowns.forEach((dropdown) => {
        dropdown.classList.remove("active");
    });
});