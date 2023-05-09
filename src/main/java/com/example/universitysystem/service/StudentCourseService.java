package com.example.universitysystem.service;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Student;
import com.example.universitysystem.model.StudentCourse;
import com.example.universitysystem.model.TimeTable;
import com.example.universitysystem.repository.StudentCourseRepository;
import com.example.universitysystem.repository.TimetableRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentCourseService {
    private final StudentCourseRepository studentCourseRepository;
    private final TimeTableService timeTableService;
    private final TimetableRepository timetableRepository;

    public StudentCourseService(StudentCourseRepository studentCourseRepository, TimeTableService timeTableService, TimetableRepository timetableRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.timeTableService = timeTableService;
        this.timetableRepository = timetableRepository;
    }

    public StudentCourse add(Student student, Course course) {
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setStudent(student);
        studentCourse.setTeacherCourse(course);
        return studentCourseRepository.save(studentCourse);
    }

    public boolean overlap(Course course, Student student) {
        List<TimeTable> courses = timetableRepository.findByTeacherCourseIn(studentCourseRepository.findAllByStudent(student).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList()));
        TimeTable courseTime = timetableRepository.findByTeacherCourse(course);
        LocalTime start = courseTime.getStart();
        LocalTime end = courseTime.getEnd();
        for (TimeTable c : courses) {
            LocalTime start2 = c.getStart();
            LocalTime end2 = c.getEnd();

            if ((((start.isBefore(start2) || start.equals(start2)) && end.isAfter(start2)) || ((start2.isBefore(start) || start.equals(start2)) && end2.isAfter(start)))) {
                return true;
            }
        }
        return false;
    }

}




