package com.awpghost.auth.dto.responses;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class AccessToken {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private ZonedDateTime expiresIn;
}
