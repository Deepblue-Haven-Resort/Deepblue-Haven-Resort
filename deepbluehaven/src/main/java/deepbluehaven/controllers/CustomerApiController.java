package deepbluehaven.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import deepbluehaven.dto.CustomerProfileDTO;
import deepbluehaven.pojo.enums.ActionCode;
import deepbluehaven.pojo.enums.ObjectType;
import deepbluehaven.services.CustomerService;
import deepbluehaven.services.DiscountService;
import deepbluehaven.services.LogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/profile")
public class CustomerApiController {

    private final CustomerService customerService;
    private final DiscountService discountService;
    private final LogService logService;

    public CustomerApiController(CustomerService customerService, 
                                 DiscountService discountService, 
                                 LogService logService) {
        this.customerService = customerService;
        this.discountService = discountService;
        this.logService = logService;
    }

    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody CustomerProfileDTO.Request requestDTO,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập để thực hiện cập nhật"));
        }

        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        CustomerProfileDTO.Response updatedProfile = customerService.updateCustomerProfile(customerId, requestDTO);

        logService.log(ObjectType.USER, ActionCode.UPDATE, customerId, 
                "Customer ID #" + customerId + " updated profile information", session);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Cập nhật thông tin cá nhân thành công!",
            "profile", updatedProfile
        ));
    }

    @PostMapping("/claim-voucher")
    public ResponseEntity<Map<String, Object>> claimVoucher(
            @RequestBody Map<String, String> payload,
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInCustomerId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Please log in to claim vouchers!"));
        }

        Long customerId = (Long) session.getAttribute("loggedInCustomerId");
        String code = payload.get("code");
        Map<String, Object> result = discountService.claimVoucher(customerId, code);

        if (Boolean.TRUE.equals(result.get("success"))) {
            logService.log(ObjectType.SYSTEM, ActionCode.CREATE, customerId, 
                    "Customer ID #" + customerId + " claimed voucher: " + code, session);
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
