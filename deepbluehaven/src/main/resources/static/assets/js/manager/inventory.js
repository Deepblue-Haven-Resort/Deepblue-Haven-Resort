document.addEventListener("DOMContentLoaded", () => {
    const tabBtns = document.querySelectorAll(".panel-tab-btn");
    const tabPanels = document.querySelectorAll(".tab-content-panel");

    function switchTab(tabId) {
        tabBtns.forEach(b => b.classList.remove("active"));
        tabPanels.forEach(p => {
            p.classList.remove("active");
            p.style.display = "none";
        });

        const activeBtn = document.querySelector(`.panel-tab-btn[data-tab="${tabId}"]`);
        const activePanel = document.getElementById(tabId);

        if (activePanel) {
            if (activeBtn) activeBtn.classList.add("active");
            activePanel.classList.add("active");
            activePanel.style.display = "block";
        }
    }

    tabBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const tabId = btn.getAttribute("data-tab");
            if (tabId) {
                switchTab(tabId);
            }
        });
    });

    const urlParams = new URLSearchParams(window.location.search);
    const tabParam = urlParams.get("tab");
    if (tabParam === "suppliers") {
        switchTab("tab-suppliers");
    } else if (tabParam === "transactions" || tabParam === "movements") {
        switchTab("tab-movements");
    }

    const searchInput = document.getElementById("inventorySearchInput");
    if (searchInput) {
        searchInput.addEventListener("input", (e) => {
            const query = e.target.value.toLowerCase().trim();
            const rows = document.querySelectorAll("#inventoryTable tbody tr");
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                row.style.display = text.includes(query) ? "" : "none";
            });
        });
    }

    function openModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add("active");
            document.body.style.overflow = "hidden"; 
        }
    }

    function closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove("active");
            document.body.style.overflow = ""; 
        }
    }

    function closeAllModals() {
        document.querySelectorAll(".modal-overlay.active").forEach(m => {
            m.classList.remove("active");
        });
        document.body.style.overflow = "";
    }

    const openAddItemBtn = document.getElementById("openAddItemModalBtn");
    if (openAddItemBtn) {
        openAddItemBtn.addEventListener("click", () => {
            const title = document.getElementById("itemModalTitle");
            if (title) 
                title.textContent = "Add New Inventory Item";
            const formId = document.getElementById("itemFormId");
            if (formId) 
                formId.value = "";
            const formName = document.getElementById("itemFormName");
            if (formName) 
                formName.value = "";
            const formUnit = document.getElementById("itemFormUnit");
            if (formUnit) 
                formUnit.value = "Pcs";
            const formQty = document.getElementById("itemFormQty");
            if (formQty) 
                formQty.value = "50";
            const formMin = document.getElementById("itemFormMin");
            if (formMin) 
                formMin.value = "10";
            openModal("inventoryItemModal");
        });
    }

    const openAddSupplierBtn = document.getElementById("openAddSupplierModalBtn");
    if (openAddSupplierBtn) {
        openAddSupplierBtn.addEventListener("click", () => {
            const title = document.getElementById("supplierModalTitle");
            if (title) 
                title.textContent = "Register Supplier";
            const formId = document.getElementById("supplierFormId");
            if (formId) 
                formId.value = "";
            const formName = document.getElementById("supplierFormName");
            if (formName) 
                formName.value = "";
            const formPhone = document.getElementById("supplierFormPhone");
            if (formPhone) 
                formPhone.value = "";
            const formEmail = document.getElementById("supplierFormEmail");
            if (formEmail) 
                formEmail.value = "";
            const formAddress = document.getElementById("supplierFormAddress");
            if (formAddress) 
                formAddress.value = "";
            openModal("supplierModal");
        });
    }

    document.querySelectorAll(".edit-item-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            const title = document.getElementById("itemModalTitle");
            if (title) title.textContent = "Edit Inventory Item";
            const formId = document.getElementById("itemFormId");
            if (formId) formId.value = btn.dataset.id || "";
            const formName = document.getElementById("itemFormName");
            if (formName) formName.value = btn.dataset.name || "";
            const formUnit = document.getElementById("itemFormUnit");
            if (formUnit) formUnit.value = btn.dataset.unit || "";
            const formQty = document.getElementById("itemFormQuantity");
            if (formQty) formQty.value = btn.dataset.qty || "";
            const formMin = document.getElementById("itemFormMin");
            if (formMin) formMin.value = btn.dataset.min || "";
            const formSupplier = document.getElementById("itemFormSupplier");
            if (formSupplier) formSupplier.value = btn.dataset.supplier || "";
            openModal("inventoryItemModal");
        });
    });

    document.querySelectorAll(".adjust-stock-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            const formItemId = document.getElementById("txFormItemId");
            if (formItemId) 
                formItemId.value = btn.dataset.id || "";
            const formItemName = document.getElementById("txFormItemName");
            if (formItemName) 
                formItemName.value = btn.dataset.name || "";
            const formCurrentQty = document.getElementById("txFormCurrentQty");
            if (formCurrentQty) 
                formCurrentQty.value = (btn.dataset.qty || "0") + " " + (btn.dataset.unit || "");
            openModal("stockMovementModal");
        });
    });

    document.querySelectorAll(".edit-supplier-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            const title = document.getElementById("supplierModalTitle");
            if (title) 
                title.textContent = "Edit Supplier Details";
            const formId = document.getElementById("supplierFormId");
            if (formId) 
                formId.value = btn.dataset.id || "";
            const formName = document.getElementById("supplierFormName");
            if (formName) 
                formName.value = btn.dataset.name || "";
            const formPhone = document.getElementById("supplierFormPhone");
            if (formPhone) 
                formPhone.value = btn.dataset.phone || "";
            const formEmail = document.getElementById("supplierFormEmail");
            if (formEmail) 
                formEmail.value = btn.dataset.email || "";
            const formAddress = document.getElementById("supplierFormAddress");
            if (formAddress) 
                formAddress.value = btn.dataset.address || "";
            openModal("supplierModal");
        });
    });

    document.querySelectorAll(".view-supplier-btn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            const name = document.getElementById("viewSupplierName");
            if (name) 
                name.textContent = btn.dataset.name || "Supplier";
            const id = document.getElementById("viewSupplierId");
            if (id) 
                id.textContent = "#SUP-" + (btn.dataset.id || "");
            const phone = document.getElementById("viewSupplierPhone");
            if (phone) 
                phone.textContent = btn.dataset.phone || "N/A";
            const email = document.getElementById("viewSupplierEmail");
            if (email) 
                email.textContent = btn.dataset.email || "N/A";
            const address = document.getElementById("viewSupplierAddress");
            if (address) 
                address.textContent = btn.dataset.address || "N/A";
            openModal("viewSupplierModal");
        });
    });

    document.querySelectorAll("[data-close-modal]").forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            const overlay = btn.closest(".modal-overlay");
            if (overlay) {
                overlay.classList.remove("active");
                document.body.style.overflow = "";
            }
        });
    });

    document.querySelectorAll(".modal-overlay").forEach(overlay => {
        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) {
                overlay.classList.remove("active");
                document.body.style.overflow = "";
            }
        });
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            closeAllModals();
        }
    });
});
