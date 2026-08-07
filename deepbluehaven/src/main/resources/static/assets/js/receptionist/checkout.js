document.addEventListener('DOMContentLoaded', function() {
    const selectElem = document.querySelector('select[name="paymentMethodStr"]');
    const submitBtn = document.getElementById('coModalSubmitBtn');
    const formElem = submitBtn ? submitBtn.closest('form') : null;

    if (selectElem && submitBtn) {
        selectElem.addEventListener('change', function() {
            submitBtn.setAttribute('data-method', this.value);
            
            if (this.value === 'CREDIT_CARD') {
                submitBtn.innerHTML = '<i class="fa-solid fa-credit-card"></i> Pay via VNPAY Sandbox Gateway';
            } else if (this.value === 'BANK_TRANSFER') {
                submitBtn.innerHTML = '<i class="fa-solid fa-qrcode"></i> Display VietQR Bank Transfer Code';
            } else if (this.value === 'E_WALLET') {
                submitBtn.innerHTML = '<i class="fa-solid fa-mobile-screen-button"></i> Display E-Wallet QR Code';
            } else {
                submitBtn.innerHTML = '<i class="fa-solid fa-circle-check"></i> Confirm Payment & Execute Check-Out';
            }
        });

        function handlePaymentSubmit(e) {
            const method = selectElem.value;
            const bookingIdInput = formElem ? formElem.querySelector('input[name="bookingId"]') : document.getElementById('coModalBookingId');
            const bookingId = bookingIdInput ? bookingIdInput.value : '';
            const totalFolioElem = document.getElementById('coModalTotalFolio');
            const amountText = totalFolioElem ? totalFolioElem.innerText.replace(/[^0-9]/g, '') : '';

            if (method === 'CREDIT_CARD') {
                e.preventDefault();
                e.stopPropagation();
                if (bookingId) {
                    submitBtn.disabled = true;
                    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Connecting to VNPAY...';
                    fetch('/deepbluehaven/api/vnpay/create-checkout-payment?bookingId=' + bookingId + (amountText ? '&amount=' + amountText : ''))
                        .then(res => res.json())
                        .then(resData => {
                            if (resData.success && resData.data) {
                                window.location.href = resData.data;
                            } else {
                                alert('Could not generate VNPAY payment link: ' + (resData.message || 'Error'));
                                submitBtn.disabled = false;
                                submitBtn.innerHTML = '<i class="fa-solid fa-credit-card"></i> Pay via VNPAY Sandbox Gateway';
                            }
                        })
                        .catch(err => {
                            alert('Error generating VNPAY link: ' + err);
                            submitBtn.disabled = false;
                            submitBtn.innerHTML = '<i class="fa-solid fa-credit-card"></i> Pay via VNPAY Sandbox Gateway';
                        });
                }
            } else if (method === 'BANK_TRANSFER' || method === 'E_WALLET') {
                e.preventDefault();
                e.stopPropagation();
                if (bookingId) {
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
                }
            }
        }

        submitBtn.addEventListener('click', handlePaymentSubmit);
        if (formElem) {
            formElem.addEventListener('submit', handlePaymentSubmit);
        }
    }
});

function closeEWalletQrModal() {
    const modal = document.getElementById('eWalletQrModal');
    if (modal) {
        modal.style.display = 'none';
        modal.classList.remove('active');
    }
    const checkoutModal = document.getElementById('checkoutConfirmModal');
    if (checkoutModal) {
        checkoutModal.classList.remove('active');
        checkoutModal.style.display = '';
    }
}
