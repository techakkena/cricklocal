package com.cricklocal.service;

import com.cricklocal.entity.User;
import com.cricklocal.entity.UserAccountStatus;
import com.cricklocal.entity.UserRole;
import com.cricklocal.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminAccessService adminAccessService;

    public AdminUserService(
            UserRepository userRepository,
            AdminAccessService adminAccessService
    ) {
        this.userRepository = userRepository;
        this.adminAccessService = adminAccessService;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User blockUser(Long userId) {

        User user = getUser(userId);

        user.setAccountStatus(UserAccountStatus.BLOCKED);
        user.setActive(false);

        return userRepository.save(user);
    }

    @Transactional
    public User activateUser(Long userId) {

        User user = getUser(userId);

        user.setAccountStatus(UserAccountStatus.ACTIVE);
        user.setActive(true);

        return userRepository.save(user);
    }

    @Transactional
    public User approveAdmin(Long userId) {

        User user = getUser(userId);

        if (user.getAccountStatus() == UserAccountStatus.BLOCKED
                || !Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException(
                    "Blocked user cannot be approved as ADMIN"
            );
        }

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new IllegalStateException(
                    "User does not have an active admin entitlement"
            );
        }

        user.setRole(UserRole.ADMIN);

        return userRepository.save(user);
    }

    private User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );
    }
}