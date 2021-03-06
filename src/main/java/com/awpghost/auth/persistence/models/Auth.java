package com.awpghost.auth.persistence.models;


import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.Relations;
import com.awpghost.auth.persistence.models.relationships.AuthRoles;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Collection;

@Data
@Builder
@Document("auth")
public class Auth extends Auditable {
    @Id
    private String id;

    @ArangoId
    private String arangoId;

    private String password;

    @Relations(edges = AuthRoles.class, lazy = true)
    private Collection<Role> roles;
}
