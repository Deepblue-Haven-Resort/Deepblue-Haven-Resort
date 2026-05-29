document.addEventListener("DOMContentLoaded", () => {
    const profileTabButtons = document.querySelectorAll(".profile-tabs button");
    const activityPanel = document.getElementById("activityPanel");
    const tasksPanel = document.getElementById("tasksPanel");

    if (!profileTabButtons.length || !activityPanel || !tasksPanel) {
        return;
    }

    profileTabButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const targetId = button.dataset.target;
            const targetPanel = document.getElementById(targetId);

            if (!targetPanel) {
                return;
            }

            profileTabButtons.forEach((btn) => btn.classList.remove("active"));
            button.classList.add("active");

            activityPanel.hidden = true;
            tasksPanel.hidden = true;

            targetPanel.hidden = false;
        });
    });
});