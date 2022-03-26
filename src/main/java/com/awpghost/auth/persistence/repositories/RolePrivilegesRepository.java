package com.awpghost.auth.persistence.repositories;

import com.arangodb.springframework.repository.ArangoRepository;
import com.awpghost.auth.persistence.models.relationships.RolePrivileges;

public interface RolePrivilegesRepository extends ArangoRepository<RolePrivileges, String> {
}
