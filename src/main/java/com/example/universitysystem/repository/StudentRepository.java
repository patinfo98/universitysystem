package com.example.universitysystem.repository;

import com.example.universitysystem.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    List<Student> findAll();

    void deleteById(int id);
}

