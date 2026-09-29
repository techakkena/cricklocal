package com.cricklocal.config;

import com.cricklocal.entity.User;
import com.cricklocal.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    public OAuth2LoginSuccessHandler(
            AuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        User user = authenticationService.processGoogleLogin(oauth2User);

        if (!Boolean.TRUE.equals(user.getActive())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "CricketLocal user account is inactive"
            );
            return;
        }

        response.sendRedirect("http://localhost:5173/login/success");
    }
}