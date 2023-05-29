/*
 * UserService
 * methods affecting the user
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.service;

import com.example.universitysystem.model.Staff;
import com.example.universitysystem.model.Student;
import com.example.universitysystem.model.User;
import com.example.universitysystem.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

@Service
public class UserService {
    private final UserRepository userRepository;

    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];


    @Autowired
    HttpSession session;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public boolean loginValid(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            User user = userRepository.findByEmail(email);
            return user.getPassword().equals(password);
        }
        return false;
    }

    public void save(User user) {
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

    public int sessionId() {
        return (int) session.getAttribute("userId");
    }

    public boolean accessAllowed(UserService.userType role) {
        return session.getAttribute("userId") != null && role(findById(sessionId())).equals(role);
    }

    public boolean isSelf(UserService.userType role, int id) {
        return (accessAllowed(role) && sessionId() == id);
    }

    public void initiateSession(int id) {
        session.setAttribute("userId", id);
    }

    public enum userType {student, assistant, admin}


}







