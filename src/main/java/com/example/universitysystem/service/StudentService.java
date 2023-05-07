package com.example.universitysystem.service;

import com.example.universitysystem.model.Student;
import com.example.universitysystem.repository.StudentCourseRepository;
import com.example.universitysystem.repository.StudentRepository;
import com.example.universitysystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentCourseRepository studentCourseRepository;

    public StudentService(StudentRepository studentRepository, StudentCourseRepository studentCourseRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.studentCourseRepository = studentCourseRepository;
        this.userRepository = userRepository;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public void deleteById(int id) {
        studentCourseRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
        userRepository.deleteById(id);
    }
}
