package com.awpghost.auth.dto.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessToken {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}
