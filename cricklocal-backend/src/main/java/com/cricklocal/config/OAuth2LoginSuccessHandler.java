package com.cricklocal.config;

import com.cricklocal.entity.User;
import com.cricklocal.entity.UserRole; 
import com.cricklocal.entity.UserAccountStatus;
import com.cricklocal.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(
            AuthenticationService authenticationService,
            @Value("${app.frontend.url}") String frontendUrl
    ) {
        this.authenticationService = authenticationService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        User user = authenticationService.processGoogleLogin(oauth2User);

        if (user.getAccountStatus() == UserAccountStatus.BLOCKED
                || !Boolean.TRUE.equals(user.getActive())) {

            SecurityContextHolder.clearContext();

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "CricketLocal user account is blocked"
            );
            return;
        }

        if (!(authentication instanceof OAuth2AuthenticationToken oauth2Authentication)) {
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Invalid OAuth2 authentication"
            );
            return;
        }

        SimpleGrantedAuthority roleAuthority =
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                );

        OAuth2AuthenticationToken updatedAuthentication =
                new OAuth2AuthenticationToken(
                        oauth2User,
                        List.of(roleAuthority),
                        oauth2Authentication.getAuthorizedClientRegistrationId()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(updatedAuthentication);

        response.sendRedirect(frontendUrl + "/login/success");
    }
}