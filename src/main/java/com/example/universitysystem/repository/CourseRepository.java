package com.example.universitysystem.repository;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByStaff(Staff staff);

    Course findById(int id);

    void deleteById(int id);

    @Query("SELECT c FROM Course c WHERE c NOT IN (SELECT t.teacherCourse FROM TimeTable t)")
    List<Course> findCoursesNotInTimetable();

    List<Course> findByStaffId(int id);


}
