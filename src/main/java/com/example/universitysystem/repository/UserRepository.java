package com.example.universitysystem.repository;

import com.example.universitysystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    User findById(int id);

    User deleteById(int id);
}

