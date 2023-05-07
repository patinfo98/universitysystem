package com.example.universitysystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
@Table(name = "course")
public class Course {

    public enum types {
        VO, VU, UE, SE
    }
    public enum fieldOfStudy {
        MobileSoftwareDevelopment,
        Elektrotechnik,


    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;
    @Column(name = "description", length = 500, nullable = false )
    private String description;
    @Column(name="hours_per_week", length=4, nullable = false)
    private int hoursPerWeek;
    @Column(name="type")
    private types type;
    @Column(name="field_of_study")
    private fieldOfStudy field;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    Staff staff;

}

