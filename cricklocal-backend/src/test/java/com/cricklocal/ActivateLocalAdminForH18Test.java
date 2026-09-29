package com.cricklocal;

import com.cricklocal.entity.User;
import com.cricklocal.service.AdminAccessService;
import com.cricklocal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
class ActivateLocalAdminForH18Test {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminAccessService adminAccessService;

    @Test
    void activateLocalAdmin() {

        User user = userRepository
                .findByEmail("techvizag2017@gmail.com")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Local development user not found"
                        )
                );

        adminAccessService.activateAdminAccess(
                user,
                Instant.now(),
                null
        );

        System.out.println(
                "LOCAL ADMIN ACCESS ACTIVATED FOR USER: "
                        + user.getEmail()
        );
    }
}