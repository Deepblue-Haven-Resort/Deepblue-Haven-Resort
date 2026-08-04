package deepbluehaven.controllers;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import deepbluehaven.dto.BookingHistoryDTO;
import deepbluehaven.services.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/booking")
public class BookingApiController {

    private final BookingService bookingService;

    public BookingApiController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createBooking(
            @RequestParam("roomId") Long roomId,
            @RequestParam(value = "checkIn", required = false) String checkInStr,
            @RequestParam(value = "checkOut", required = false) String checkOutStr,
            @RequestParam(value = "note", required = false) String note,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập tài khoản để đặt phòng", "redirectUrl", "/login"));
        }

        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        LocalDate checkIn = (checkInStr != null && !checkInStr.isBlank()) ? LocalDate.parse(checkInStr) : LocalDate.now().plusDays(1);
        LocalDate checkOut = (checkOutStr != null && !checkOutStr.isBlank()) ? LocalDate.parse(checkOutStr) : checkIn.plusDays(2);

        BookingHistoryDTO.Response bookingResp = bookingService.createRoomBooking(roomId, checkIn, checkOut, note, customerId);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đặt phòng thành công!",
            "bookingCode", bookingResp.getBookingCode(),
            "redirectUrl", "/services?bookingCode=" + bookingResp.getBookingCode()
        ));
    }

    @PostMapping("/{bookingCode}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable String bookingCode, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Vui lòng đăng nhập để thực hiện"));
        }
        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        boolean success = bookingService.cancelBooking(bookingCode, customerId);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Hủy đặt phòng thành công"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể hủy đơn đặt phòng này"));
        }
    }

    @PostMapping("/service-order/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelServiceOrder(@PathVariable Long orderId, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Vui lòng đăng nhập để thực hiện"));
        }
        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        boolean success = bookingService.cancelServiceOrder(orderId, customerId);
        if (success) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Hủy dịch vụ thành công"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Không thể hủy dịch vụ này"));
        }
    }
}
