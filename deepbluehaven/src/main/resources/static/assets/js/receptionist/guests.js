(function () {
    function initDropdowns(container) {
        if (!container) return;
        const dropdowns = container.querySelectorAll('.filter-dropdown[data-input]');
        dropdowns.forEach(dd => {
            const btn = dd.querySelector('.filter-btn, .form-filter-btn');
            const menu = dd.querySelector('.filter-menu, .form-filter-menu');
            const label = dd.querySelector('.filter-label strong');
            const input = document.getElementById(dd.dataset.input);
            if (btn) btn.addEventListener('click', e => { e.stopPropagation(); dropdowns.forEach(o => o !== dd && o.classList.remove('active')); dd.classList.toggle('active'); });
            if (menu) menu.querySelectorAll('button').forEach(opt => opt.addEventListener('click', () => { if (label) label.textContent = opt.textContent.trim(); if (input) input.value = opt.dataset.value; dd.classList.remove('active'); }));
        });
        document.addEventListener('click', () => dropdowns.forEach(d => d.classList.remove('active')));
    }

    function setDd(inputId, value) {
        const input = document.getElementById(inputId);
        const dd = document.querySelector('.filter-dropdown[data-input="' + inputId + '"]');
        if (!input || !dd) return;
        input.value = value;
        const opt = dd.querySelector('.filter-menu button[data-value="' + value + '"]');
        const lbl = dd.querySelector('.filter-label strong');
        if (opt && lbl) lbl.textContent = opt.textContent.trim();
    }

    function openModal(id) {
        const el = document.getElementById(id);
        if (el) el.classList.add('active');
    }
    function closeModal(id) {
        const el = document.getElementById(id);
        if (el) el.classList.remove('active');
    }

    const editModal = document.getElementById('editGuestModal');
    if (editModal) initDropdowns(editModal);

    document.querySelectorAll('.edit-guest-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const nameEl = document.getElementById('guestName');
            const passportEl = document.getElementById('guestPassport');
            const phoneEl = document.getElementById('guestPhone');
            const emailEl = document.getElementById('guestEmail');
            const noteEl = document.getElementById('guestNote');
            if (nameEl) nameEl.value = btn.dataset.name || '';
            if (passportEl) passportEl.value = btn.dataset.passport || '';
            if (phoneEl) phoneEl.value = btn.dataset.phone || '';
            if (emailEl) emailEl.value = btn.dataset.email || '';
            if (noteEl) noteEl.value = btn.dataset.note || '';
            setDd('guestVip', btn.dataset.vip || 'NONE');
            openModal('editGuestModal');
        });
    });

    document.getElementById('closeEditGuestModal')?.addEventListener('click', () => closeModal('editGuestModal'));
    document.getElementById('cancelEditGuestModal')?.addEventListener('click', () => closeModal('editGuestModal'));
    document.getElementById('editGuestModal')?.addEventListener('click', e => { if (e.target === e.currentTarget) closeModal('editGuestModal'); });
    document.getElementById('editGuestForm')?.addEventListener('submit', e => { e.preventDefault(); alert('Guest profile saved!'); closeModal('editGuestModal'); });

    document.getElementById('registerGuestBtn')?.addEventListener('click', () => openModal('registerGuestModal'));
    document.getElementById('closeRegisterGuestModal')?.addEventListener('click', () => closeModal('registerGuestModal'));
    document.getElementById('cancelRegisterGuestModal')?.addEventListener('click', () => closeModal('registerGuestModal'));
    document.getElementById('registerGuestModal')?.addEventListener('click', e => { if (e.target === e.currentTarget) closeModal('registerGuestModal'); });
})();
