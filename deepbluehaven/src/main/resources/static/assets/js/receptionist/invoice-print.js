/**
 * Folio Invoice Print & Export Handling
 */
document.addEventListener('DOMContentLoaded', () => {
    // Auto-trigger print preview if query parameter '?autoprint' is present
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('autoprint')) {
        window.print();
    }

    const printBtn = document.getElementById('btnPrintInvoice');
    if (printBtn) {
        printBtn.addEventListener('click', () => {
            window.print();
        });
    }
});
