package com.example.universitysystem.service;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.CourseRepository;
import com.example.universitysystem.repository.CourseRoomTimePreferenceRepository;
import com.example.universitysystem.repository.StudentCourseRepository;
import com.example.universitysystem.repository.TimetableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class CourseService {
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;
    private final UserService userService;
    private final TimetableRepository timetableRepository;


    public CourseService(StudentCourseRepository studentCourseRepository, CourseRepository courseRepository, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository, UserService userService, TimetableRepository timetableRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
        this.userService = userService;
        this.timetableRepository = timetableRepository;
    }

    public Course add(Course course, int teacherid) {
        Staff teacher = (Staff) userService.findById(teacherid);
        course.setStaff(teacher);
        return courseRepository.save(course);
    }

    public void delete(Course course) {
        studentCourseRepository.deleteByTeacherCourseId(course.getId());
        courseRepository.delete(course);
    }

    public List<Course> findByStaff(Staff staff) {
        return courseRepository.findByStaff(staff);
    }

    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(int id) {
        return courseRepository.findById(id);
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public void deleteById(int id) {
        studentCourseRepository.deleteByTeacherCourseId(id);
        courseRoomTimePreferenceRepository.deleteByCourseId(id);
        timetableRepository.deleteByTeacherCourseId(id);
        courseRepository.deleteById(id);
    }


    public CourseRoomTimePreference saveRoomTimePreference(Room room, LocalTime starttime, days day, int courseid) {
        CourseRoomTimePreference courseRoomTimePreference = new CourseRoomTimePreference();
        courseRoomTimePreference.setRoom(room);
        courseRoomTimePreference.setCourse(courseRepository.findById(courseid));
        courseRoomTimePreference.setTime(starttime);
        courseRoomTimePreference.setDay(day);
        return courseRoomTimePreferenceRepository.save(courseRoomTimePreference);
    }

}

