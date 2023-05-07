package com.example.universitysystem.model;

import com.example.universitysystem.repository.StudentCourseRepository;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
public class Student extends User {



    @Column(name = "student_number", length = 8)
    private int studentNumber;

}
