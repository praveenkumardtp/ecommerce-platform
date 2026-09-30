package com.portfolio.ecommerce.repository;

import com.portfolio.ecommerce.entity.Role;
import com.portfolio.ecommerce.entity.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
