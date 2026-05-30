package deepbluehaven.services;

import deepbluehaven.pojo.Customer;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.enums.WorkerStatus;
import deepbluehaven.repositories.CustomerRepository;
import deepbluehaven.repositories.WorkerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
}