/*
 * StudentCourseService
 * methods affecting students and their respective courses
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.service;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Student;
import com.example.universitysystem.model.StudentCourse;
import com.example.universitysystem.repository.StudentCourseRepository;
import com.example.universitysystem.repository.TimetableRepository;
import org.springframework.stereotype.Service;

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

}




