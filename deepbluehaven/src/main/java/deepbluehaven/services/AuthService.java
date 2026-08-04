package deepbluehaven.services;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.github.benmanes.caffeine.cache.Cache;

import deepbluehaven.dto.RegisterDTO;
import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.CustomerProfile;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.WorkerStatus;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.WorkerRepository;

@Service
public class AuthService {

    private final WorkerRepository workerRepository;
    private final CustomerRepository customerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Cache<String, String> otpCache;
    private final Cache<String, Boolean> verifiedOtpCache;
    private final SecureRandom secureRandom = new SecureRandom();
    private final NotificationService notificationService;
    private final TwilioVerifyService twilioVerifyService;

    public AuthService(
            WorkerRepository workerRepository,
            CustomerRepository customerRepository,
            BCryptPasswordEncoder passwordEncoder,
            @Qualifier("otpCache") Cache<String, String> otpCache,
            @Qualifier("verifiedOtpCache") Cache<String, Boolean> verifiedOtpCache,
            NotificationService notificationService,
            TwilioVerifyService twilioVerifyService) {
        this.workerRepository = workerRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpCache = otpCache;
        this.verifiedOtpCache = verifiedOtpCache;
        this.notificationService = notificationService;
        this.twilioVerifyService = twilioVerifyService;
    }

    public Worker loginWorker(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        return workerRepository.findByUsername(username.trim())
                .filter(worker -> worker.getStatus() == WorkerStatus.ACTIVE)
                .filter(worker -> passwordEncoder.matches(password, worker.getPasswordHash()))
                .orElse(null);
    }

    public Customer loginCustomer(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        return customerRepository.findByUsername(username.trim())
                .filter(customer -> passwordEncoder.matches(password, customer.getPasswordHash()))
                .orElse(null);
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[0-9]{9,10}$");

    @Transactional
    public Customer registerCustomer(RegisterDTO.Request dto) {
        if (customerRepository.existsByUsername(dto.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        Customer customer = new Customer();
        customer.setUsername(dto.getUsername());
        customer.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        CustomerProfile profile = new CustomerProfile();
        profile.setCustomer(customer);
        profile.setFullName(dto.getFullName());
        applyContact(profile, dto.getContact().trim());

        customer.setProfile(profile);

        try {
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
    }

    @Transactional
    public Customer findOrCreateCustomerFromOAuth(String email, String fullName) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google account has no email, cannot sign in.");
        }
        return customerRepository.findByUsername(email).orElseGet(() -> createOAuthCustomer(email, fullName));
    }

    private Customer createOAuthCustomer(String email, String fullName) {
        Customer customer = new Customer();
        customer.setUsername(email);
        String randomHiddenPassword = UUID.randomUUID().toString();
        customer.setPasswordHash(passwordEncoder.encode(randomHiddenPassword));
        CustomerProfile profile = new CustomerProfile();
        profile.setCustomer(customer);
        profile.setFullName((fullName == null || fullName.isBlank()) ? email : fullName);
        profile.setEmail(email);
        customer.setProfile(profile);
        try {
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
            return customerRepository.findByUsername(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Could not create or find account"));
        }
    }

    private void applyContact(
            CustomerProfile profile,
            String rawContact) {
        String contact;

        try {
            contact = normalizeIdentity(rawContact);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage());
        }

        if (EMAIL_PATTERN.matcher(contact).matches()) {
            profile.setEmail(contact);
        } else {
            profile.setPhoneNumber(contact);
        }
    }

    public void processForgotPassword(
            String rawIdentity) {
        String identity = normalizeIdentity(rawIdentity);

        boolean exists = customerRepository.existsByIdentity(identity);

        if (!exists) {
            throw new RuntimeException(
                    "Account not found. "
                            + "Please check your information.");
        }

        if (EMAIL_PATTERN.matcher(identity).matches()) {
            String otp = generateEmailOtp();

            otpCache.put(identity, otp);

            try {
                notificationService.sendEmailOtp(
                        identity,
                        otp);
            } catch (RuntimeException e) {
                otpCache.invalidate(identity);
                throw e;
            }

            return;
        }

        if (PHONE_PATTERN.matcher(identity).matches()) {
            String phoneE164 = convertVietnamPhoneToE164(identity);

            twilioVerifyService.sendSmsOtp(phoneE164);
            return;
        }

        throw new RuntimeException(
                "Invalid email or phone number format.");
    }

    public void verifyOtp(
            String rawIdentity,
            String otpCode) {
        String identity = normalizeIdentity(rawIdentity);

        validateOtpCode(otpCode);

        boolean verified;

        if (EMAIL_PATTERN.matcher(identity).matches()) {
            verified = verifyEmailOtp(
                    identity,
                    otpCode);
        } else if (PHONE_PATTERN.matcher(identity).matches()) {
            String phoneE164 = convertVietnamPhoneToE164(identity);

            verified = twilioVerifyService.verifySmsOtp(
                    phoneE164,
                    otpCode);
        } else {
            throw new RuntimeException(
                    "Invalid email or phone number format.");
        }

        if (!verified) {
            throw new RuntimeException(
                    "Invalid or expired OTP code.");
        }

        verifiedOtpCache.put(identity, true);
    }

    @Transactional
    public void resetPassword(
            String rawIdentity,
            String newPassword) {
        String identity = normalizeIdentity(rawIdentity);

        validateNewPassword(newPassword);

        Boolean verified = verifiedOtpCache.getIfPresent(identity);

        if (!Boolean.TRUE.equals(verified)) {
            throw new RuntimeException(
                    "Unauthorized request. "
                            + "Please verify OTP first.");
        }

        Customer customer = customerRepository.findByIdentity(identity)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Account not found."));

        customer.setPasswordHash(
                passwordEncoder.encode(newPassword));

        customerRepository.save(customer);

        verifiedOtpCache.invalidate(identity);
    }

    private String generateEmailOtp() {
        int value = secureRandom.nextInt(1_000_000);

        return String.format("%06d", value);
    }

    private boolean verifyEmailOtp(
            String identity,
            String otpCode) {
        String cachedOtp = otpCache.getIfPresent(identity);

        if (cachedOtp == null) {
            return false;
        }

        if (!cachedOtp.equals(otpCode)) {
            return false;
        }

        otpCache.invalidate(identity);
        return true;
    }

    private void validateOtpCode(String otpCode) {
        if (otpCode == null
                || !otpCode.matches("^[0-9]{6}$")) {
            throw new RuntimeException(
                    "OTP must contain exactly 6 digits.");
        }
    }

    private void validateNewPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new RuntimeException(
                    "New password is required.");
        }

        if (password.length() < 8) {
            throw new RuntimeException(
                    "Password must contain at least 8 characters.");
        }

        if (password.length() > 72) {
            throw new RuntimeException(
                    "Password must not exceed 72 characters.");
        }
    }

    private String normalizeIdentity(
            String rawIdentity) {
        if (rawIdentity == null
                || rawIdentity.isBlank()) {
            throw new RuntimeException(
                    "Email or phone number is required.");
        }

        String value = rawIdentity.trim();

        if (EMAIL_PATTERN.matcher(value).matches()) {
            return value.toLowerCase(Locale.ROOT);
        }

        String phone = value.replaceAll("[\\s().-]", "");

        if (phone.startsWith("+84")) {
            phone = phone.substring(3);
        } else if (phone.startsWith("84")) {
            phone = phone.substring(2);
        }

        if (phone.startsWith("0")) {
            phone = phone.replaceAll("^0+", "0");
        } else if (!phone.isEmpty()) {
            phone = "0" + phone;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new RuntimeException(
                    "Invalid Vietnamese phone number.");
        }

        return phone;
    }

    private String convertVietnamPhoneToE164(
            String localPhone) {
        if (!PHONE_PATTERN.matcher(localPhone).matches()) {
            throw new RuntimeException(
                    "Invalid Vietnamese phone number.");
        }

        return "+84" + localPhone.substring(1);
    }

}