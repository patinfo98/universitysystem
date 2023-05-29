/*
 * StudentService
 * methods affecting the student objects
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.service;

import com.example.universitysystem.model.Student;
import com.example.universitysystem.repository.StudentCourseRepository;
import com.example.universitysystem.repository.StudentRepository;
import com.example.universitysystem.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    public void deleteById(int id) {
        studentCourseRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    public void updateStudent(Student updateStudent, int id) {
        Student oldStudent = (Student) userRepository.findById(id);
        oldStudent.setFirstName(updateStudent.getFirstName());
        oldStudent.setLastName(updateStudent.getLastName());
        oldStudent.setEmail(updateStudent.getEmail());
        oldStudent.setPassword(updateStudent.getPassword().isEmpty() ? oldStudent.getPassword() : updateStudent.getPassword());
        oldStudent.setStudentNumber(updateStudent.getStudentNumber());
        studentRepository.save(oldStudent);
    }
}
