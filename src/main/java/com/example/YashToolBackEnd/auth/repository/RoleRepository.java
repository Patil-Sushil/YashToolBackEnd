package com.example.YashToolBackEnd.auth.repository;

import com.example.YashToolBackEnd.auth.entity.enums.RoleName;
import com.example.YashToolBackEnd.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(RoleName name);

}
