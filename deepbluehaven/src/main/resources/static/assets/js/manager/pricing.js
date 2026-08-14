document.addEventListener('DOMContentLoaded', () => {
    console.log('[Pricing JS] Initializing pricing & offers management scripts...');

    // Tab switching logic
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            const targetId = btn.getAttribute('data-target');
            console.log('[Pricing JS] Switching tab to:', targetId);
            
            tabBtns.forEach(b => b.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));
            
            btn.classList.add('active');
            const targetContent = document.getElementById(targetId);
            if (targetContent) {
                targetContent.classList.add('active');
            } else {
                console.warn('[Pricing JS] Target tab content not found:', targetId);
            }
        });
    });

    // 1. Pricing Rule Modal
    const ruleModal = document.getElementById('ruleModal');
    const addRuleBtn = document.getElementById('addRuleBtn');
    const closeRuleModalBtn = document.getElementById('closeRuleModalBtn');
    const cancelRuleModalBtn = document.getElementById('cancelRuleModalBtn');
    const ruleForm = document.getElementById('ruleForm');
    const ruleTitle = document.getElementById('ruleModalTitle');

    const closeRuleModal = () => {
        console.log('[Pricing JS] Closing Rule Modal');
        if (ruleModal) ruleModal.classList.remove('active');
    };
    if (closeRuleModalBtn) closeRuleModalBtn.addEventListener('click', closeRuleModal);
    if (cancelRuleModalBtn) cancelRuleModalBtn.addEventListener('click', closeRuleModal);

    if (addRuleBtn) {
        addRuleBtn.addEventListener('click', () => {
            console.log('[Pricing JS] Add Rule button clicked');
            if (ruleTitle) ruleTitle.innerHTML = '<i class="fa-solid fa-tags"></i> Add Pricing Rule';
            if (ruleForm) ruleForm.reset();
            const ruleIdInput = document.getElementById('ruleId');
            if (ruleIdInput) ruleIdInput.value = '';
            window.setDropdownValue('ruleStatus', 'ACTIVE');
            if (ruleModal) ruleModal.classList.add('active');
        });
    }

    document.querySelectorAll('.edit-rule-btn').forEach((btn, idx) => {
        btn.addEventListener('click', (e) => {
            console.log('[Pricing JS] Edit Rule button clicked at index:', idx);
            if (ruleTitle) ruleTitle.innerHTML = '<i class="fa-solid fa-tags"></i> Edit Pricing Rule';
            const button = e.target.closest('.edit-rule-btn');
            if (button) {
                const ruleIdInput = document.getElementById('ruleId');
                const ruleRoomTypeSelect = document.getElementById('ruleRoomType');
                const ruleStartDateInput = document.getElementById('ruleStartDate');
                const ruleEndDateInput = document.getElementById('ruleEndDate');
                const ruleMultiplierInput = document.getElementById('ruleMultiplier');

                if (ruleIdInput) ruleIdInput.value = button.dataset.id || '';
                if (ruleRoomTypeSelect) ruleRoomTypeSelect.value = button.dataset.roomType || 'STANDARD';
                if (ruleStartDateInput) ruleStartDateInput.value = button.dataset.startDate || '';
                if (ruleEndDateInput) ruleEndDateInput.value = button.dataset.endDate || '';
                if (ruleMultiplierInput) ruleMultiplierInput.value = button.dataset.multiplier || '1.0';
            }
            if (ruleModal) ruleModal.classList.add('active');
        });
    });

    // 2. Discount Modal
    const discountModal = document.getElementById('discountModal');
    const addDiscountBtn = document.getElementById('addDiscountBtn');
    const closeDiscountModalBtn = document.getElementById('closeDiscountModalBtn');
    const cancelDiscountModalBtn = document.getElementById('cancelDiscountModalBtn');
    const discountForm = document.getElementById('discountForm');
    const discountTitle = document.getElementById('discountModalTitle');

    const closeDiscountModal = () => {
        console.log('[Pricing JS] Closing Discount Modal');
        if (discountModal) discountModal.classList.remove('active');
    };
    if (closeDiscountModalBtn) closeDiscountModalBtn.addEventListener('click', closeDiscountModal);
    if (cancelDiscountModalBtn) cancelDiscountModalBtn.addEventListener('click', closeDiscountModal);

    if (addDiscountBtn) {
        addDiscountBtn.addEventListener('click', () => {
            console.log('[Pricing JS] Add Discount button clicked');
            if (discountTitle) discountTitle.innerHTML = '<i class="fa-solid fa-ticket"></i> Add Discount Code';
            if (discountForm) discountForm.reset();
            window.setDropdownValue('discountType', 'PERCENTAGE');
            window.setDropdownValue('discountStatus', 'ACTIVE');
            if (discountModal) discountModal.classList.add('active');
        });
    }

    document.querySelectorAll('.edit-discount-btn').forEach((btn, idx) => {
        btn.addEventListener('click', (e) => {
            console.log('[Pricing JS] Edit Discount button clicked at index:', idx);
            if (discountTitle) discountTitle.innerHTML = '<i class="fa-solid fa-ticket"></i> Edit Discount Code';
            const row = e.target.closest('tr');
            if (row) {
                const codeTd = row.querySelector('td:nth-child(1)');
                const typeTd = row.querySelector('td:nth-child(2)');
                const valTd = row.querySelector('td:nth-child(3)');
                const statusSpan = row.querySelector('td:nth-child(5) .status');

                if (codeTd && document.getElementById('discountCode')) {
                    document.getElementById('discountCode').value = codeTd.textContent.trim();
                }
                if (typeTd) {
                    const type = typeTd.textContent.trim();
                    window.setDropdownValue('discountType', type.toLowerCase().includes('percentage') ? 'PERCENTAGE' : 'FIXED_AMOUNT');
                }
                if (valTd && document.getElementById('discountValue')) {
                    document.getElementById('discountValue').value = valTd.textContent.trim().replace(/[^\d.]/g, '');
                }
                if (statusSpan) {
                    const statusText = statusSpan.textContent.trim();
                    window.setDropdownValue('discountStatus', statusText === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE');
                }
            }
            if (discountModal) discountModal.classList.add('active');
        });
    });

    // 3. Membership Tier Modal
    const tierModal = document.getElementById('tierModal');
    const closeTierModalBtn = document.getElementById('closeTierModalBtn');
    const cancelTierModalBtn = document.getElementById('cancelTierModalBtn');

    const closeTierModal = () => {
        console.log('[Pricing JS] Closing Tier Modal');
        if (tierModal) tierModal.classList.remove('active');
    };
    if (closeTierModalBtn) closeTierModalBtn.addEventListener('click', closeTierModal);
    if (cancelTierModalBtn) cancelTierModalBtn.addEventListener('click', closeTierModal);

    document.querySelectorAll('.edit-tier-btn').forEach((btn, idx) => {
        btn.addEventListener('click', (e) => {
            console.log('[Pricing JS] Edit Tier button clicked at index:', idx);
            const button = e.target.closest('.edit-tier-btn');
            if (button) {
                const tierId = document.getElementById('tierId');
                const tierNameInput = document.getElementById('tierNameInput');
                const tierMinSpent = document.getElementById('tierMinSpent');
                const tierMinPoints = document.getElementById('tierMinPoints');
                const tierPointMultiplier = document.getElementById('tierPointMultiplier');
                const tierDiscountRate = document.getElementById('tierDiscountRate');
                const tierDescription = document.getElementById('tierDescription');

                if (tierId) tierId.value = button.dataset.id || '';
                if (tierNameInput) tierNameInput.value = button.dataset.name || '';
                if (tierMinSpent) tierMinSpent.value = button.dataset.minSpent || '0';
                if (tierMinPoints) tierMinPoints.value = button.dataset.minPoints || '0';
                if (tierPointMultiplier) tierPointMultiplier.value = button.dataset.multiplier || '1.00';
                if (tierDiscountRate) tierDiscountRate.value = button.dataset.discountRate || '0.00';
                if (tierDescription) tierDescription.value = button.dataset.description || '';

                console.log('[Pricing JS] Populated Tier Modal with data:', button.dataset);
            }
            if (tierModal) tierModal.classList.add('active');
        });
    });

    // Overlay click to close any modal
    [ruleModal, discountModal, tierModal].forEach(modal => {
        if (modal) {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    console.log('[Pricing JS] Overlay clicked, closing modal');
                    modal.classList.remove('active');
                }
            });
        }
    });

    // Filter dropdowns logic
    const filterDropdowns = document.querySelectorAll('.filter-dropdown');

    filterDropdowns.forEach(dropdown => {
        const btn = dropdown.querySelector('.filter-btn');
        const menu = dropdown.querySelector('.filter-menu');
        const label = dropdown.querySelector('.filter-label strong');
        const inputId = dropdown.dataset.input;
        const hiddenInput = document.getElementById(inputId);

        if (btn) {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                filterDropdowns.forEach(other => {
                    if (other !== dropdown) other.classList.remove('active');
                });
                dropdown.classList.toggle('active');
            });
        }

        if (menu) {
            menu.querySelectorAll('button').forEach(opt => {
                opt.addEventListener('click', () => {
                    if (label) label.textContent = opt.textContent.trim();
                    if (hiddenInput) hiddenInput.value = opt.dataset.value;
                    dropdown.classList.remove('active');
                });
            });
        }
    });

    document.addEventListener('click', () => {
        filterDropdowns.forEach(d => d.classList.remove('active'));
    });

    window.setDropdownValue = function(inputId, value) {
        const hidden = document.getElementById(inputId);
        const dropdown = document.querySelector(`.filter-dropdown[data-input="${inputId}"]`);
        if (hidden && dropdown) {
            hidden.value = value;
            const option = dropdown.querySelector(`.filter-menu button[data-value="${value}"]`);
            const lbl = dropdown.querySelector('.filter-label strong');
            if (option && lbl) {
                lbl.textContent = option.textContent.trim();
            } else if (value === '' && lbl) {
                lbl.textContent = 'Select...';
            }
        }
    };

    console.log('[Pricing JS] Setup completed. Edit buttons bound:', {
        editRules: document.querySelectorAll('.edit-rule-btn').length,
        editDiscounts: document.querySelectorAll('.edit-discount-btn').length,
        editTiers: document.querySelectorAll('.edit-tier-btn').length
    });
});
