function updateCheckoutPaymentBtn(val) {
    const submitBtn = document.getElementById('coModalSubmitBtn');
    if (!submitBtn) return;
    if (val === 'CREDIT_CARD') {
        submitBtn.innerHTML = '<i class="fa-solid fa-credit-card"></i> Pay via VNPAY Sandbox Gateway';
        submitBtn.style.backgroundColor = '#0284c7';
    } else if (val === 'BANK_TRANSFER') {
        submitBtn.innerHTML = '<i class="fa-solid fa-qrcode"></i> Display VietQR Bank Transfer Code';
        submitBtn.style.backgroundColor = '#0284c7';
    } else if (val === 'E_WALLET') {
        submitBtn.innerHTML = '<i class="fa-solid fa-mobile-screen-button"></i> Display E-Wallet QR Code';
        submitBtn.style.backgroundColor = '#0d9488';
    } else {
        submitBtn.innerHTML = '<i class="fa-solid fa-circle-check"></i> Confirm Payment & Execute Check-Out';
        submitBtn.style.backgroundColor = '';
    }
}

function triggerPaymentAction(e) {
    const selectElem = document.querySelector('select[name="paymentMethodStr"]');
    if (!selectElem) return true;

    const method = selectElem.value;
    const bookingIdInput = document.getElementById('coModalBookingId');
    const bookingId = bookingIdInput ? bookingIdInput.value : '';
    const totalFolioElem = document.getElementById('coModalTotalFolio');
    const amountText = totalFolioElem ? totalFolioElem.innerText.replace(/[^0-9]/g, '') : '';
    const submitBtn = document.getElementById('coModalSubmitBtn');

    if (method === 'CREDIT_CARD') {
        if (e) { e.preventDefault(); e.stopPropagation(); }
        if (bookingId) {
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Connecting to VNPAY...';
            }
            fetch('/deepbluehaven/api/vnpay/create-checkout-payment?bookingId=' + bookingId + (amountText ? '&amount=' + amountText : ''))
                .then(res => res.json())
                .then(resData => {
                    if (resData.success && resData.data) {
                        window.location.href = resData.data;
                    } else {
                        showToast('error', 'Payment Link Failed', 'Could not generate VNPAY payment link: ' + (resData.message || 'Error'));
                        if (submitBtn) {
                            submitBtn.disabled = false;
                            submitBtn.innerHTML = '<i class="fa-solid fa-credit-card"></i> Pay via VNPAY Sandbox Gateway';
                        }
                    }
                })
                .catch(err => {
                    showToast('error', 'VNPAY Error', 'Error generating VNPAY link: ' + err);
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.innerHTML = '<i class="fa-solid fa-credit-card"></i> Pay via VNPAY Sandbox Gateway';
                    }
                });
        }
        return false;
    } else if (method === 'BANK_TRANSFER' || method === 'E_WALLET') {
        if (e) { e.preventDefault(); e.stopPropagation(); }

        const checkoutModal = document.getElementById('checkoutConfirmModal');
        if (checkoutModal) {
            checkoutModal.classList.remove('active');
            checkoutModal.style.display = 'none';
        }

        const amountTextVal = amountText || '500000';
        const qrUrl = 'https://img.vietqr.io/image/970419-9704198526191432-compact.png?amount=' + amountTextVal + '&addInfo=Booking%20' + bookingId;

        const qrImg = document.getElementById('eWalletQrImage');
        if (qrImg) qrImg.src = qrUrl;

        const refText = document.getElementById('eWalletBookingRefText');
        if (refText) {
            refText.innerText = 'Booking #' + bookingId + (method === 'BANK_TRANSFER' ? ' VietQR Settlement' : ' E-Wallet QR');
        }

        const modal = document.getElementById('eWalletQrModal');
        if (modal) {
            modal.style.display = 'flex';
            modal.classList.add('active');
        }
        return false;
    }
    return true;
}

document.addEventListener('DOMContentLoaded', () => {
    const paymentSelect = document.querySelector('select[name="paymentMethodStr"]');
    if (paymentSelect) {
        paymentSelect.addEventListener('change', (e) => {
            updateCheckoutPaymentBtn(e.target.value);
        });
    }

    const submitBtn = document.getElementById('coModalSubmitBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', (e) => {
            triggerPaymentAction(e);
        });
    }

    const closeQrBtn = document.querySelector('#eWalletQrModal .ewallet-qr-actions button, [data-close-qr-modal]');
    if (closeQrBtn) {
        closeQrBtn.addEventListener('click', () => {
            closeEWalletQrModal();
        });
    }
});
