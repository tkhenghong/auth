package com.awpghost.auth.persistence.models;

import com.arangodb.springframework.annotation.ArangoId;
import com.arangodb.springframework.annotation.Document;
import com.arangodb.springframework.annotation.Relations;
import com.awpghost.auth.persistence.models.relationships.RolePrivileges;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Collection;

@Data
@Builder
@Document("roles")
public class Role extends Auditable {
    @Id
    private String id;

    @ArangoId
    private String arangoId;

    private String name;

    @Relations(edges = RolePrivileges.class, lazy = true)
    private Collection<Privilege> privileges;
}
