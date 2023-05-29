/*
 * CourseService
 * methods in connection to course objects
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.service;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {
    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;
    private final UserRepository userRepository;
    private final TimetableRepository timetableRepository;


    public CourseService(StudentCourseRepository studentCourseRepository, CourseRepository courseRepository, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository, UserRepository userRepository, TimetableRepository timetableRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
        this.userRepository = userRepository;
        this.timetableRepository = timetableRepository;
    }

    public void updateCourse(int id, Course updateCourse, Course.types type, Course.fieldOfStudy field, Staff teacher) {
        Course oldCourse = courseRepository.findById(id);
        oldCourse.setName(updateCourse.getName());
        oldCourse.setDescription(updateCourse.getDescription());
        oldCourse.setStaff(teacher);
        oldCourse.setHoursPerWeek(updateCourse.getHoursPerWeek());
        oldCourse.setType(type);
        oldCourse.setField(field);
        courseRepository.save(oldCourse);
    }

    public List<Course> CoursesNotEnrolled(int id) {
        List<Course> courses = courseRepository.findAll();
        List<Course> coursesStudent = studentCourseRepository.findAllByStudent((Student) userRepository.findById(id)).stream().map(StudentCourse::getTeacherCourse).toList();
        courses.removeAll(coursesStudent);
        return courses;
    }

    public Course add(Course course, int teacherid) {
        Staff teacher = (Staff) userRepository.findById(teacherid);
        course.setStaff(teacher);
        return courseRepository.save(course);
    }

    public void deleteById(int id) {
        studentCourseRepository.deleteByTeacherCourseId(id);
        courseRoomTimePreferenceRepository.deleteByCourseId(id);
        timetableRepository.deleteByTeacherCourseId(id);
        courseRepository.deleteById(id);
    }


    public CourseRoomTimePreference saveRoomTimePreference(Room room, LocalTime starttime, LocalTime endtime, days day, int courseid) {
        CourseRoomTimePreference courseRoomTimePreference = new CourseRoomTimePreference();
        courseRoomTimePreference.setRoom(room);
        courseRoomTimePreference.setCourse(courseRepository.findById(courseid));
        courseRoomTimePreference.setTime(starttime);
        courseRoomTimePreference.setDay(day);
        courseRoomTimePreference.setEndtime(endtime);
        return courseRoomTimePreferenceRepository.save(courseRoomTimePreference);
    }

    public List<Course> findNotFull() {
        List<Course> needed = new ArrayList<>();
        List<Course> courses = courseRepository.findAll();
        for (Course course : courses) {
            if (timetableRepository.existsByTeacherCourseId(course.getId())) {
                List<TimeTable> allCourseTimes = timetableRepository.findByTeacherCourseId(course.getId());
                int totalhours = 0;
                for (TimeTable table : allCourseTimes) {
                    LocalTime start = table.getStart();
                    LocalTime end = table.getEnd();
                    int hours = Duration.between(start, end).toHoursPart();
                    totalhours += hours;
                }
                if (totalhours < course.getHoursPerWeek()) {
                    needed.add(course);
                }
            } else {
                needed.add(course);
            }
        }
        return needed;
    }

}

