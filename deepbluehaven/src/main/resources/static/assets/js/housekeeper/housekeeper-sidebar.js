const sidebar = document.getElementById("housekeeperSidebar");
const toggle = document.getElementById("sidebarToggle");

if (sidebar && toggle) {
    toggle.addEventListener("click", function () {
        sidebar.classList.toggle("is-collapsed");
    });
}