package com.example.universitysystem.controller;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Room;
import com.example.universitysystem.model.Staff;
import com.example.universitysystem.model.Student;
import com.example.universitysystem.repository.RoomRepository;
import com.example.universitysystem.repository.UserRepository;
import com.example.universitysystem.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class APIController {
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final CourseService courseService;

    public APIController(UserRepository userRepository, RoomRepository roomRepository, CourseService courseService) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.courseService = courseService;
    }

    @PostMapping("/api/students")
    @ResponseBody
    public Student saveStudent(@RequestBody Student student) {
        return userRepository.save(student);
    }

    @PostMapping("/api/admins")
    @ResponseBody
    public Staff saveStaff(@RequestBody Staff[] staffs) {
        for (Staff staff : staffs) {
            userRepository.save(staff);
        }
        return staffs[0];

    }

    @PostMapping("/api/courses")
    @ResponseBody
    public void saveCourse(@RequestBody Course[] courses) {
        for (Course course : courses) {
            courseService.add(course, course.getStaff().getId());
        }
    }

    @PostMapping("/api/rooms")
    @ResponseBody
    public Room saveRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }
}

