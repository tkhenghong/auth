package com.awpghost.auth.persistence.repositories;

import com.arangodb.springframework.repository.ArangoRepository;
import com.awpghost.auth.persistence.models.relationships.AuthUser;

import java.util.Optional;

public interface AuthUserRepository extends ArangoRepository<AuthUser, String> {
    Optional<AuthUser> findByUserId(String userId);

    Optional<AuthUser> findByAuth_Id(String authId);
}
