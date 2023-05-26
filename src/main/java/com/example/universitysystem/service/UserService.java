package com.example.universitysystem.service;

import com.example.universitysystem.model.Staff;
import com.example.universitysystem.model.Student;
import com.example.universitysystem.model.User;
import com.example.universitysystem.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean loginValid(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            User user = userRepository.findByEmail(email);
            return password.equals(user.getPassword());
        }
        return false;
    }

    public void save(User user){
        user.setFirstLogin(false);
        userRepository.save(user);
    }

    public User findById(int id) {
        return userRepository.findById(id);
    }

    public userType role(User user) {
        if (user instanceof Student) {
            return userType.student;
        } else if (user instanceof Staff) {
            return switch (((Staff) user).getRole()) {
                case admin -> userType.admin;
                case assistant -> userType.assistant;
            };
        }
        return null;
    }

    public enum userType {student, assistant, admin}

}







