package com.awpghost.auth.persistence.repositories;

import com.arangodb.springframework.repository.ArangoRepository;
import com.awpghost.auth.persistence.models.Auth;

public interface AuthRepository extends ArangoRepository<Auth, String> {
    Auth findByEmail(String email);
}
