package deepbluehaven.pojo.enums;

import java.util.EnumSet;
import java.util.Set;

public enum Role {

    ADMIN(EnumSet.allOf(PermissionTag.class)),

    MANAGER(EnumSet.of(
            PermissionTag.VIEW_BOOKING,
            PermissionTag.CREATE_BOOKING,
            PermissionTag.CANCEL_BOOKING,
            PermissionTag.CHECK_IN,
            PermissionTag.CHECK_OUT,
            PermissionTag.ASSIGN_ROOM,
            PermissionTag.CREATE_SERVICE_ORDER,
            PermissionTag.PROCESS_SERVICE_ORDER,
            PermissionTag.CREATE_INVOICE,
            PermissionTag.PROCESS_PAYMENT,
            PermissionTag.APPLY_DISCOUNT,
            PermissionTag.VIEW_TASK,
            PermissionTag.UPDATE_TASK_STATUS,
            PermissionTag.MANAGE_ROOM,
            PermissionTag.MANAGE_SERVICE,
            PermissionTag.MANAGE_INVENTORY,
            PermissionTag.MANAGE_DISCOUNT,
            PermissionTag.MANAGE_PRICING,
            PermissionTag.MANAGE_MEMBERSHIP,
            PermissionTag.VIEW_REPORT,
            PermissionTag.MANAGE_COMMENT
    )),

    RECEPTIONIST(EnumSet.of(
            PermissionTag.VIEW_BOOKING,
            PermissionTag.CREATE_BOOKING,
            PermissionTag.CANCEL_BOOKING,
            PermissionTag.CHECK_IN,
            PermissionTag.CHECK_OUT,
            PermissionTag.ASSIGN_ROOM,
            PermissionTag.CREATE_SERVICE_ORDER,
            PermissionTag.CREATE_INVOICE,
            PermissionTag.PROCESS_PAYMENT,
            PermissionTag.APPLY_DISCOUNT,
            PermissionTag.VIEW_TASK,
            PermissionTag.MANAGE_COMMENT
    )),

    HOUSEKEEPER(EnumSet.of(
            PermissionTag.VIEW_TASK,
            PermissionTag.UPDATE_TASK_STATUS
    ));

    private final Set<PermissionTag> defaultPermissions;

    Role(Set<PermissionTag> defaultPermissions) {
        this.defaultPermissions = Set.copyOf(defaultPermissions);
    }

    public Set<PermissionTag> getDefaultPermissions() {
        return defaultPermissions;
    }
}