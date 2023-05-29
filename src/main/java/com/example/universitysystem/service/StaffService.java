/*
 * StaffService
 * methods affecting the staff
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.service;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Staff;
import com.example.universitysystem.model.User;
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
        for (Course course : courses) {
            course.setStaff(null);
            courseRepository.save(course);
        }
        staffRepository.deleteById(id);
        userRepository.deleteById(id);
    }

    public void updateStaff(Staff updateStaff, int id, Staff.role role) {
        Staff oldStaff = (Staff) userRepository.findById(id);
        oldStaff.setFirstName(updateStaff.getFirstName());
        oldStaff.setLastName(updateStaff.getLastName());
        oldStaff.setEmail(updateStaff.getEmail());
        oldStaff.setPassword(updateStaff.getPassword().isEmpty() ? oldStaff.getPassword() : updateStaff.getPassword());
        oldStaff.setRole(role);
        userRepository.save(oldStaff);
    }

    public void save(Staff staff) {
        staff.setFirstLogin(false);
        userRepository.save(staff);
    }

}
