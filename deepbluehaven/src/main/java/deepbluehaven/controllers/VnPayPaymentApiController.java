package deepbluehaven.controllers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import deepbluehaven.dto.ApiResponse;
import deepbluehaven.pojo.Booking;
import deepbluehaven.repositories.BookingRepository;
import deepbluehaven.services.BookingService;
import deepbluehaven.services.VnPayService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping({"/api/vnpay", "/vnpay", "/dinio/vnpay"})
public class VnPayPaymentApiController {

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/create-deposit")
    public ResponseEntity<ApiResponse<String>> createDepositUrl(@RequestParam("bookingId") Long bookingId,
                                                                HttpServletRequest request) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Booking not found: #" + bookingId));
        }

        Booking booking = bookingOpt.get();
        BigDecimal totalAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.valueOf(500000);
        BigDecimal depositAmount = totalAmount.multiply(new BigDecimal("0.30")); // 30% deposit
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
            BigDecimal totalAmount = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.valueOf(500000);
            payAmount = totalAmount.multiply(new BigDecimal("0.70")); // 70% remaining balance
        }
        String paymentUrl = vnPayService.createPaymentUrl(bookingId, payAmount, "Thanh toan check-out cho booking " + bookingId, request);

        return ResponseEntity.ok(ApiResponse.success("VNPAY final checkout URL generated successfully", paymentUrl));
    }

    @PostMapping("/staff-confirm")
    public ResponseEntity<ApiResponse<String>> confirmBookingByStaff(@RequestParam("bookingId") Long bookingId,
                                                                      @RequestParam(value = "workerId", required = false, defaultValue = "1") Long workerId) {
        boolean success = bookingService.confirmBookingByStaff(bookingId, workerId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Booking #" + bookingId + " has been confirmed by staff.", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to confirm booking #" + bookingId + ". Ensure status is PENDING."));
        }
    }

    @GetMapping({"/payment-return", "/callback"})
    public ResponseEntity<ApiResponse<Map<String, String>>> processPaymentReturn(@RequestParam Map<String, String> queryParams) {
        boolean isValid = vnPayService.validateChecksum(queryParams);
        String responseCode = queryParams.get("vnp_ResponseCode");

        Map<String, String> result = new HashMap<>();
        result.put("vnp_ResponseCode", responseCode);
        result.put("vnp_TxnRef", queryParams.get("vnp_TxnRef"));
        result.put("vnp_TransactionNo", queryParams.get("vnp_TransactionNo"));

        if (isValid && "00".equals(responseCode)) {
            result.put("status", "PAID");
            return ResponseEntity.ok(ApiResponse.success("Payment completed successfully", result));
        } else {
            result.put("status", "FAILED");
            return ResponseEntity.badRequest().body(ApiResponse.error("Payment failed or invalid checksum"));
        }
    }
}
