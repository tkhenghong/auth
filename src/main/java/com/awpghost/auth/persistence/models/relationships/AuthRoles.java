package com.awpghost.auth.persistence.models.relationships;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import com.awpghost.auth.persistence.models.Auth;
import com.awpghost.auth.persistence.models.Role;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Edge("auth_roles")
public class AuthRoles {
    @Id
    private String id;

    @From
    private Auth auth;

    @To
    private Role role;
}
