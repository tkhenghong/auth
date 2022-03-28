package com.awpghost.auth.persistence.models.relationships;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import com.awpghost.auth.persistence.models.Auth;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Edge
public class AuthUser {
    @From
    private Auth auth;

    // Linked to User object in User microservice.
    @To
    private String userId;
}
