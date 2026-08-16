package deepbluehaven.controllers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import deepbluehaven.dto.ApiResponse;
import deepbluehaven.dto.ReceptionistDTO;
import deepbluehaven.pojo.Booking;
import deepbluehaven.pojo.BookingDetail;
import deepbluehaven.pojo.Invoice;
import deepbluehaven.pojo.PaymentTransaction;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.BookingStatus;
import deepbluehaven.pojo.enums.InvoiceStatus;
import deepbluehaven.pojo.enums.NotificationType;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.pojo.enums.PaymentMethod;
import deepbluehaven.pojo.enums.PaymentType;
import deepbluehaven.pojo.ServiceOrder;
import deepbluehaven.pojo.enums.ServiceOrderStatus;
import deepbluehaven.repositories.ServiceOrderRepository;
import deepbluehaven.repositories.BookingRepository;
import deepbluehaven.repositories.InvoiceRepository;
import deepbluehaven.repositories.WorkerRepository;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.LogService;
import deepbluehaven.services.NotificationService;
import deepbluehaven.services.ReceptionistService;
import deepbluehaven.services.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping({"/api/vnpay", "/vnpay", "/dinio/vnpay"})
public class VnPayPaymentApiController {

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ReceptionistService receptionistService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LogService logService;

    @GetMapping("/create-deposit")
    public ResponseEntity<ApiResponse<String>> createDepositUrl(@RequestParam("bookingId") Long bookingId,
                                                                HttpServletRequest request) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Booking not found: #" + bookingId));
        }

        Booking booking = bookingOpt.get();
        BigDecimal roomCharge = BigDecimal.ZERO;
        if (booking.getDetails() != null && !booking.getDetails().isEmpty() && booking.getDetails().get(0).getSubTotal() != null) {
            roomCharge = booking.getDetails().get(0).getSubTotal();
        }
        if (roomCharge.compareTo(BigDecimal.ZERO) == 0 && booking.getTotalAmount() != null) {
            roomCharge = booking.getTotalAmount();
        }

        BigDecimal serviceTotal = BigDecimal.ZERO;
        try {
            List<ServiceOrder> orders = serviceOrderRepository.findByBookingId(booking.getId());
            if (orders != null) {
                for (ServiceOrder so : orders) {
                    if (so.getStatus() != ServiceOrderStatus.CANCELLED) {
                        serviceTotal = serviceTotal.add(so.getTotalPrice() != null ? so.getTotalPrice() : BigDecimal.ZERO);
                    }
                }
            }
        } catch (Exception ignored) {}

        BigDecimal subtotal = roomCharge.add(serviceTotal);
        BigDecimal vat = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
        BigDecimal totalWithVat = subtotal.add(vat);
        BigDecimal depositAmount = totalWithVat.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);

        String paymentUrl = vnPayService.createPaymentUrl(bookingId, depositAmount, "Thanh toan dat coc 30 percent cho booking " + bookingId, request);

        return ResponseEntity.ok(ApiResponse.success("VNPAY deposit checkout URL generated successfully", paymentUrl));
    }

    @GetMapping("/create-checkout-payment")
    public ResponseEntity<ApiResponse<String>> createCheckoutPaymentUrl(@RequestParam("bookingId") Long bookingId,
                                                                         @RequestParam(value = "amount", required = false) BigDecimal amount,
                                                                         HttpServletRequest request) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Booking not found: #" + bookingId));
        }

        Booking booking = bookingOpt.get();
        BigDecimal payAmount = amount;
        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            BigDecimal roomCharge = BigDecimal.ZERO;
            if (booking.getDetails() != null && !booking.getDetails().isEmpty() && booking.getDetails().get(0).getSubTotal() != null) {
                roomCharge = booking.getDetails().get(0).getSubTotal();
            }
            if (roomCharge.compareTo(BigDecimal.ZERO) == 0 && booking.getTotalAmount() != null) {
                roomCharge = booking.getTotalAmount();
            }

            BigDecimal serviceTotal = BigDecimal.ZERO;
            try {
                List<ServiceOrder> orders = serviceOrderRepository.findByBookingId(booking.getId());
                if (orders != null) {
                    for (ServiceOrder so : orders) {
                        if (so.getStatus() != ServiceOrderStatus.CANCELLED) {
                            serviceTotal = serviceTotal.add(so.getTotalPrice() != null ? so.getTotalPrice() : BigDecimal.ZERO);
                        }
                    }
                }
            } catch (Exception ignored) {}

            BigDecimal subtotal = roomCharge.add(serviceTotal);
            BigDecimal vat = subtotal.multiply(new BigDecimal("0.08")).setScale(0, RoundingMode.HALF_UP);
            BigDecimal totalWithVat = subtotal.add(vat);
            payAmount = totalWithVat.multiply(new BigDecimal("0.70")).setScale(0, RoundingMode.HALF_UP);
        }
        String paymentUrl = vnPayService.createPaymentUrl(bookingId, payAmount, "Thanh toan check-out cho booking " + bookingId, request);

        return ResponseEntity.ok(ApiResponse.success("VNPAY final checkout URL generated successfully", paymentUrl));
    }

    @PostMapping("/staff-confirm")
    public ResponseEntity<ApiResponse<String>> confirmBookingByStaff(@RequestParam("bookingId") Long bookingId,
                                                                      @RequestParam(value = "workerId", required = false, defaultValue = "1") Long workerId,
                                                                      HttpServletRequest request) {
        boolean success = bookingService.confirmBookingByStaff(bookingId, workerId);
        if (success) {
            logService.log(ObjectType.BOOKING, ActionCode.CONFIRM, bookingId, 
                    "Staff ID #" + workerId + " confirmed booking #" + bookingId, request.getSession(false));
            return ResponseEntity.ok(ApiResponse.success("Booking #" + bookingId + " has been confirmed by staff.", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to confirm booking #" + bookingId + ". Ensure status is PENDING."));
        }
    }

    @GetMapping({"/payment-return", "/callback"})
    public ModelAndView processPaymentReturn(@RequestParam Map<String, String> queryParams,
                                             HttpServletRequest request,
                                             RedirectAttributes redirectAttrs) {
        boolean isValid = vnPayService.validateChecksum(queryParams);
        String responseCode = queryParams.get("vnp_ResponseCode");
        String txnRef = queryParams.get("vnp_TxnRef");
        String transactionNo = queryParams.get("vnp_TransactionNo");
        String amountStr = queryParams.get("vnp_Amount");

        BigDecimal paidAmount = BigDecimal.ZERO;
        if (amountStr != null && !amountStr.isBlank()) {
            try {
                paidAmount = new BigDecimal(amountStr).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP);
            } catch (Exception ignored) {
            }
        }

        Long bookingId = null;
        if (txnRef != null && !txnRef.isBlank()) {
            try {
                String idPart = txnRef;
                if (idPart.toUpperCase().startsWith("BOOKING")) {
                    idPart = idPart.substring(7);
                }
                if (idPart.contains("_")) {
                    idPart = idPart.split("_")[0];
                }
                bookingId = Long.parseLong(idPart);
            } catch (Exception ignored) {
            }
        }

        Booking booking = null;
        if (bookingId != null) {
            booking = bookingRepository.findById(bookingId).orElse(null);
        }

        if (isValid && "00".equals(responseCode)) {
            if (booking != null) {
                if (booking.getStatus() == BookingStatus.CHECKED_IN) {
                    Worker receptionist = null;
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        receptionist = (Worker) session.getAttribute("ACTIVE_WORKER");
                        if (receptionist == null) {
                            receptionist = (Worker) session.getAttribute("worker");
                        }
                    }
                    if (receptionist == null) {
                        receptionist = workerRepository.findAll().stream().findFirst().orElse(null);
                    }

                    ReceptionistDTO.CheckOutRequest coReq = new ReceptionistDTO.CheckOutRequest();
                    coReq.setBookingId(booking.getId());
                    coReq.setPaymentMethodStr("CREDIT_CARD");
                    coReq.setNotes("Settled via VNPay Sandbox Gateway (TxnNo: " + transactionNo + ")");

                    try {
                        receptionistService.executeCheckOut(coReq, receptionist);
                    } catch (Exception e) {
                        booking.setStatus(BookingStatus.CHECKED_OUT);
                        if (booking.getDetails() != null) {
                            for (BookingDetail d : booking.getDetails()) {
                                d.setStatus(BookingStatus.CHECKED_OUT);
                            }
                        }
                        bookingRepository.save(booking);
                    }

                    if (booking.getCustomer() != null) {
                        int year = (booking.getBookingTime() != null) ? booking.getBookingTime().getYear() : LocalDate.now().getYear();
                        String code = String.format("DBH-%d-%03d", year, booking.getId());
                        notificationService.createCustomerNotification(
                                booking.getCustomer(),
                                "Check-Out & Payment Completed",
                                String.format("Your check-out and payment of %,.0f VND via VNPay for Booking %s (Txn: %s) has been completed. Thank you for staying with us!",
                                        paidAmount, code, transactionNo),
                                NotificationType.BOOKING,
                                "/booking/history"
                        );
                    }

                    logService.log(ObjectType.PAYMENT, ActionCode.PAYMENT_SUCCESS, booking.getId(),
                            "VNPay Check-Out Payment SUCCESS for Booking #" + booking.getId() + " - Amount: " + paidAmount + " VND - TxnNo: " + transactionNo,
                            request.getSession(false));

                    redirectAttrs.addFlashAttribute("successMessage",
                            String.format("Check-Out and Invoice Settlement of %,.0f VND via VNPay completed successfully for Booking #%s!", paidAmount, booking.getId()));
                    return new ModelAndView("redirect:/receptionist/check-out");

                } else if (booking.getStatus() == BookingStatus.PENDING) {
                    BookingStatus oldStatus = booking.getStatus();
                    booking.setStatus(BookingStatus.CONFIRMED);
                    if (booking.getDetails() != null) {
                        for (BookingDetail d : booking.getDetails()) {
                            d.setStatus(BookingStatus.CONFIRMED);
                        }
                    }
                    bookingRepository.save(booking);
                    logService.logBookingStatusChange(booking, oldStatus, BookingStatus.CONFIRMED, 1L,
                            "VNPay Payment SUCCESS (TxnNo: " + transactionNo + ", Amount: " + paidAmount + " VND)");

                    List<Invoice> existingInvoices = invoiceRepository.findByBookingId(booking.getId());
                    Invoice invoice;
                    if (!existingInvoices.isEmpty()) {
                        invoice = existingInvoices.get(0);
                    } else {
                        invoice = new Invoice();
                        invoice.setBooking(booking);
                        invoice.setCustomer(booking.getCustomer());
                        Worker defaultWorker = workerRepository.findAll().stream().findFirst().orElse(null);
                        invoice.setWorker(defaultWorker);
                        invoice.setTotalAmount(booking.getTotalAmount() != null ? booking.getTotalAmount() : paidAmount);
                    }

                    BigDecimal currentPaid = (invoice.getPaidAmount() != null) ? invoice.getPaidAmount() : BigDecimal.ZERO;
                    BigDecimal newPaid = currentPaid.add(paidAmount);
                    invoice.setPaidAmount(newPaid);

                    if (newPaid.compareTo(invoice.getTotalAmount()) >= 0) {
                        invoice.setStatus(InvoiceStatus.PAID);
                    } else {
                        invoice.setStatus(InvoiceStatus.PARTIAL_PAID);
                    }

                    PaymentTransaction pt = new PaymentTransaction();
                    pt.setInvoice(invoice);
                    pt.setAmount(paidAmount);
                    pt.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
                    pt.setPaymentType(newPaid.compareTo(invoice.getTotalAmount()) >= 0 ? PaymentType.FINAL : PaymentType.DEPOSIT);
                    pt.setTransactionRef(transactionNo);
                    pt.setAction("VNPay Gateway Payment: " + responseCode);
                    invoice.getPaymentTransactions().add(pt);

                    invoiceRepository.save(invoice);

                    if (booking.getCustomer() != null) {
                        int year = (booking.getBookingTime() != null) ? booking.getBookingTime().getYear() : LocalDate.now().getYear();
                        String code = String.format("DBH-%d-%03d", year, booking.getId());
                        notificationService.createCustomerNotification(
                                booking.getCustomer(),
                                "Payment Confirmed",
                                String.format("We have received your payment of %,.0f VND via VNPay for Booking %s (Txn: %s).",
                                        paidAmount, code, transactionNo),
                                NotificationType.BOOKING,
                                "/booking/history"
                        );
                    }

                    logService.log(ObjectType.PAYMENT, ActionCode.PAYMENT_SUCCESS, booking.getId(),
                            "VNPay Payment SUCCESS for Booking #" + booking.getId() + " - Amount: " + paidAmount + " VND - TxnNo: " + transactionNo,
                            request.getSession(false));

                    redirectAttrs.addFlashAttribute("successMessage",
                            String.format("Payment of %,.0f VND via VNPay completed successfully! Your booking is confirmed.", paidAmount));
                    return new ModelAndView("redirect:/booking/history");

                } else {
                    redirectAttrs.addFlashAttribute("successMessage",
                            String.format("Payment of %,.0f VND via VNPay recorded successfully!", paidAmount));
                    return new ModelAndView("redirect:/booking/history");
                }
            }

            redirectAttrs.addFlashAttribute("successMessage",
                    String.format("Payment of %,.0f VND via VNPay completed successfully!", paidAmount));
            return new ModelAndView("redirect:/booking/history");

        } else {
            logService.log(ObjectType.PAYMENT, ActionCode.CHANGE_STATUS, (bookingId != null ? bookingId : 0L),
                    "VNPay Payment FAILED or Cancelled. ResponseCode: " + responseCode + ", TxnRef: " + txnRef,
                    request.getSession(false));

            boolean isCheckOutFlow = (booking != null && booking.getStatus() == BookingStatus.CHECKED_IN);

            if ("24".equals(responseCode)) {
                redirectAttrs.addFlashAttribute("errorMessage", "VNPay payment was cancelled by user.");
            } else {
                redirectAttrs.addFlashAttribute("errorMessage", "VNPay payment failed or transaction was declined (Code: " + responseCode + ").");
            }

            if (isCheckOutFlow) {
                return new ModelAndView("redirect:/receptionist/check-out");
            } else {
                return new ModelAndView("redirect:/booking/history");
            }
        }
    }
}
