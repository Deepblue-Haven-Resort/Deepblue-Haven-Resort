document.addEventListener("DOMContentLoaded", function () {
    const editButton = document.getElementById("editProfileBtn");
    const cancelButton = document.getElementById("cancelEditBtn");
    const profileForm = document.getElementById("profileForm");
    const formActions = document.getElementById("profileFormActions");

    function getApiUrl(path) {
        let ctx = document.querySelector('meta[name="_context_path"]')?.content;
        if (!ctx || ctx === "/") {
            const match = window.location.pathname.match(/^\/([^\/]+)/);
            ctx = (match && match[1] === "deepbluehaven") ? "/" + match[1] : "";
        }
        return ctx.replace(/\/$/, "") + (path.startsWith("/") ? path : "/" + path);
    }

    if (!editButton || !profileForm) {
        return;
    }

    const inputs = profileForm.querySelectorAll("input");

    const initialValues = {};
    inputs.forEach(function (input) {
        initialValues[input.id] = input.value;
    });

    let birthdayPicker = null;
    if (window.flatpickr) {
        birthdayPicker = flatpickr("#birthday", {
            dateFormat: "Y-m-d",
            clickOpens: false,
            allowInput: true
        });
    }

    function enableEditing() {
        inputs.forEach(function (input) {
            input.removeAttribute("readonly");
        });
        if (birthdayPicker) birthdayPicker.set("clickOpens", true);
        editButton.style.display = "none";
        formActions.classList.add("is-visible");
    }

    function disableEditing() {
        inputs.forEach(function (input) {
            input.setAttribute("readonly", true);
        });
        if (birthdayPicker) birthdayPicker.set("clickOpens", false);
        editButton.style.display = "inline-flex";
        formActions.classList.remove("is-visible");
    }

    function restoreInitialValues() {
        inputs.forEach(function (input) {
            input.value = initialValues[input.id];
        });
        if (birthdayPicker) {
            birthdayPicker.setDate(initialValues["birthday"]);
        }
    }

    editButton.addEventListener("click", function () {
        enableEditing();
        if (typeof showToast === "function") {
            showToast("info", "Edit Mode", "You can now edit your profile information.");
        }
    });

    cancelButton.addEventListener("click", function () {
        restoreInitialValues();
        disableEditing();
        if (typeof showToast === "function") {
            showToast("warning", "Cancelled", "Profile editing cancelled.");
        }
    });

    profileForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const fullName = document.getElementById("fullName")?.value || "";
        const email = document.getElementById("email")?.value || "";
        const phoneNumber = document.getElementById("phone")?.value || "";
        const birthDay = document.getElementById("birthday")?.value || null;

        const payload = {
            fullName: fullName,
            email: email,
            phoneNumber: phoneNumber,
            birthDay: birthDay
        };

        try {
            const response = await fetch(getApiUrl("/api/profile/update"), {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (data.success) {
                if (typeof showToast === "function") {
                    showToast("success", "Success", "Profile updated successfully!");
                } else {
                    alert("Profile updated successfully!");
                }

                inputs.forEach(function (input) {
                    initialValues[input.id] = input.value;
                });

                const sidebarFullName = document.getElementById("sidebarFullName");
                const sidebarEmail = document.getElementById("sidebarEmail");
                if (sidebarFullName) sidebarFullName.textContent = fullName;
                if (sidebarEmail) sidebarEmail.textContent = email;

                disableEditing();
            } else {
                if (typeof showToast === "function") {
                    showToast("error", "Failed", data.message || "Profile update failed.");
                } else {
                    alert(data.message || "Profile update failed.");
                }
            }
        } catch (err) {
            if (typeof showToast === "function") {
                showToast("error", "Error", "An error occurred while updating profile.");
            } else {
                alert("An error occurred while updating profile.");
            }
        }
    });
});