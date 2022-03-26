package com.awpghost.auth.persistence.repositories;

import com.arangodb.springframework.repository.ArangoRepository;
import com.awpghost.auth.persistence.models.relationships.AuthRoles;

public interface AuthRolesRepository extends ArangoRepository<AuthRoles, String> {
}
