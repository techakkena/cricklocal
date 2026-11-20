package com.cricklocal.config;

import com.cricklocal.service.AccountStatusService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AccountStatusAuthorizationFilter extends OncePerRequestFilter {

    private final AccountStatusService accountStatusService;

    public AccountStatusAuthorizationFilter(
            AccountStatusService accountStatusService) {
        this.accountStatusService = accountStatusService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof OAuth2User oauth2User) {

            String providerUserId =
                    oauth2User.getAttribute("sub");

            if (providerUserId != null
                    && !providerUserId.isBlank()
                    && accountStatusService.isBlocked(providerUserId)) {

                SecurityContextHolder.clearContext();

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "CricketLocal user account is blocked"
                );

                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}