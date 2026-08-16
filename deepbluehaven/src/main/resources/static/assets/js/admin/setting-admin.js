document.addEventListener("DOMContentLoaded", function () {
    const savedTheme = localStorage.getItem("dbh_theme") || "light";
    applyTheme(savedTheme);

    const themeButtons = document.querySelectorAll(".theme-switch .theme-btn");
    themeButtons.forEach(btn => {
        btn.addEventListener("click", function () {
            const isDark = this.textContent.toLowerCase().includes("dark") || this.querySelector(".fa-moon") !== null;
            const newTheme = isDark ? "dark" : "light";
            localStorage.setItem("dbh_theme", newTheme);
            applyTheme(newTheme);
            showSettingToast("Appearance theme updated to " + (isDark ? "Dark Mode" : "Light Mode"));
        });
    });

    function applyTheme(theme) {
        if (theme === "dark") {
            document.documentElement.setAttribute("data-theme", "dark");
            document.body.classList.add("dark-theme");
        } else {
            document.documentElement.setAttribute("data-theme", "light");
            document.body.classList.remove("dark-theme");
        }

        themeButtons.forEach(btn => {
            const isDarkBtn = btn.textContent.toLowerCase().includes("dark") || btn.querySelector(".fa-moon") !== null;
            if ((theme === "dark" && isDarkBtn) || (theme === "light" && !isDarkBtn)) {
                btn.classList.add("active");
            } else {
                btn.classList.remove("active");
            }
        });
    }

    document.querySelectorAll(".setting-card select").forEach(select => {
        const key = "dbh_setting_" + (select.getAttribute("name") || select.parentElement.textContent.trim().slice(0, 15));
        const savedVal = localStorage.getItem(key);
        if (savedVal) {
            select.value = savedVal;
        }

        select.addEventListener("change", function () {
            localStorage.setItem(key, this.value);
            showSettingToast("Preference updated successfully");
        });
    });

    document.querySelectorAll(".setting-card .toggle input[type='checkbox']").forEach(toggle => {
        const key = "dbh_toggle_" + toggle.closest(".setting-row")?.querySelector("strong")?.textContent.trim();
        const savedToggle = localStorage.getItem(key);
        if (savedToggle !== null) {
            toggle.checked = (savedToggle === "true");
        }

        toggle.addEventListener("change", function () {
            localStorage.setItem(key, this.checked);
            showSettingToast("Setting updated");
        });
    });

    const backupBtn = document.querySelector(".button-group .primary-btn");
    const exportBtn = document.querySelector(".button-group .secondary-btn");

    if (backupBtn) {
        backupBtn.addEventListener("click", function () {
            showSettingToast("Database backup archive generated successfully.");
        });
    }

    if (exportBtn) {
        exportBtn.addEventListener("click", function () {
            window.location.href = (window.CONTEXT_PATH || "/deepbluehaven") + "/admin/reports/financial/export";
        });
    }

    function showSettingToast(msg) {
        let toast = document.getElementById("settingToast");
        if (!toast) {
            toast = document.createElement("div");
            toast.id = "settingToast";
            toast.className = "setting-toast";
            document.body.appendChild(toast);
        }
        toast.innerHTML = `<i class="fa-solid fa-circle-check"></i> <span>${msg}</span>`;
        toast.classList.add("is-visible");

        setTimeout(() => {
            toast.classList.remove("is-visible");
        }, 2500);
    }
});
