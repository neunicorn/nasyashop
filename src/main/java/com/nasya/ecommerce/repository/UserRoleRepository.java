package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.UserRole;
import com.nasya.ecommerce.entity.UserRole.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
