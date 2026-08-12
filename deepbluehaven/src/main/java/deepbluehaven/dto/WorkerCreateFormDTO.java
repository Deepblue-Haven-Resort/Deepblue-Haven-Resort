package deepbluehaven.dto;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import deepbluehaven.pojo.enums.Department;
import deepbluehaven.pojo.enums.Gender;
import deepbluehaven.pojo.enums.PermissionTag;
import deepbluehaven.pojo.enums.Role;
import deepbluehaven.pojo.enums.WorkerStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WorkerCreateFormDTO {

    @NotNull(message = "Department is required")
    private Department department;

    @NotNull(message = "Role is required")
    private Role role;

    @NotNull(message = "Permission level is required")
    @Min(value = 1, message = "Permission level must be from 1 to 4")
    @Max(value = 4, message = "Permission level must be from 1 to 4")
    private Integer permissionLevel;

    @NotNull(message = "Account status is required")
    private WorkerStatus accountStatus = WorkerStatus.ACTIVE;

    @NotBlank(message = "Full name is required")
    @Size(max = 120, message = "Full name cannot exceed 120 characters")
    private String fullName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be before today")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Pattern(
            regexp = "^[0-9+\\s]{9,15}$",
            message = "Phone number must contain 9-15 digits"
    )
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Size(max = 120, message = "Email cannot exceed 120 characters")
    private String email;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    private String avatarUrl;

    private String employeeCode;

    private String username;

    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 72,
            message = "Password must contain 8-72 characters"
    )
    private String password;

    @NotBlank(message = "Please confirm the password")
    private String confirmPassword;

    /**
     * true: backend bỏ qua permissions gửi lên và dùng quyền mặc định của Role.
     * false: backend dùng chính xác tập permissions do admin chọn.
     */
    private boolean useDefaultPermissions = false;

    private Set<PermissionTag> permissions =
            EnumSet.noneOf(PermissionTag.class);

    @AssertTrue(message = "Please confirm the account information")
    private boolean confirmCreate;

    @AssertTrue(message = "Confirm password does not match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

    @AssertTrue(message = "Select at least one permission")
    public boolean isPermissionSelectionValid() {
        return useDefaultPermissions
                || (permissions != null && !permissions.isEmpty());
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public WorkerStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(WorkerStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isUseDefaultPermissions() {
        return useDefaultPermissions;
    }

    public void setUseDefaultPermissions(boolean useDefaultPermissions) {
        this.useDefaultPermissions = useDefaultPermissions;
    }

    public Set<PermissionTag> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<PermissionTag> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            this.permissions = EnumSet.noneOf(PermissionTag.class);
            return;
        }

        this.permissions = EnumSet.copyOf(permissions);
    }

    public boolean isConfirmCreate() {
        return confirmCreate;
    }

    public void setConfirmCreate(boolean confirmCreate) {
        this.confirmCreate = confirmCreate;
    }
}