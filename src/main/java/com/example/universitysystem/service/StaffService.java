package com.example.universitysystem.service;

import com.example.universitysystem.model.Staff;
import com.example.universitysystem.repository.StaffRepository;
import com.example.universitysystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    public StaffService(StaffRepository staffRepository, UserRepository userRepository) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }

    public List<Staff> findAllStaff() {
        return staffRepository.findAll();
    }

    public void deleteById(int id) {
        staffRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    public Staff findByLastName(String name) {
        return staffRepository.findByLastName(name);
    }
}
