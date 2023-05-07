package com.example.universitysystem.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
@Entity
@Table(name = "course_room_time_preference", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"course_id", "room_id", "start_time"})})
public class    CourseRoomTimePreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    @Column(name="day")
    private days day;
    @Column(name="start_time")
    private LocalTime time;
}
