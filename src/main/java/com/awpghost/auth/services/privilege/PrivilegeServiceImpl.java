package com.awpghost.auth.services.privilege;

import com.awpghost.auth.persistence.repositories.PrivilegeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class PrivilegeServiceImpl implements PrivilegeService {
    private final PrivilegeRepository privilegeRepository;

    @Autowired
    PrivilegeServiceImpl(@Lazy PrivilegeRepository privilegeRepository) {
        this.privilegeRepository = privilegeRepository;
    }
}
