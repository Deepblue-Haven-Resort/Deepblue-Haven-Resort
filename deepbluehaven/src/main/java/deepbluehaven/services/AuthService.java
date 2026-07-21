package deepbluehaven.services;

import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    public AuthService(WorkerRepository workerRepository,
                       CustomerRepository customerRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.workerRepository = workerRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
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
 
    private void applyContact(CustomerProfile profile, String contact) {
        if (EMAIL_PATTERN.matcher(contact).matches()) {
            profile.setEmail(contact);
        } else if (PHONE_PATTERN.matcher(contact).matches()) {
            profile.setPhoneNumber(contact);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email or phone number format");
        }
    }
}