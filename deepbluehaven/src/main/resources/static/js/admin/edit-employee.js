document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("editEmployeeForm");

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

    const rolePreviewTitle = document.getElementById("rolePreviewTitle");
    const rolePreviewBadge = document.getElementById("rolePreviewBadge");
    const rolePreviewDescription = document.getElementById("rolePreviewDescription");
    const rolePreviewPermissions = document.getElementById("rolePreviewPermissions");

    const changePasswordToggle = document.getElementById("changePassword");
    const passwordFieldsContainer = document.getElementById("passwordFieldsContainer");

    let currentStep = 1;
    const totalSteps = 3;

    const roleConfig = {
        HOUSEKEEPER: {
            department: "HOUSEKEEPING",
            level: "1",
            badge: "Housekeeper",
            description: "Housekeeper can view assigned tasks, update cleaning progress and change room cleaning status.",
            preview: ["View own tasks", "Update task status", "Update room status"],
            permissions: [
                "VIEW_TASK",
                "UPDATE_TASK_STATUS"
            ]
        },

        RECEPTIONIST: {
            department: "RECEPTION",
            level: "2",
            badge: "Receptionist",
            description: "Receptionist can manage booking workflow, customer check-in/check-out and invoice viewing.",
            preview: ["Booking workflow", "Check-in/out", "View invoices"],
            permissions: [
                "VIEW_BOOKING",
                "CREATE_BOOKING",
                "CANCEL_BOOKING",
                "CHECK_IN",
                "CHECK_OUT",
                "ASSIGN_ROOM",
                "CREATE_SERVICE_ORDER",
                "PROCESS_SERVICE_ORDER",
                "CREATE_INVOICE",
                "PROCESS_PAYMENT",
                "APPLY_DISCOUNT",
                "VIEW_TASK",
                "UPDATE_TASK_STATUS",
                "MANAGE_ROOM",
                "MANAGE_SERVICE",
                "MANAGE_INVENTORY",
                "MANAGE_DISCOUNT",
                "MANAGE_PRICING",
                "MANAGE_MEMBERSHIP",
                "VIEW_REPORT",
                "MANAGE_COMMENT"
            ]
        },

        MANAGER: {
            department: "MANAGEMENT",
            level: "3",
            badge: "Manager",
            description: "Manager can supervise operations, assign tasks, update rooms and view/export reports.",
            preview: ["Assign tasks", "View reports", "Manage operations"],
            permissions: [
                "VIEW_BOOKING",
                "CREATE_BOOKING",
                "CANCEL_BOOKING",
                "CHECK_IN",
                "CHECK_OUT",
                "ASSIGN_ROOM",
                "CREATE_SERVICE_ORDER",
                "PROCESS_SERVICE_ORDER",
                "CREATE_INVOICE",
                "PROCESS_PAYMENT",
                "APPLY_DISCOUNT",
                "VIEW_TASK",
                "UPDATE_TASK_STATUS",
                "MANAGE_ROOM",
                "MANAGE_SERVICE",
                "MANAGE_INVENTORY",
                "MANAGE_DISCOUNT",
                "MANAGE_PRICING",
                "MANAGE_MEMBERSHIP",
                "VIEW_REPORT",
                "MANAGE_COMMENT"
            ]
        },

        ADMIN: {
            department: "ADMINISTRATION",
            level: "4",
            badge: "Admin",
            description: "Admin can manage employee accounts, permission levels and account security settings.",
            preview: ["Manage accounts", "Assign permissions", "Lock/unlock access"],
            permissions: [
                "VIEW_BOOKING",
                "CREATE_BOOKING",
                "CANCEL_BOOKING",
                "CHECK_IN",
                "CHECK_OUT",
                "ASSIGN_ROOM",
                "CREATE_SERVICE_ORDER",
                "PROCESS_SERVICE_ORDER",
                "CREATE_INVOICE",
                "PROCESS_PAYMENT",
                "APPLY_DISCOUNT",
                "VIEW_TASK",
                "UPDATE_TASK_STATUS",
                "MANAGE_WORKER",
                "MANAGE_ROOM",
                "MANAGE_SERVICE",
                "MANAGE_INVENTORY",
                "MANAGE_DISCOUNT",
                "MANAGE_PRICING",
                "MANAGE_MEMBERSHIP",
                "VIEW_REPORT",
                "MANAGE_COMMENT"
            ]
        }
    };

    const formatEnumValue = (value) => {
        if (!value) {
            return "-";
        }

        return value
            .toString()
            .replaceAll("_", " ")
            .toLowerCase()
            .replace(/\b\w/g, (char) => char.toUpperCase());
    };

    const escapeHtml = (value) => value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");

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
                    label.innerHTML = `<strong>${formatEnumValue(value)}</strong>`;
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
        
        // Restore values from inputs on load
        dropdowns.forEach((dropdown) => {
            const inputId = dropdown.dataset.input;
            if (inputId) {
                const input = document.getElementById(inputId);
                if (input && input.value) {
                    setDropdownValue(inputId, input.value, false);
                }
            }
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

    const getFieldLabel = (field) => {
        const group = field?.closest(".form-group");
        const label = group?.querySelector("label");

        if (!label) {
            return "Field";
        }

        return label.textContent.replace("*", "").trim();
    };

    const showErrorToast = (messages) => {
        if (typeof showToast !== "function" || !messages.length) {
            return;
        }

        const uniqueMessages = [...new Set(messages)].slice(0, 4);
        const content = uniqueMessages
            .map((message) => `• ${escapeHtml(message)}`)
            .join("<br>");

        showToast("error", "Please check the form", content, { duration: 6000 });
    };

    const collectCurrentStepErrors = () => {
        const panel = getCurrentPanel();
        const messages = [];

        panel?.querySelectorAll(".form-group.has-error").forEach((group) => {
            const field = group.querySelector("input, select, textarea");
            const message = group.querySelector(".field-error")?.textContent?.trim();

            if (message) {
                messages.push(`${getFieldLabel(field)}: ${message}`);
            }
        });

        if (currentStep === 3 && permissionError?.textContent.trim()) {
            messages.push(`Permissions: ${permissionError.textContent.trim()}`);
        }

        return messages;
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
        if (!changePasswordToggle || !changePasswordToggle.checked) {
            return true;
        }

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
        if (currentStep !== 3) {
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

    const validateCurrentStep = (notify = true) => {
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

        const isValid = checks.every(Boolean);

        if (!isValid && notify) {
            showErrorToast(collectCurrentStepErrors());
        }

        return isValid;
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

        rolePreviewTitle.textContent = formatEnumValue(role);
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
    /* PASSWORD TOGGLE */
    /* ============================= */

    const togglePasswordVisibility = (btn) => {
        const targetId = btn.dataset.togglePassword;
        const input = document.getElementById(targetId);
        const icon = btn.querySelector("i");

        if (!input || !icon) return;

        if (input.type === "password") {
            input.type = "text";
            icon.classList.replace("fa-eye", "fa-eye-slash");
        } else {
            input.type = "password";
            icon.classList.replace("fa-eye-slash", "fa-eye");
        }
    };

    document.querySelectorAll("[data-toggle-password]").forEach((btn) => {
        btn.addEventListener("click", () => togglePasswordVisibility(btn));
    });

    if (changePasswordToggle && passwordFieldsContainer) {
        const updatePasswordFieldVisibility = () => {
            if (changePasswordToggle.checked) {
                passwordFieldsContainer.style.display = "block";
                // Add required data attr when visible
                passwordFieldsContainer.querySelectorAll("input[type='password']").forEach(i => i.setAttribute("data-required", "true"));
            } else {
                passwordFieldsContainer.style.display = "none";
                // Remove required data attr when hidden and clear error
                passwordFieldsContainer.querySelectorAll("input[type='password']").forEach(i => {
                    i.removeAttribute("data-required");
                    clearFieldError(i);
                });
            }
        };

        changePasswordToggle.addEventListener("change", updatePasswordFieldVisibility);
        // Initial setup
        updatePasswordFieldVisibility();
    }

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
        });
    });

    form.addEventListener("submit", (e) => {
        const isValid = validateCurrentStep();
        if (!isValid) {
            e.preventDefault();
        }
    });

    const init = () => {
        initFilterDropdowns();
        showStep(1);
        updateRolePreview();

        const serverErrors = document.querySelectorAll(".server-validation-error");
        if (serverErrors.length > 0) {
            const messages = Array.from(serverErrors).map(el => `${el.dataset.field}: ${el.dataset.message}`);
            showErrorToast(messages);

            serverErrors.forEach((el) => {
                const field = document.querySelector(`[name="${el.dataset.field}"]`);
                if (field) {
                    setFieldError(field, el.dataset.message);
                }
            });
        }
    };

    init();
});
