package deepbluehaven.services;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

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
    private final NotificationService notificationService;

    public AuthService(WorkerRepository workerRepository, CustomerRepository customerRepository, BCryptPasswordEncoder passwordEncoder, Cache<String, String> otpCache,
                       Cache<String, Boolean> verifiedOtpCache, NotificationService notificationService) {
        this.workerRepository = workerRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpCache = otpCache;
        this.verifiedOtpCache = verifiedOtpCache;
        this.notificationService = notificationService;
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

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
 
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[0-9]{9,15}$");

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
        return customerRepository.findByUsername(email) .orElseGet(() -> createOAuthCustomer(email, fullName));
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
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not create or find account"));
        }
    }
 
    private void applyContact(CustomerProfile profile, String contact) {
        if (EMAIL_PATTERN.matcher(contact).matches()) {
            profile.setEmail(contact);
        } else if (PHONE_PATTERN.matcher(contact).matches()) {
            profile.setPhoneNumber(contact);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email or phone number format");
        }
    }

    public void processForgotPassword(String identity) {
        boolean exists = customerRepository.existsByIdentity(identity);
        if (!exists) {
            throw new RuntimeException("Account not found. Please check your information.");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpCache.put(identity, otp);

        if (EMAIL_PATTERN.matcher(identity).matches()) {
            notificationService.sendEmailOtp(identity, otp);
        } 
        // else if (PHONE_PATTERN.matcher(identity).matches()) {
        //      notificationService.sendSmsOtp(identity, otp);
        // } 
        else {
            throw new RuntimeException("Invalid identity format.");
        }
    }

    public void verifyOtp(String identity, String otpCode) {
        String cachedOtp = otpCache.getIfPresent(identity);
        
        if (cachedOtp == null) {
            throw new RuntimeException("OTP has expired or not generated.");
        }
        if (!cachedOtp.equals(otpCode)) {
            throw new RuntimeException("Invalid OTP code.");
        }
        
        otpCache.invalidate(identity); 
        verifiedOtpCache.put(identity, true); 
    }

    @Transactional
    public void resetPassword(String identity, String newPassword) {
        Boolean isVerified = verifiedOtpCache.getIfPresent(identity);
        
        if (isVerified == null || !isVerified) {
            throw new RuntimeException("Unauthorized request. Please verify OTP first.");
        }

       Customer customer = customerRepository.findByIdentity(identity)
            .orElseThrow(() -> new RuntimeException("Account not found"));

        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        verifiedOtpCache.invalidate(identity); 
    }
    
}