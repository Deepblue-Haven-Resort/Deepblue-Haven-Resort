// pricing.js
document.addEventListener('DOMContentLoaded', () => {
    // --- Tabs Logic ---
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            // Remove active class from all
            tabBtns.forEach(b => b.classList.remove('active'));
            tabContents.forEach(c => c.classList.remove('active'));
            
            // Add active class to clicked
            btn.classList.add('active');
            const targetId = btn.getAttribute('data-target');
            document.getElementById(targetId).classList.add('active');
        });
    });

    // --- Modal Logic (Pricing Rules) ---
    const ruleModal = document.getElementById('ruleModal');
    const addRuleBtn = document.getElementById('addRuleBtn');
    const closeRuleModalBtn = document.getElementById('closeRuleModalBtn');
    const cancelRuleModalBtn = document.getElementById('cancelRuleModalBtn');
    const ruleForm = document.getElementById('ruleForm');
    const ruleTitle = document.getElementById('ruleModalTitle');

    const closeRuleModal = () => ruleModal.classList.remove('active');
    if (closeRuleModalBtn) closeRuleModalBtn.addEventListener('click', closeRuleModal);
    if (cancelRuleModalBtn) cancelRuleModalBtn.addEventListener('click', closeRuleModal);

    if (addRuleBtn) {
        addRuleBtn.addEventListener('click', () => {
            ruleTitle.innerHTML = '<i class="fa-solid fa-tags"></i> Add Pricing Rule';
            ruleForm.reset();
            window.setDropdownValue('ruleStatus', 'ACTIVE');
            ruleModal.classList.add('active');
        });
    }

    document.querySelectorAll('.edit-rule-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            ruleTitle.innerHTML = '<i class="fa-solid fa-tags"></i> Edit Pricing Rule';
            const row = e.target.closest('tr');
            document.getElementById('ruleName').value = row.querySelector('td:nth-child(1)').textContent.trim();
            // date parsing skipped for simple mockup
            document.getElementById('ruleMultiplier').value = row.querySelector('td:nth-child(4)').textContent.trim().replace('x ', '');
            const statusText = row.querySelector('td:nth-child(5) .status').textContent.trim();
            window.setDropdownValue('ruleStatus', statusText === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE');
            ruleModal.classList.add('active');
        });
    });

    ruleForm.addEventListener('submit', (e) => {
        e.preventDefault();
        alert('Pricing Rule saved successfully!');
        closeRuleModal();
    });

    // --- Modal Logic (Discounts) ---
    const discountModal = document.getElementById('discountModal');
    const addDiscountBtn = document.getElementById('addDiscountBtn');
    const closeDiscountModalBtn = document.getElementById('closeDiscountModalBtn');
    const cancelDiscountModalBtn = document.getElementById('cancelDiscountModalBtn');
    const discountForm = document.getElementById('discountForm');
    const discountTitle = document.getElementById('discountModalTitle');

    const closeDiscountModal = () => discountModal.classList.remove('active');
    if (closeDiscountModalBtn) closeDiscountModalBtn.addEventListener('click', closeDiscountModal);
    if (cancelDiscountModalBtn) cancelDiscountModalBtn.addEventListener('click', closeDiscountModal);

    if (addDiscountBtn) {
        addDiscountBtn.addEventListener('click', () => {
            discountTitle.innerHTML = '<i class="fa-solid fa-ticket"></i> Add Discount Code';
            discountForm.reset();
            window.setDropdownValue('discountType', 'PERCENTAGE');
            window.setDropdownValue('discountStatus', 'ACTIVE');
            discountModal.classList.add('active');
        });
    }

    document.querySelectorAll('.edit-discount-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            discountTitle.innerHTML = '<i class="fa-solid fa-ticket"></i> Edit Discount Code';
            const row = e.target.closest('tr');
            document.getElementById('discountCode').value = row.querySelector('td:nth-child(1)').textContent.trim();
            
            const type = row.querySelector('td:nth-child(2)').textContent.trim();
            window.setDropdownValue('discountType', type === 'Percentage' ? 'PERCENTAGE' : 'FIXED');
            
            document.getElementById('discountValue').value = row.querySelector('td:nth-child(3)').textContent.trim().replace(/\D/g, '');
            const statusText = row.querySelector('td:nth-child(5) .status').textContent.trim();
            window.setDropdownValue('discountStatus', statusText === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE');
            discountModal.classList.add('active');
        });
    });

    discountForm.addEventListener('submit', (e) => {
        e.preventDefault();
        alert('Discount Code saved successfully!');
        closeDiscountModal();
    });

    // Close Modals on outside click
    [ruleModal, discountModal].forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    });

    // --- Custom Dropdown Logic ---
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
                    label.textContent = opt.textContent.trim();
                    if (hiddenInput) hiddenInput.value = opt.dataset.value;
                    dropdown.classList.remove('active');
                });
            });
        }
    });

    document.addEventListener('click', () => {
        filterDropdowns.forEach(d => d.classList.remove('active'));
    });

    // Helper to set dropdown value programmatically
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
});
