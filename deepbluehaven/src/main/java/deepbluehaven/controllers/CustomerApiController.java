package deepbluehaven.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import deepbluehaven.dto.CustomerProfileDTO;
import deepbluehaven.services.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/profile")
public class CustomerApiController {

    private final CustomerService customerService;

    public CustomerApiController(CustomerService customerService) {
        this.customerService = customerService;
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

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Cập nhật thông tin cá nhân thành công!",
            "profile", updatedProfile
        ));
    }
}
