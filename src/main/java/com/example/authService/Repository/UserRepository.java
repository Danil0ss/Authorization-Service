package com.example.authService.Repository;

import com.example.authService.Entity.Role;
import com.example.authService.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findUserByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.isActive =:isActive " +
            " WHERE u.id=:id")
    void changeIsActiveStatus(@Param("id") UUID id,@Param("isActive") Boolean isActive);
}
