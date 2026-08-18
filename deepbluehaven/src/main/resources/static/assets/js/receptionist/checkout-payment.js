/**
 * Check-Out & Folio Payment Interactive Logic - DeepBlue Haven PMS
 */

let currentFolioData = {
    roomCharge: 0,
    serviceCharge: 0,
    tierDiscount: 0,
    depositPaid: 0,
    vouchers: []
};

const formatVnd = (num) => {
    return new Intl.NumberFormat('vi-VN').format(Math.round(num)) + ' VND';
};

const parseVnd = (str) => {
    if (!str) return 0;
    const clean = String(str).replace(/[^0-9]/g, '');
    return clean ? parseInt(clean, 10) : 0;
};

const getContextPath = () => {
    const path = window.location.pathname;
    if (path.startsWith('/deepbluehaven/')) return '/deepbluehaven';
    return '';
};
const ctx = getContextPath();

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

function recalculateCheckoutFolio() {
    const surchargeSelect = document.getElementById('coModalSurchargeType');
    const voucherSelect = document.getElementById('coModalVoucherSelect');
    const surchargeType = surchargeSelect ? surchargeSelect.value : 'NONE';
    const selectedVoucherId = voucherSelect ? voucherSelect.value : '';

    let surchargeAmount = 0;
    let surchargeLabel = 'Standard';
    const oneNightEst = currentFolioData.roomCharge > 0 ? currentFolioData.roomCharge : 1000000;

    if (surchargeType === 'EARLY_CHECKIN_30') {
        surchargeAmount = Math.round(oneNightEst * 0.3);
        surchargeLabel = 'Early Check-In (+30%)';
    } else if (surchargeType === 'EARLY_CHECKIN_50') {
        surchargeAmount = Math.round(oneNightEst * 0.5);
        surchargeLabel = 'Early Check-In (+50%)';
    } else if (surchargeType === 'LATE_CHECKOUT_30') {
        surchargeAmount = Math.round(oneNightEst * 0.3);
        surchargeLabel = 'Late Check-Out (+30%)';
    } else if (surchargeType === 'LATE_CHECKOUT_50') {
        surchargeAmount = Math.round(oneNightEst * 0.5);
        surchargeLabel = 'Late Check-Out (+50%)';
    }

    const surchargeRow = document.getElementById('coModalSurchargeRow');
    const surchargeNote = document.getElementById('coModalSurchargeNote');
    const surchargeVal = document.getElementById('coModalSurchargeAmount');

    if (surchargeRow) {
        if (surchargeAmount > 0) {
            surchargeRow.style.display = '';
            if (surchargeNote) surchargeNote.textContent = surchargeLabel;
            if (surchargeVal) surchargeVal.textContent = '+' + formatVnd(surchargeAmount);
        } else {
            surchargeRow.style.display = 'none';
        }
    }

    const grossSubtotal = currentFolioData.roomCharge + currentFolioData.serviceCharge + surchargeAmount;

    // Calculate Voucher Discount
    let voucherDiscount = 0;
    let voucherCodeText = '';
    if (selectedVoucherId && currentFolioData.vouchers) {
        const found = currentFolioData.vouchers.find(v => String(v.id) === String(selectedVoucherId));
        if (found) {
            voucherCodeText = found.code;
            if (found.discountType === 'PERCENTAGE') {
                voucherDiscount = Math.round(grossSubtotal * (found.discountValue / 100));
            } else {
                voucherDiscount = Math.round(found.discountValue);
            }
        }
    }

    const totalDiscount = Math.min(grossSubtotal, currentFolioData.tierDiscount + voucherDiscount);
    const discountRow = document.getElementById('coModalDiscountRow');
    const discountCodeElem = document.getElementById('coModalDiscountCode');
    const discountAmtElem = document.getElementById('coModalDiscountAmount');

    if (discountRow) {
        if (totalDiscount > 0) {
            discountRow.style.display = '';
            if (discountCodeElem) discountCodeElem.textContent = voucherCodeText ? `Voucher ${voucherCodeText}` : 'Member Tier Discount';
            if (discountAmtElem) discountAmtElem.textContent = '-' + formatVnd(totalDiscount);
        } else {
            discountRow.style.display = 'none';
        }
    }

    const netSubtotal = Math.max(0, grossSubtotal - totalDiscount);
    const tax = Math.round(netSubtotal * 0.08);
    const totalFolio = netSubtotal + tax;
    const remainingDue = Math.max(0, totalFolio - currentFolioData.depositPaid);

    const taxElem = document.getElementById('coModalTax');
    if (taxElem) taxElem.textContent = formatVnd(tax);

    const totalFolioElem = document.getElementById('coModalTotalFolio');
    if (totalFolioElem) totalFolioElem.textContent = formatVnd(remainingDue);
}

async function populateAndOpenCheckoutModal(row) {
    const bookingId = row.dataset.bookingId;
    const bookingCode = row.dataset.bookingCode;
    const custName = row.dataset.customerName;
    const roomNum = row.dataset.roomNumber;
    const stayDates = row.dataset.stayDates;
    const roomCharge = row.dataset.roomCharge;
    const pricingNote = row.dataset.pricingNote;
    const serviceCharge = row.dataset.serviceCharge;
    const discountCode = row.dataset.discountCode;
    const discountAmount = row.dataset.discountAmount;
    const depositPaid = row.dataset.depositPaid;
    const remainingPayable = row.dataset.remainingPayable;
    const tax = row.dataset.tax;

    // Cache values
    currentFolioData.roomCharge = parseVnd(roomCharge);
    currentFolioData.serviceCharge = parseVnd(serviceCharge);
    currentFolioData.tierDiscount = parseVnd(discountAmount);
    currentFolioData.depositPaid = parseVnd(depositPaid);
    currentFolioData.vouchers = [];

    // Fill Modal elements
    const bookingIdInput = document.getElementById('coModalBookingId');
    if (bookingIdInput) bookingIdInput.value = bookingId;

    const refElem = document.getElementById('coModalBookingRef');
    if (refElem) refElem.textContent = bookingCode || ('DBH-' + bookingId);

    const guestElem = document.getElementById('coModalGuestName');
    if (guestElem) guestElem.textContent = custName || 'Guest';

    const roomElem = document.getElementById('coModalRoomNum');
    if (roomElem) roomElem.textContent = 'Room ' + (roomNum || '304');

    const datesElem = document.getElementById('coModalStayDates');
    if (datesElem) datesElem.textContent = stayDates || '';

    const noteElem = document.getElementById('coModalPricingNote');
    if (noteElem) noteElem.textContent = pricingNote || 'Standard Rate';

    const roomChargeElem = document.getElementById('coModalRoomCharge');
    if (roomChargeElem) roomChargeElem.textContent = roomCharge || '0 VND';

    const serviceChargeElem = document.getElementById('coModalServiceCharge');
    if (serviceChargeElem) serviceChargeElem.textContent = serviceCharge || '0 VND';

    const depositElem = document.getElementById('coModalDepositPaid');
    if (depositElem) depositElem.textContent = '-' + (depositPaid || '0 VND');

    // Reset Surcharge & Voucher Selectors
    const surchargeSelect = document.getElementById('coModalSurchargeType');
    if (surchargeSelect) surchargeSelect.value = 'NONE';

    const voucherSelect = document.getElementById('coModalVoucherSelect');
    if (voucherSelect) {
        voucherSelect.innerHTML = '<option value="">-- No Extra Voucher Applied --</option>';
        try {
            const res = await fetch(`${ctx}/receptionist/api/checkout-folio/${bookingId}`);
            if (res.ok) {
                const folio = await res.json();
                if (folio.availableVouchers && folio.availableVouchers.length > 0) {
                    currentFolioData.vouchers = folio.availableVouchers;
                    folio.availableVouchers.forEach(v => {
                        const opt = document.createElement('option');
                        opt.value = v.id;
                        opt.textContent = `${v.code} (${v.description || (v.discountValue + (v.discountType === 'PERCENTAGE' ? '%' : ' VND'))})`;
                        voucherSelect.appendChild(opt);
                    });
                }
            }
        } catch (err) {
            console.error('Error fetching folio vouchers:', err);
        }
    }

    recalculateCheckoutFolio();

    const modal = document.getElementById('checkoutConfirmModal');
    if (modal) {
        modal.style.display = 'flex';
        modal.classList.add('active');
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
            fetch(`${ctx}/api/vnpay/create-checkout-payment?bookingId=` + bookingId + (amountText ? '&amount=' + amountText : ''))
                .then(res => res.json())
                .then(resData => {
                    if (resData.success && resData.data) {
                        window.location.href = resData.data;
                    } else {
                        alert('Could not generate VNPAY payment link: ' + (resData.message || 'Error'));
                        if (submitBtn) {
                            submitBtn.disabled = false;
                            submitBtn.innerHTML = '<i class="fa-solid fa-credit-card"></i> Pay via VNPAY Sandbox Gateway';
                        }
                    }
                })
                .catch(err => {
                    alert('Error generating VNPAY link: ' + err);
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
    // Open Modal from Table Rows
    document.querySelectorAll('[data-open-checkout-modal]').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const row = btn.closest('tr');
            if (row) populateAndOpenCheckoutModal(row);
        });
    });

    const paymentSelect = document.querySelector('select[name="paymentMethodStr"]');
    if (paymentSelect) {
        paymentSelect.addEventListener('change', (e) => {
            updateCheckoutPaymentBtn(e.target.value);
        });
    }

    const surchargeSelect = document.getElementById('coModalSurchargeType');
    if (surchargeSelect) {
        surchargeSelect.addEventListener('change', () => {
            recalculateCheckoutFolio();
        });
    }

    const voucherSelect = document.getElementById('coModalVoucherSelect');
    if (voucherSelect) {
        voucherSelect.addEventListener('change', () => {
            recalculateCheckoutFolio();
        });
    }

    const submitBtn = document.getElementById('coModalSubmitBtn');
    if (submitBtn) {
        submitBtn.addEventListener('click', (e) => {
            triggerPaymentAction(e);
        });
    }

    // Modal close triggers
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = document.getElementById('checkoutConfirmModal');
            if (modal) {
                modal.classList.remove('active');
                modal.style.display = 'none';
            }
        });
    });

    const closeQrBtn = document.querySelector('#eWalletQrModal .ewallet-qr-actions button, [data-close-qr-modal]');
    if (closeQrBtn) {
        closeQrBtn.addEventListener('click', () => {
            const modal = document.getElementById('eWalletQrModal');
            if (modal) {
                modal.classList.remove('active');
                modal.style.display = 'none';
            }
        });
    }
});
