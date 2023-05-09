package com.example.universitysystem.repository;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Room;
import com.example.universitysystem.model.TimeTable;
import com.example.universitysystem.model.days;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Time;
import java.util.List;

public interface TimetableRepository extends JpaRepository<TimeTable, Integer> {
    List<TimeTable> findByTeacherCourseIn(List<Course> course);

    void deleteByRoom(Room room);

    List<TimeTable> findByTeacherCourse_Staff_Id(int id);

    boolean existsByTeacherCourseId(int id);

    List<TimeTable> findByDay(days day);

    void deleteByTeacherCourseId(int id);

    TimeTable findByTeacherCourse(Course course);

    List<TimeTable> findByTeacherCourseId(int id);


}
