package deepbluehaven.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.WorkerStatus;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.WorkerRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Service
public class RememberMeService {

    private static final Logger logger = LoggerFactory.getLogger(RememberMeService.class);

    public static final String CUSTOMER_COOKIE_NAME = "REMEMBER_CUSTOMER_TOKEN";
    public static final String WORKER_COOKIE_NAME = "REMEMBER_WORKER_TOKEN";
    private static final int COOKIE_MAX_AGE_SECONDS = 30 * 24 * 60 * 60;  
    private static final String SECRET_KEY = "DeepBlueHavenResortSecretKeyForRememberMeTokenSignature!2026";

    private final CustomerRepository customerRepository;
    private final WorkerRepository workerRepository;

    public RememberMeService(CustomerRepository customerRepository, WorkerRepository workerRepository) {
        this.customerRepository = customerRepository;
        this.workerRepository = workerRepository;
    }

    public void createRememberCustomerCookie(HttpServletResponse response, String contextPath, Long customerId) {
        createRememberCookie(response, contextPath, CUSTOMER_COOKIE_NAME, "CUSTOMER", customerId);
    }

    public void createRememberWorkerCookie(HttpServletResponse response, String contextPath, Long workerId) {
        createRememberCookie(response, contextPath, WORKER_COOKIE_NAME, "WORKER", workerId);
    }

    private void createRememberCookie(HttpServletResponse response, String contextPath, String cookieName, String userType, Long userId) {
        try {
            long expiryTime = System.currentTimeMillis() + (COOKIE_MAX_AGE_SECONDS * 1000L);
            String payload = userType + ":" + userId + ":" + expiryTime;
            String signature = hmacSha256(payload, SECRET_KEY);
            String rawToken = payload + "::" + signature;
            String encodedToken = Base64.getUrlEncoder().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));

            Cookie cookie = new Cookie(cookieName, encodedToken);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
            cookie.setPath(contextPath != null && !contextPath.isBlank() ? contextPath : "/");
            response.addCookie(cookie);

            logger.info("Created remember-me cookie [{}] for {} ID: {}", cookieName, userType, userId);
        } catch (Exception e) {
            logger.error("Failed to create remember-me cookie for {} ID: {}", userType, userId, e);
        }
    }

    public boolean tryAutoLoginFromCookie(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);

        if (session != null && (session.getAttribute("loggedInCustomerId") != null || session.getAttribute("loggedInWorkerId") != null)) {
            return true;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return false;
        }

        for (Cookie cookie : cookies) {
            if (CUSTOMER_COOKIE_NAME.equals(cookie.getName())) {
                if (processCustomerAutoLogin(request, cookie.getValue())) {
                    return true;
                }
            } else if (WORKER_COOKIE_NAME.equals(cookie.getName())) {
                if (processWorkerAutoLogin(request, cookie.getValue())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean processCustomerAutoLogin(HttpServletRequest request, String tokenValue) {
        Long customerId = validateTokenAndExtractUserId("CUSTOMER", tokenValue);
        if (customerId == null) {
            return false;
        }

        Optional<Customer> optCustomer = customerRepository.findById(customerId);
        if (optCustomer.isEmpty()) {
            return false;
        }

        Customer customer = optCustomer.get();
        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInCustomerId", customer.getId());
        session.setAttribute("customerName", customer.getProfile() != null ? customer.getProfile().getFullName() : customer.getUsername());
        session.setMaxInactiveInterval(60 * 60 * 8);

        logger.info("Auto-logged in CUSTOMER ID: {} via Remember-Me token", customer.getId());
        return true;
    }

    private boolean processWorkerAutoLogin(HttpServletRequest request, String tokenValue) {
        Long workerId = validateTokenAndExtractUserId("WORKER", tokenValue);
        if (workerId == null) {
            return false;
        }

        Optional<Worker> optWorker = workerRepository.findById(workerId);
        if (optWorker.isEmpty()) {
            return false;
        }

        Worker worker = optWorker.get();
        if (worker.getStatus() != WorkerStatus.ACTIVE) {
            logger.warn("Remember-Me login rejected: Worker ID {} status is {}", worker.getId(), worker.getStatus());
            return false;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("loggedInWorkerId", worker.getId());
        session.setAttribute("workerRole", worker.getProfile().getRole().name());
        session.setAttribute("workerName", worker.getProfile().getFullName());
        session.setMaxInactiveInterval(60 * 60 * 8);

        logger.info("Auto-logged in WORKER ID: {} ({}) via Remember-Me token", worker.getId(), worker.getProfile().getRole());
        return true;
    }

    private Long validateTokenAndExtractUserId(String expectedType, String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(tokenValue);
            String raw = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = raw.split("::");
            if (parts.length != 2) {
                return null;
            }

            String payload = parts[0];
            String expectedSignature = parts[1];

            String actualSignature = hmacSha256(payload, SECRET_KEY);
            if (!MessageDigest.isEqual(actualSignature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                logger.warn("Invalid remember-me token signature");
                return null;
            }

            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 3) {
                return null;
            }

            String userType = payloadParts[0];
            Long userId = Long.parseLong(payloadParts[1]);
            long expiryTime = Long.parseLong(payloadParts[2]);

            if (!expectedType.equals(userType)) {
                return null;
            }

            if (System.currentTimeMillis() > expiryTime) {
                logger.info("Remember-Me token for {} ID: {} has expired", userType, userId);
                return null;
            }

            return userId;
        } catch (Exception e) {
            logger.debug("Failed to validate remember-me token: {}", e.getMessage());
            return null;
        }
    }

    public void clearRememberMeCookies(HttpServletResponse response, String contextPath) {
        clearCookie(response, contextPath, CUSTOMER_COOKIE_NAME);
        clearCookie(response, contextPath, WORKER_COOKIE_NAME);
    }

    private void clearCookie(HttpServletResponse response, String contextPath, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath(contextPath != null && !contextPath.isBlank() ? contextPath : "/");
        response.addCookie(cookie);
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }
}
