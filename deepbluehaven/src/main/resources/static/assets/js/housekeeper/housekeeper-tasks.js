(function () {
    function getApiUrl(path) {
        let contextPath = document.querySelector('meta[name="_context_path"]')?.content;
        if (!contextPath || contextPath === "/") {
            const match = window.location.pathname.match(/^\/([^\/]+)/);
            if (match && match[1] === "deepbluehaven") {
                contextPath = "/" + match[1];
            } else {
                contextPath = "";
            }
        }
        return contextPath.replace(/\/$/, "") + (path.startsWith("/") ? path : "/" + path);
    }

    const dropdowns = document.querySelectorAll('#reportIssueModal .filter-dropdown[data-input]');
    dropdowns.forEach(dd => {
        const btn = dd.querySelector('.filter-btn, .form-filter-btn');
        const menu = dd.querySelector('.filter-menu, .form-filter-menu');
        const label = dd.querySelector('.filter-label strong');
        const input = document.getElementById(dd.dataset.input);
        if (btn) btn.addEventListener('click', e => { e.stopPropagation(); dropdowns.forEach(o => o !== dd && o.classList.remove('active')); dd.classList.toggle('active'); });
        if (menu) menu.querySelectorAll('button').forEach(opt => opt.addEventListener('click', () => { if (label) label.textContent = opt.textContent.trim(); if (input) input.value = opt.dataset.value; dd.classList.remove('active'); }));
    });
    document.addEventListener('click', () => dropdowns.forEach(d => d.classList.remove('active')));

    const modal = document.getElementById('reportIssueModal');
    document.getElementById('reportIssueBtn')?.addEventListener('click', () => modal?.classList.add('active'));
    document.getElementById('closeReportModal')?.addEventListener('click', () => modal?.classList.remove('active'));
    document.getElementById('cancelReportModal')?.addEventListener('click', () => modal?.classList.remove('active'));
    modal?.addEventListener('click', e => { if (e.target === modal) modal.classList.remove('active'); });

    document.getElementById('reportIssueForm')?.addEventListener('submit', async e => {
        e.preventDefault();
        const roomNumber = document.getElementById('issueRoom')?.value;
        const issueType = document.getElementById('issueType')?.value || 'OTHER';
        const priority = document.getElementById('issuePriority')?.value || 'NORMAL';
        const description = document.getElementById('issueDescription')?.value;

        const formData = new URLSearchParams();
        formData.append('roomNumber', roomNumber);
        formData.append('issueType', issueType);
        formData.append('priority', priority);
        formData.append('description', description);

        try {
            const resp = await fetch(getApiUrl('/housekeeper/report-issue'), {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            });
            const data = await resp.json();
            if (data.success) {
                alert(data.message || 'Issue reported! Room has been set to MAINTENANCE.');
                modal?.classList.remove('active');
                window.location.reload();
            } else {
                alert(data.message || 'Failed to report issue: ' + data.message);
            }
        } catch (err) {
            alert('Error connecting to server.');
        }
    });
})();
