package com.nasya.ecommerce.repository;

import com.nasya.ecommerce.entity.UserRole;
import com.nasya.ecommerce.entity.UserRole.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query(value = """
    SELECT * FROM user_role
        WHERE user_id = :userId
    """, nativeQuery = true)
    List<UserRole> findByUserId(Long userId);
}
