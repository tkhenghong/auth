package com.awpghost.auth.init;

import com.arangodb.springframework.core.ArangoOperations;
import com.awpghost.auth.persistence.models.Privilege;
import com.awpghost.auth.persistence.models.Role;
import com.awpghost.auth.persistence.models.relationships.RolePrivileges;
import com.awpghost.auth.persistence.repositories.PrivilegeRepository;
import com.awpghost.auth.persistence.repositories.RolePrivilegesRepository;
import com.awpghost.auth.persistence.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;

@Component
public class InitializeRolesAndPrivileges implements CommandLineRunner {

    private final RoleRepository roleRepository;

    private final RolePrivilegesRepository rolePrivilegesRepository;

    private final PrivilegeRepository privilegeRepository;

    private final ArangoOperations arangoOperations;

    boolean alreadySetup = false;

    @Autowired
    InitializeRolesAndPrivileges(RoleRepository roleRepository,
                                 RolePrivilegesRepository rolePrivilegesRepository,
                                 PrivilegeRepository privilegeRepository,
                                 ArangoOperations arangoOperations) {
        this.roleRepository = roleRepository;
        this.rolePrivilegesRepository = rolePrivilegesRepository;
        this.privilegeRepository = privilegeRepository;
        this.arangoOperations = arangoOperations;
    }

    @Override
    public void run(String... args) {
        if (alreadySetup)
            return;

        if (ObjectUtils.isEmpty(arangoOperations.collection("roles")) ||
                arangoOperations.collection("roles").count() == 0 ||
                ObjectUtils.isEmpty(arangoOperations.collection("privileges")) ||
                arangoOperations.collection("privileges").count() == 0 ||
                ObjectUtils.isEmpty(arangoOperations.collection("role_privileges")) ||
                arangoOperations.collection("role_privileges").count() == 0) {

            // Create privileges
            Privilege readPrivilege = createPrivilege("READ");
            Privilege writePrivilege = createPrivilege("WRITE");

            List<Privilege> privilegeList = Arrays.asList(readPrivilege, writePrivilege);

            // Create roles
            Role adminRole = createRole("ROLE_ADMIN", privilegeList);
            Role userRole = createRole("ROLE_USER", privilegeList);

            List<Role> roleList = Arrays.asList(adminRole, userRole);

            // Create roles and privileges relationships
            roleList.forEach(role -> privilegeList.forEach(privilege -> createRolePrivileges(role, privilege)));
        }

        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilege(String name) {
        Privilege privilege = Privilege.builder()
                .name(name)
                .build();

        return privilegeRepository.save(privilege);
    }

    @Transactional
    Role createRole(String name, List<Privilege> privilegeList) {
        Role role = Role.builder()
                .name(name)
                .privileges(privilegeList)
                .build();

        return roleRepository.save(role);
    }

    RolePrivileges createRolePrivileges(Role role, Privilege privilege) {
        return rolePrivilegesRepository.save(RolePrivileges.builder()
                .role(role)
                .privilege(privilege)
                .build());
    }
}
