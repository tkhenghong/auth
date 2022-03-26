package com.awpghost.auth.persistence.models.relationships;

import com.arangodb.springframework.annotation.Edge;
import com.arangodb.springframework.annotation.From;
import com.arangodb.springframework.annotation.To;
import com.awpghost.auth.persistence.models.Privilege;
import com.awpghost.auth.persistence.models.Role;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Edge("role_privileges")
public class RolePrivileges {

    @Id
    private String id;

    @From
    private Role role;

    @To
    private Privilege privilege;
}
