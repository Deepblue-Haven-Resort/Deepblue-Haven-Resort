document.addEventListener("DOMContentLoaded", function () {
    const editButton = document.getElementById("editProfileBtn");
    const cancelButton = document.getElementById("cancelEditBtn");
    const profileForm = document.getElementById("profileForm");
    const formActions = document.getElementById("profileFormActions");

    if (!editButton || !profileForm) {
        return;
    }

    const inputs = profileForm.querySelectorAll("input");
    const selects = profileForm.querySelectorAll("select");

    const initialValues = {};

    inputs.forEach(function (input) {
        initialValues[input.id] = input.value;
    });

    selects.forEach(function (select) {
        initialValues[select.id] = select.value;
    });

    function enableEditing() {
        inputs.forEach(function (input) {
            input.removeAttribute("readonly");
        });

        selects.forEach(function (select) {
            select.removeAttribute("disabled");
        });

        editButton.style.display = "none";
        formActions.classList.add("is-visible");
    }

    function disableEditing() {
        inputs.forEach(function (input) {
            input.setAttribute("readonly", true);
        });

        selects.forEach(function (select) {
            select.setAttribute("disabled", true);
        });

        editButton.style.display = "inline-flex";
        formActions.classList.remove("is-visible");
    }

    function restoreInitialValues() {
        inputs.forEach(function (input) {
            input.value = initialValues[input.id];
        });

        selects.forEach(function (select) {
            select.value = initialValues[select.id];
        });
    }

    editButton.addEventListener("click", function () {
        enableEditing();
    });

    cancelButton.addEventListener("click", function () {
        restoreInitialValues();
        disableEditing();
    });

    profileForm.addEventListener("submit", function (event) {
        event.preventDefault();

        inputs.forEach(function (input) {
            initialValues[input.id] = input.value;
        });

        selects.forEach(function (select) {
            initialValues[select.id] = select.value;
        });

        disableEditing();

        alert("Profile information updated successfully.");
    });
});