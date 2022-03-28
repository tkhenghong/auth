package com.awpghost.auth.persistence.repositories;

import com.arangodb.springframework.repository.ArangoRepository;
import com.awpghost.auth.persistence.models.Role;

import java.util.Optional;

public interface RoleRepository extends ArangoRepository<Role, String> {
    Optional<Role> findByName(String name);
}
