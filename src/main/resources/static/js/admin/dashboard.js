const ratingButtons = document.querySelectorAll(".rating-row button");

ratingButtons.forEach((button) => {
    button.addEventListener("click", () => {
        ratingButtons.forEach((btn) => {
            btn.style.transform = "scale(1)";
        });

        button.style.transform = "scale(1.16)";
    });
});