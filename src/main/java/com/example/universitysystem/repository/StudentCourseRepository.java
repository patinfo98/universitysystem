package com.example.universitysystem.repository;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Student;
import com.example.universitysystem.model.StudentCourse;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentCourseRepository extends JpaRepository<StudentCourse, Integer> {

    List<StudentCourse> findAllByStudent(Student student);

    @Transactional
    void deleteByStudentId(int id);

    StudentCourse findByTeacherCourseIdAndStudentId(int courseid, int studentid);

    void deleteByTeacherCourseId(int id);


}
