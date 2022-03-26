package com.awpghost.auth.persistence.repositories;

import com.arangodb.springframework.repository.ArangoRepository;
import com.awpghost.auth.persistence.models.Privilege;

public interface PrivilegeRepository extends ArangoRepository<Privilege, String> {
}
