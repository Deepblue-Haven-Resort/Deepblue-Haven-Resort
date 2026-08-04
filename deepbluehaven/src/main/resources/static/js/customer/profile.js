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

    function enableEditing() {
        inputs.forEach(function (input) {
            input.removeAttribute("readonly");
        });
        editButton.style.display = "none";
        formActions.classList.add("is-visible");
    }

    function disableEditing() {
        inputs.forEach(function (input) {
            input.setAttribute("readonly", true);
        });
        editButton.style.display = "inline-flex";
        formActions.classList.remove("is-visible");
    }

    function restoreInitialValues() {
        inputs.forEach(function (input) {
            input.value = initialValues[input.id];
        });
    }

    editButton.addEventListener("click", function () {
        enableEditing();
    });

    cancelButton.addEventListener("click", function () {
        restoreInitialValues();
        disableEditing();
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
                alert("Cập nhật thông tin cá nhân thành công!");

                inputs.forEach(function (input) {
                    initialValues[input.id] = input.value;
                });

                const sidebarFullName = document.getElementById("sidebarFullName");
                const sidebarEmail = document.getElementById("sidebarEmail");
                if (sidebarFullName) sidebarFullName.textContent = fullName;
                if (sidebarEmail) sidebarEmail.textContent = email;

                disableEditing();
            } else {
                alert(data.message || "Cập nhật không thành công.");
            }
        } catch (err) {
            alert("Đã xảy ra lỗi khi gửi thông tin cập nhật.");
        }
    });
});