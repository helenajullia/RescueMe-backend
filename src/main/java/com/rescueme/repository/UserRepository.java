package com.rescueme.repository;

import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.ShelterStatus;
import com.rescueme.repository.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByRole(Role role);

    Long countByRole(Role role);

    Long countByRoleAndStatus(Role role, ShelterStatus status);

}
