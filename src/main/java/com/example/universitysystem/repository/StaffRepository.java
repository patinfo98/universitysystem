package com.example.universitysystem.repository;

import com.example.universitysystem.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff, Integer> {
    void deleteById(int id);

    Staff findByLastName(String name);
}
