document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("createEmployeeForm");

    if (!form) {
        return;
    }

    const panels = document.querySelectorAll("[data-step-panel]");
    const stepButtons = document.querySelectorAll("[data-step-button]");
    const prevBtn = document.getElementById("prevBtn");
    const nextBtn = document.getElementById("nextBtn");
    const submitBtn = document.getElementById("submitBtn");

    const roleSelect = document.getElementById("role");
    const departmentSelect = document.getElementById("department");
    const permissionLevelSelect = document.getElementById("permissionLevel");

    const useDefaultPermissions = document.getElementById("useDefaultPermissions");
    const resetPermissionBtn = document.getElementById("resetPermissionBtn");
    const permissionError = document.getElementById("permissionError");
    const confirmCreate = document.getElementById("confirmCreate");
    const confirmError = document.getElementById("confirmError");

    const rolePreviewTitle = document.getElementById("rolePreviewTitle");
    const rolePreviewBadge = document.getElementById("rolePreviewBadge");
    const rolePreviewDescription = document.getElementById("rolePreviewDescription");
    const rolePreviewPermissions = document.getElementById("rolePreviewPermissions");

    let currentStep = 1;
    const totalSteps = 5;

    const roleConfig = {
        HOUSEKEEPER: {
            department: "HOUSEKEEPING",
            level: "STAFF",
            badge: "Housekeeper",
            description: "Housekeeper can view assigned tasks, update cleaning progress and change room cleaning status.",
            preview: ["View own tasks", "Update task status", "Update room status"],
            permissions: [
                "DASHBOARD_VIEW",
                "ROOM_VIEW",
                "ROOM_STATUS_UPDATE",
                "TASK_VIEW",
                "TASK_UPDATE"
            ]
        },

        RECEPTIONIST: {
            department: "RECEPTION",
            level: "STAFF",
            badge: "Receptionist",
            description: "Receptionist can manage booking workflow, customer check-in/check-out and invoice viewing.",
            preview: ["Booking workflow", "Check-in/out", "View invoices"],
            permissions: [
                "DASHBOARD_VIEW",
                "ROOM_VIEW",
                "BOOKING_VIEW",
                "BOOKING_CREATE",
                "BOOKING_UPDATE",
                "CHECKIN_CHECKOUT",
                "INVOICE_VIEW"
            ]
        },

        MANAGER: {
            department: "MANAGEMENT",
            level: "MANAGER",
            badge: "Manager",
            description: "Manager can supervise operations, assign tasks, update rooms and view/export reports.",
            preview: ["Assign tasks", "View reports", "Manage operations"],
            permissions: [
                "DASHBOARD_VIEW",
                "ROOM_VIEW",
                "ROOM_UPDATE",
                "ROOM_STATUS_UPDATE",
                "BOOKING_VIEW",
                "BOOKING_UPDATE",
                "TASK_VIEW",
                "TASK_CREATE",
                "TASK_ASSIGN",
                "TASK_UPDATE",
                "TASK_INSPECT",
                "INVOICE_VIEW",
                "REPORT_VIEW",
                "REPORT_EXPORT"
            ]
        },

        ADMIN: {
            department: "ADMINISTRATION",
            level: "ADMIN",
            badge: "Admin",
            description: "Admin can manage employee accounts, permission levels and account security settings.",
            preview: ["Manage accounts", "Assign permissions", "Lock/unlock access"],
            permissions: [
                "DASHBOARD_VIEW",
                "ACCOUNT_VIEW",
                "ACCOUNT_CREATE",
                "ACCOUNT_UPDATE",
                "ACCOUNT_LOCK",
                "ROOM_VIEW",
                "ROOM_UPDATE",
                "BOOKING_VIEW",
                "TASK_VIEW",
                "INVOICE_VIEW",
                "REPORT_VIEW",
                "REPORT_EXPORT"
            ]
        }
    };

    const formatValue = (value) => {
        if (!value) {
            return "-";
        }

        return value
            .toString()
            .replaceAll("_", " ")
            .toLowerCase()
            .replace(/\b\w/g, (char) => char.toUpperCase());
    };

    /* ============================= */
    /* DROPDOWN COMPONENT SUPPORT */
    /* ============================= */

    const setDropdownValue = (inputId, value, triggerChange = true) => {
        const input = document.getElementById(inputId);

        if (!input) {
            return;
        }

        input.value = value;

        const dropdown = document.querySelector(`.filter-dropdown[data-input="${inputId}"]`);

        if (dropdown) {
            const label = dropdown.querySelector(".filter-label");
            const selectedItem = dropdown.querySelector(`.filter-menu button[data-value="${value}"]`);

            if (label) {
                if (selectedItem) {
                    label.innerHTML = selectedItem.innerHTML;
                } else {
                    label.innerHTML = `<strong>${formatValue(value)}</strong>`;
                }
            }
        }

        if (triggerChange) {
            input.dispatchEvent(new Event("change", { bubbles: true }));
        }
    };

    const initFilterDropdowns = () => {
        const dropdowns = document.querySelectorAll(".filter-dropdown");

        dropdowns.forEach((dropdown) => {
            const btn = dropdown.querySelector(".filter-btn");
            const label = dropdown.querySelector(".filter-label");
            const items = dropdown.querySelectorAll(".filter-menu button");

            if (!btn || !label) {
                return;
            }

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

                    label.innerHTML = item.innerHTML;

                    const inputId = dropdown.dataset.input;

                    if (inputId) {
                        const input = document.getElementById(inputId);

                        if (input) {
                            input.value = item.dataset.value || item.textContent.trim();
                            input.dispatchEvent(new Event("change", { bubbles: true }));
                        }
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
    };

    /* ============================= */
    /* STEP DISPLAY */
    /* ============================= */

    const showStep = (step) => {
        currentStep = step;

        panels.forEach((panel) => {
            const panelStep = Number(panel.dataset.stepPanel);
            panel.classList.toggle("active", panelStep === currentStep);
        });

        stepButtons.forEach((button) => {
            const buttonStep = Number(button.dataset.stepButton);

            button.classList.toggle("active", buttonStep === currentStep);
            button.classList.toggle("completed", buttonStep < currentStep);
        });

        if (prevBtn) {
            prevBtn.hidden = currentStep === 1;
        }

        if (nextBtn) {
            nextBtn.hidden = currentStep === totalSteps;
        }

        if (submitBtn) {
            submitBtn.hidden = currentStep !== totalSteps;
        }

        if (currentStep === totalSteps) {
            updateReview();
        }
    };

    const getCurrentPanel = () => {
        return document.querySelector(`[data-step-panel="${currentStep}"]`);
    };

    /* ============================= */
    /* VALIDATION */
    /* ============================= */

    const clearFieldError = (field) => {
        const group = field.closest(".form-group");

        if (!group) {
            return;
        }

        group.classList.remove("has-error");

        const error = group.querySelector(".field-error");

        if (error) {
            error.textContent = "";
        }
    };

    const setFieldError = (field, message) => {
        const group = field.closest(".form-group");

        if (!group) {
            return;
        }

        group.classList.add("has-error");

        const error = group.querySelector(".field-error");

        if (error) {
            error.textContent = message;
        }
    };

    const validateRequired = (panel) => {
        let isValid = true;
        const fields = panel.querySelectorAll("[data-required]");

        fields.forEach((field) => {
            clearFieldError(field);

            if (!field.value.trim()) {
                setFieldError(field, "This field is required.");
                isValid = false;
            }
        });

        return isValid;
    };

    const validateEmail = (panel) => {
        let isValid = true;
        const emailFields = panel.querySelectorAll("[data-email]");
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        emailFields.forEach((field) => {
            if (field.value.trim() && !emailRegex.test(field.value.trim())) {
                setFieldError(field, "Please enter a valid email address.");
                isValid = false;
            }
        });

        return isValid;
    };

    const validatePhone = (panel) => {
        let isValid = true;
        const phoneFields = panel.querySelectorAll("[data-phone]");
        const phoneRegex = /^[0-9+\s]{9,15}$/;

        phoneFields.forEach((field) => {
            if (field.value.trim() && !phoneRegex.test(field.value.trim())) {
                setFieldError(field, "Phone number should contain 9-15 digits.");
                isValid = false;
            }
        });

        return isValid;
    };

    const validatePassword = (panel) => {
        const password = panel.querySelector("[data-password]");
        const confirmPassword = panel.querySelector("[data-confirm-password]");

        if (!password || !confirmPassword) {
            return true;
        }

        let isValid = true;

        if (password.value.length < 8) {
            setFieldError(password, "Password must be at least 8 characters.");
            isValid = false;
        }

        if (password.value !== confirmPassword.value) {
            setFieldError(confirmPassword, "Confirm password does not match.");
            isValid = false;
        }

        return isValid;
    };

    const validateDateOfBirth = (panel) => {
        const dob = panel.querySelector("#dateOfBirth");

        if (!dob || !dob.value) {
            return true;
        }

        const selectedDate = new Date(dob.value);
        const today = new Date();

        if (selectedDate >= today) {
            setFieldError(dob, "Date of birth must be before today.");
            return false;
        }

        return true;
    };

    const validatePermissions = () => {
        if (currentStep !== 4) {
            return true;
        }

        const checkedPermissions = document.querySelectorAll('input[name="permissions"]:checked');

        if (!checkedPermissions.length) {
            if (permissionError) {
                permissionError.textContent = "Please select at least one permission.";
            }

            return false;
        }

        if (permissionError) {
            permissionError.textContent = "";
        }

        return true;
    };

    const validateCurrentStep = () => {
        const panel = getCurrentPanel();

        if (!panel) {
            return true;
        }

        const checks = [
            validateRequired(panel),
            validateEmail(panel),
            validatePhone(panel),
            validateDateOfBirth(panel),
            validatePassword(panel),
            validatePermissions()
        ];

        return checks.every(Boolean);
    };

    /* ============================= */
    /* PERMISSIONS */
    /* ============================= */

    const setPermissionChecked = (permissions) => {
        const permissionInputs = document.querySelectorAll('input[name="permissions"]');

        permissionInputs.forEach((input) => {
            input.checked = permissions.includes(input.value);
        });
    };

    const applyDefaultPermissions = () => {
        const role = roleSelect.value;

        if (!role || !roleConfig[role]) {
            setPermissionChecked([]);
            return;
        }

        setPermissionChecked(roleConfig[role].permissions);
    };

    /* ============================= */
    /* ROLE PREVIEW */
    /* ============================= */

    const updateRolePreview = () => {
        const role = roleSelect.value;

        if (!role || !roleConfig[role]) {
            rolePreviewTitle.textContent = "No role selected";
            rolePreviewBadge.textContent = "Waiting";
            rolePreviewDescription.textContent = "Select a role to preview default permissions and access scope.";

            rolePreviewPermissions.innerHTML = `
                <span>Dashboard access</span>
                <span>Module visibility</span>
                <span>Update scope</span>
            `;

            return;
        }

        const config = roleConfig[role];

        rolePreviewTitle.textContent = formatValue(role);
        rolePreviewBadge.textContent = config.badge;
        rolePreviewDescription.textContent = config.description;

        rolePreviewPermissions.innerHTML = config.preview
            .map((item) => `<span>${item}</span>`)
            .join("");
    };

    const syncRoleDefaults = () => {
        const role = roleSelect.value;

        if (!role || !roleConfig[role]) {
            updateRolePreview();
            return;
        }

        const config = roleConfig[role];

        if (!departmentSelect.value) {
            setDropdownValue("department", config.department, true);
        }

        if (!permissionLevelSelect.value) {
            setDropdownValue("permissionLevel", config.level, true);
        }

        if (useDefaultPermissions && useDefaultPermissions.checked) {
            applyDefaultPermissions();
        }

        updateRolePreview();
    };

    /* ============================= */
    /* REVIEW */
    /* ============================= */

    const updateReview = () => {
        const fields = [
            "department",
            "role",
            "permissionLevel",
            "accountStatus",
            "fullName",
            "gender",
            "dateOfBirth",
            "phone",
            "email",
            "address",
            "employeeCode",
            "username"
        ];

        fields.forEach((fieldName) => {
            const field = form.querySelector(`[name="${fieldName}"]`);
            const review = document.querySelector(`[data-review="${fieldName}"]`);

            if (!field || !review) {
                return;
            }

            review.textContent = formatValue(field.value);
        });

        const loginAccess = form.querySelector('[name="loginAccess"]');
        const locked = form.querySelector('[name="locked"]');
        const forceChangePassword = form.querySelector('[name="forceChangePassword"]');

        const loginAccessReview = document.querySelector('[data-review="loginAccess"]');
        const lockedReview = document.querySelector('[data-review="locked"]');
        const forceChangePasswordReview = document.querySelector('[data-review="forceChangePassword"]');

        if (loginAccessReview && loginAccess) {
            loginAccessReview.textContent = loginAccess.checked ? "Allowed" : "Not Allowed";
        }

        if (lockedReview && locked) {
            lockedReview.textContent = locked.checked ? "Yes" : "No";
        }

        if (forceChangePasswordReview && forceChangePassword) {
            forceChangePasswordReview.textContent = forceChangePassword.checked ? "Yes" : "No";
        }

        const selectedPermissions = [...document.querySelectorAll('input[name="permissions"]:checked')]
            .map((input) => formatValue(input.value));

        const selectedPermissionsReview = document.getElementById("selectedPermissionsReview");

        if (!selectedPermissionsReview) {
            return;
        }

        if (!selectedPermissions.length) {
            selectedPermissionsReview.textContent = "No permissions selected.";
            return;
        }

        selectedPermissionsReview.innerHTML = selectedPermissions
            .map((permission) => `<span>${permission}</span>`)
            .join("");
    };

    /* ============================= */
    /* STEP ACTIONS */
    /* ============================= */

    const goNext = () => {
        if (!validateCurrentStep()) {
            return;
        }

        if (currentStep < totalSteps) {
            showStep(currentStep + 1);
        }
    };

    const goPrev = () => {
        if (currentStep > 1) {
            showStep(currentStep - 1);
        }
    };

    stepButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const targetStep = Number(button.dataset.stepButton);

            if (targetStep <= currentStep) {
                showStep(targetStep);
                return;
            }

            let canMove = true;

            while (currentStep < targetStep && canMove) {
                canMove = validateCurrentStep();

                if (canMove) {
                    showStep(currentStep + 1);
                }
            }
        });
    });

    if (nextBtn) {
        nextBtn.addEventListener("click", goNext);
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", goPrev);
    }

    if (roleSelect) {
        roleSelect.addEventListener("change", syncRoleDefaults);
    }

    if (useDefaultPermissions) {
        useDefaultPermissions.addEventListener("change", () => {
            if (useDefaultPermissions.checked) {
                applyDefaultPermissions();
            }
        });
    }

    if (resetPermissionBtn) {
        resetPermissionBtn.addEventListener("click", () => {
            applyDefaultPermissions();
        });
    }

    document.querySelectorAll('input[name="permissions"]').forEach((input) => {
        input.addEventListener("change", () => {
            if (input.checked === false && useDefaultPermissions) {
                useDefaultPermissions.checked = false;
            }

            if (permissionError) {
                permissionError.textContent = "";
            }
        });
    });

    document.querySelectorAll("[data-required], [data-email], [data-phone], [data-password], [data-confirm-password]").forEach((field) => {
        field.addEventListener("input", () => clearFieldError(field));
        field.addEventListener("change", () => clearFieldError(field));
    });

    document.querySelectorAll("[data-toggle-password]").forEach((button) => {
        button.addEventListener("click", () => {
            const inputId = button.dataset.togglePassword;
            const input = document.getElementById(inputId);
            const icon = button.querySelector("i");

            if (!input || !icon) {
                return;
            }

            const isPassword = input.type === "password";

            input.type = isPassword ? "text" : "password";
            icon.classList.toggle("fa-eye", !isPassword);
            icon.classList.toggle("fa-eye-slash", isPassword);
        });
    });

    form.addEventListener("submit", (event) => {
        if (!validateCurrentStep()) {
            event.preventDefault();
            return;
        }

        if (confirmCreate && !confirmCreate.checked) {
            event.preventDefault();

            if (confirmError) {
                confirmError.textContent = "Please confirm the account information before creating.";
            }

            return;
        }

        if (confirmError) {
            confirmError.textContent = "";
        }
    });

    if (confirmCreate) {
        confirmCreate.addEventListener("change", () => {
            if (confirmError) {
                confirmError.textContent = "";
            }
        });
    }

    /* ============================= */
    /* INIT */
    /* ============================= */

    initFilterDropdowns();

    if (document.getElementById("accountStatus") && !document.getElementById("accountStatus").value) {
        setDropdownValue("accountStatus", "ACTIVE", false);
    }

    showStep(1);
    updateRolePreview();
});