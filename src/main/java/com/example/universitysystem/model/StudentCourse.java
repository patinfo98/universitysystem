package com.example.universitysystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "student_courses")
public class StudentCourse {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne()
    @JoinColumn(name = "student_id")
    Student student;
    @ManyToOne()
    @JoinColumn(name = "teacher_course_id")
    Course teacherCourse;
}
