package com.example.universitysystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@NamedQuery(name="overlapEntries", query = "SELECT t " +
                "FROM TimeTable t " +
                "WHERE t.start < :end " +
                "AND t.end > :start " +
                "AND t.day = :day"
)
@Data
@Entity
@Table(name = "teacher_course_time_room")
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "teachercourse_id")
    private Course teacherCourse;

    @Column(name = "start_time")
    private LocalTime start;

    @Column(name = "end_time")
    private LocalTime end;

    @Column days day;

    @ManyToOne
    @JoinColumn(name="room_id")
    private Room room;
}
