package com.kompu.api.usecase.user;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.kompu.api.entity.role.exception.RoleNotFoundException;
import com.kompu.api.entity.role.gateway.RoleGateway;
import com.kompu.api.entity.role.model.RoleModel;
import com.kompu.api.entity.user.gateway.UserGateway;
import com.kompu.api.entity.user.model.UserAccountModel;

/**
 * AssignUserRoleUseCase orchestrates the assignment of roles to users.
 * 
 * This use case handles:
 * 1. Finding the user account
 * 2. Finding the role to assign
 * 3. Adding the role to the user's roles collection
 * 4. Persisting the updated user
 * 
 * Responsibilities:
 * - Validate user and role existence
 * - Prevent duplicate role assignments
 * - Update user with new role(s)
 * - Inherit permissions from role to user
 * 
 * This class is completely framework-agnostic and contains pure business logic.
 */
public class AssignUserRoleUseCase {

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;

    /**
     * Constructor with gateway dependency injection
     * 
     * @param userGateway the gateway for user persistence
     * @param roleGateway the gateway for role persistence
     */
    public AssignUserRoleUseCase(UserGateway userGateway, RoleGateway roleGateway) {
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
    }

    /**
     * Assign a role to a user by role ID.
     * 
     * @param userId the user's ID
     * @param roleId the role's ID
     * @return the updated user with the new role assigned
     * @throws com.kompu.api.entity.user.exception.UserNotFoundException if user not
     *                                                                   found
     * @throws RoleNotFoundException                                     if role not
     *                                                                   found
     */
    public UserAccountModel assignRoleToUser(UUID userId, UUID roleId) {
        // Find the user
        UserAccountModel user = userGateway
                .findById(userId)
                .orElseThrow(com.kompu.api.entity.user.exception.UserNotFoundException::new);

        // Find the role
        RoleModel role = roleGateway
                .findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(
                        "Role with ID " + roleId + " not found"));

        // Check if user already has this role (prevent duplicates)
        boolean alreadyAssigned = user.getRoles().stream()
                .anyMatch(r -> r.getId().equals(roleId));

        if (alreadyAssigned) {
            // Role already assigned, return user as-is
            return user;
        }

        // Add role to user's roles collection
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);

        // Persist the updated user
        return userGateway.update(user);
    }

    /**
     * Assign multiple roles to a user.
     * 
     * @param userId  the user's ID
     * @param roleIds the IDs of roles to assign
     * @return the updated user with all roles assigned
     * @throws com.kompu.api.entity.user.exception.UserNotFoundException if user not
     *                                                                   found
     * @throws RoleNotFoundException                                     if any role
     *                                                                   is not
     *                                                                   found
     */
    public UserAccountModel assignRolesToUser(UUID userId, Set<UUID> roleIds) {
        // Find the user
        UserAccountModel user = userGateway
                .findById(userId)
                .orElseThrow(com.kompu.api.entity.user.exception.UserNotFoundException::new);

        // Initialize roles collection if null
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        // Get all existing role IDs for the user
        Set<UUID> existingRoleIds = new HashSet<>();
        for (RoleModel role : user.getRoles()) {
            existingRoleIds.add(role.getId());
        }

        // Assign each role that isn't already assigned
        for (UUID roleId : roleIds) {
            // Skip if already assigned
            if (existingRoleIds.contains(roleId)) {
                continue;
            }

            // Find the role
            RoleModel role = roleGateway
                    .findById(roleId)
                    .orElseThrow(() -> new RoleNotFoundException(
                            "Role with ID " + roleId + " not found"));

            // Add to user's roles
            user.getRoles().add(role);
        }

        // Persist the updated user
        return userGateway.update(user);
    }

    /**
     * Assign a default role to a user (e.g., "User" or "Member" role).
     * 
     * @param userId   the user's ID
     * @param tenantId the tenant context
     * @return the updated user with default role assigned
     * @throws com.kompu.api.entity.user.exception.UserNotFoundException if user not
     *                                                                   found
     * @throws RoleNotFoundException                                     if default
     *                                                                   role not
     *                                                                   found for
     *                                                                   tenant
     */
    public UserAccountModel assignDefaultRoleToUser(UUID userId, UUID tenantId) {
        // Find the user
        UserAccountModel user = userGateway
                .findById(userId)
                .orElseThrow(com.kompu.api.entity.user.exception.UserNotFoundException::new);

        // Find the default "Member" or "User" role for the tenant
        RoleModel defaultRole = roleGateway
                .findByTenantIdAndName(tenantId, "Member")
                .or(() -> roleGateway.findByTenantIdAndName(tenantId, "User"))
                .orElseThrow(() -> new RoleNotFoundException(
                        "Default role not found for tenant " + tenantId));

        // Check if user already has this role
        boolean alreadyAssigned = user.getRoles().stream()
                .anyMatch(r -> r.getId().equals(defaultRole.getId()));

        if (alreadyAssigned) {
            return user;
        }

        // Initialize roles collection if null
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        // Add default role
        user.getRoles().add(defaultRole);

        // Persist the updated user
        return userGateway.update(user);
    }
}
