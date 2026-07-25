package deepbluehaven.services;

import java.text.Normalizer;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import deepbluehaven.dto.WorkerCreateFormDTO;
import deepbluehaven.dto.WorkerDTO;
import deepbluehaven.pojo.Worker;
import deepbluehaven.pojo.WorkerProfile;
import deepbluehaven.pojo.WorkerRoleTag;
import deepbluehaven.pojo.enums.Department;
import deepbluehaven.pojo.enums.PermissionTag;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.pojo.enums.WorkerStatus;
import deepbluehaven.repositories.WorkerRepository;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final int PAGE_SIZE = 5; 
    
    public WorkerService(WorkerRepository workerRepository, BCryptPasswordEncoder passwordEncoder) {
            this.workerRepository = workerRepository;
            this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Worker createWorker(WorkerCreateFormDTO form) {
        validateRoleDepartment(form.getRole(), form.getDepartment());

        String email = normalizeEmail(form.getEmail());
        validateUniqueEmail(email);

        WorkerIdentity identity = buildAvailableIdentity(form.getFullName(), form.getRole());
        String username = identity.username();
        String employeeCode = identity.employeeCode();


        form.setEmployeeCode(employeeCode);
        form.setUsername(username);

        Worker worker = new Worker();
        worker.setEmployeeCode(employeeCode);
        worker.setUsername(username);
        worker.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        worker.setStatus(form.getAccountStatus());
        worker.setLocked(false);
        worker.setForceChangePassword(true);

        WorkerProfile profile = new WorkerProfile();
        profile.setWorker(worker);
        profile.setFullName(form.getFullName().trim());
        profile.setRole(form.getRole());
        profile.setRoleLevel(form.getPermissionLevel());
        profile.setPhoneNumber(form.getPhone().trim());
        profile.setEmail(email);
        profile.setDepartment(form.getDepartment());
        profile.setGender(form.getGender());
        profile.setDateOfBirth(form.getDateOfBirth());
        profile.setAddress(normalizeNullable(form.getAddress()));

        worker.setProfile(profile);

        Set<PermissionTag> permissions = resolvePermissions(form);
        String sourceDescription = form.isUseDefaultPermissions()
                        ? "Default permission of role " + form.getRole().name()
                        : "Custom permission selected at worker creation";

        for (PermissionTag permission : permissions) {
                WorkerRoleTag roleTag = new WorkerRoleTag();
                roleTag.setWorker(worker);
                roleTag.setPermissionTag(permission);
                roleTag.setDescription(sourceDescription);
                worker.getRoleTags().add(roleTag);
        }

        try {
                return workerRepository.saveAndFlush(worker);
        } catch (DataIntegrityViolationException exception) {
                throw new WorkerFormExceptionService(
                                "username",
                                "Username, employee ID or email already exists");
        }
    }

    @Transactional(readOnly = true)
    public Worker getWorker(Long workerId) {
        if (workerId == null) {
                throw new IllegalArgumentException("Worker ID cannot be null");
        }

        return workerRepository.findWithProfileAndPermissionsById(workerId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                        "Worker not found: " + workerId));
    }

    private Set<PermissionTag> resolvePermissions(WorkerCreateFormDTO form) {
        if (form.isUseDefaultPermissions()) {
                return form.getRole().getDefaultPermissions();
        }

        if (form.getPermissions() == null || form.getPermissions().isEmpty()) {
                throw new WorkerFormExceptionService(
                                "permissions",
                                "Select at least one permission");
        }

        return EnumSet.copyOf(form.getPermissions());
    }

    private void validateUniqueEmail(String email) {
        if (workerRepository.existsByProfile_EmailIgnoreCase(email)) {
                throw new WorkerFormExceptionService("email", "Email already exists");
        }
    }

    @Transactional(readOnly = true)
    public Map<String, String> generateWorkerIdentity(String fullName, Role role) {
        if (fullName == null || fullName.isBlank()) {
                throw new IllegalArgumentException("Full name is required to generate account identity");
        }
        if (role == null) {
                throw new IllegalArgumentException("Role is required to generate account identity");
        }

        WorkerIdentity identity = buildAvailableIdentity(fullName, role);
        Map<String, String> result = new LinkedHashMap<>();
        result.put("employeeCode", identity.employeeCode());
        result.put("username", identity.username());
        return result;
    }

    private WorkerIdentity buildAvailableIdentity(String fullName, Role role) {
        String employeeCode = generateEmployeeCode(role);
        String username = generateUsername(fullName, role);
        return new WorkerIdentity(employeeCode, username);
    }

    private String generateEmployeeCode(Role role) {
        String roleCode = switch (role) {
            case HOUSEKEEPER -> "HK";
            case RECEPTIONIST -> "RC";
            case MANAGER -> "MG";
            case ADMIN -> "AD";
        };

        long sequence = Math.max(workerRepository.countByProfile_Role(role) + 1, 1);
        String candidate;

        do {
                candidate = String.format(Locale.ROOT, "EMP-%s-%03d", roleCode, sequence++);
        } while (workerRepository.existsByEmployeeCodeIgnoreCase(candidate));

        return candidate;
    }

    private String generateUsername(String fullName, Role role) {
        String base = toUsernameSlug(fullName);

        if (base.isBlank()) {
                base = role.name().toLowerCase(Locale.ROOT);
        }

        base = limitLength(base, 100);
        String candidate = base;
        int suffix = 2;

        while (workerRepository.existsByUsernameIgnoreCase(candidate)) {
                String suffixText = "." + suffix++;
                candidate = limitLength(base, 100 - suffixText.length()) + suffixText;
        }

        return candidate;
    }

    private String toUsernameSlug(String fullName) {
        String normalized = Normalizer.normalize(fullName.trim(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}+", "")
                        .replace('đ', 'd')
                        .replace('Đ', 'D')
                        .toLowerCase(Locale.ROOT);

        return normalized
                        .replaceAll("[^a-z0-9]+", ".")
                        .replaceAll("\\.{2,}", ".")
                        .replaceAll("^\\.|\\.$", "");
    }

    private String limitLength(String value, int maxLength) {
        if (value.length() <= maxLength) {
                return value;
        }

        String limited = value.substring(0, maxLength);
        return limited.replaceAll("\\.$", "");
    }

    private record WorkerIdentity(String employeeCode, String username) {
    }

    private void validateRoleDepartment(Role role, Department department) {
        Department expectedDepartment = switch (role) {
            case HOUSEKEEPER -> Department.HOUSEKEEPING;
            case RECEPTIONIST -> Department.RECEPTION;
            case MANAGER -> Department.MANAGEMENT;
            case ADMIN -> Department.ADMINISTRATION;
        };

        if (department != expectedDepartment) {
            throw new WorkerFormExceptionService("department", "Department does not match selected role");
        }
    }

    private String normalizeEmail(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
                return null;
        }
        return value.trim();
    }

    private Worker getWorkerWithPermissions(Long workerId) {
        if (workerId == null) {
            throw new IllegalArgumentException("Worker id không được null");
        }
        return workerRepository.findWithPermissionsById(workerId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy worker id " + workerId));
    }

    @Transactional
    public void grantPermission(Long workerId, PermissionTag permission) {
        Worker worker = getWorkerWithPermissions(workerId);

        worker.addPermission(permission, "Permission được cấp thủ công");
    }

    @Transactional
    public void revokePermission(Long workerId, PermissionTag permission) {
        Worker worker = getWorkerWithPermissions(workerId);
        worker.removePermission(permission);
    }
    
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalAccounts", workerRepository.count());
        stats.put("active", workerRepository.countByStatus(WorkerStatus.ACTIVE));
        stats.put("employees", workerRepository.countNonAdmin());
        stats.put("locked", workerRepository.countByStatus(WorkerStatus.LOCKED));
        return stats;
    }

    public Map<String, Long> getRoleDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Role role : Role.values()) {
        distribution.put(role.name(), workerRepository.countByProfile_Role(role));
        }
        return distribution;
    }

    public Map<String, Object> listAccounts(String search, Role role, WorkerStatus status, int page) {
        Page<Worker> result = workerRepository.search(
                (search == null || search.isBlank()) ? null : search.trim(), role, status, PageRequest.of(Math.max(page, 0), PAGE_SIZE)
        );

        List<WorkerDTO.Response> items = result.getContent().stream().map(this::toResponse).toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("totalItems", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        body.put("currentPage", result.getNumber());
        return body;
    }

    @Transactional
    public WorkerDTO.Response setStatus(Long workerId, WorkerStatus newStatus) {
        Worker worker = workerRepository.findById(workerId) .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
        worker.setStatus(newStatus);
        return toResponse(workerRepository.save(worker));
    }

    @Transactional
    public void deleteWorker(Long workerId) {
        Worker worker = workerRepository.findById(workerId) .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Worker not found"));
        worker.setStatus(WorkerStatus.INACTIVE);
        workerRepository.save(worker);
    }
    

    private WorkerDTO.Response toResponse(Worker worker) {
        WorkerDTO.Response dto = new WorkerDTO.Response();
        dto.setId(worker.getId());
        dto.setUsername(worker.getUsername());
        dto.setStatus(worker.getStatus());
        dto.setCreatedAt(worker.getCreatedAt());

        if (worker.getProfile() != null) {
            dto.setFullName(worker.getProfile().getFullName());
            dto.setRole(worker.getProfile().getRole() != null ? worker.getProfile().getRole().name() : null);
            dto.setPhoneNumber(worker.getProfile().getPhoneNumber());
            dto.setEmail(worker.getProfile().getEmail());
            dto.setRoleLevel(worker.getProfile().getRoleLevel());
        }
        return dto;
    }
}