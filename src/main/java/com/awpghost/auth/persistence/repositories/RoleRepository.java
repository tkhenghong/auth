package com.awpghost.auth.persistence.repositories;

import com.arangodb.springframework.repository.ArangoRepository;
import com.awpghost.auth.persistence.models.Role;

public interface RoleRepository extends ArangoRepository<Role, String> {
}
