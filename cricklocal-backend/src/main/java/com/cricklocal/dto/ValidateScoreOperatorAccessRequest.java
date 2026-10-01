package com.cricklocal.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateScoreOperatorAccessRequest(

        @NotBlank
        String accessToken,

        @NotBlank
        String securityCode
) {
}