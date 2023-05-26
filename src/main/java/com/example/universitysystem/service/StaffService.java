package com.example.universitysystem.service;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Staff;
import com.example.universitysystem.repository.CourseRepository;
import com.example.universitysystem.repository.StaffRepository;
import com.example.universitysystem.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {
    private final StaffRepository staffRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public StaffService(StaffRepository staffRepository, UserRepository userRepository, CourseRepository courseRepository) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public void deleteById(int id) {
        List<Course> courses = courseRepository.findByStaffId(id);
        for(Course course : courses){
            course.setStaff(null);
            courseRepository.save(course);
        }
        staffRepository.deleteById(id);
        userRepository.deleteById(id);
    }

}
