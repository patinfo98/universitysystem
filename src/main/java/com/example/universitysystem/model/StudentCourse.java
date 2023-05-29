/*
 * Student Database Object
 * contains information about the courses a student takes
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "student_courses")
public class StudentCourse {
    @ManyToOne()
    @JoinColumn(name = "student_id")
    Student student;
    @ManyToOne()
    @JoinColumn(name = "teacher_course_id")
    Course teacherCourse;
    @Id
    @GeneratedValue
    private int id;
}
