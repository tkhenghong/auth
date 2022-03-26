package com.awpghost.auth.persistence.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

@Data
@Builder
@RedisHash("auth_token")
public class AuthToken extends Auditable {
    private String token;
    private String username;
    private String ip;
    private long expires;
}
