package com.example.universitysystem.repository;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.firstLogin = true")
    void updateFirstLogin();

    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findById(int id);

    User deleteById(int id);
}

