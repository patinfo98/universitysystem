/*
 * CourseController
 * methods in connection to the display of course information
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.controller;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.*;
import com.example.universitysystem.service.CourseService;
import com.example.universitysystem.service.StudentCourseService;
import com.example.universitysystem.service.TimeTableService;
import com.example.universitysystem.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CourseController {

    private final StudentCourseRepository studentCourseRepository;
    private final UserService userService;
    private final StudentCourseService studentCourseService;
    private final TimeTableService timeTableService;
    private final CourseRepository courseRepository;
    private final TimetableRepository timetableRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final StaffRepository staffRepository;
    private final CourseService courseService;

    public CourseController(StudentCourseRepository studentCourseRepository, UserService userService, StudentCourseService studentCourseService, TimeTableService timeTableService, CourseRepository courseRepository, TimetableRepository timetableRepository, UserRepository userRepository, RoomRepository roomRepository, StaffRepository staffRepository, CourseService courseService) {
        this.studentCourseRepository = studentCourseRepository;
        this.userService = userService;
        this.studentCourseService = studentCourseService;
        this.timeTableService = timeTableService;
        this.courseRepository = courseRepository;
        this.timetableRepository = timetableRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.staffRepository = staffRepository;
        this.courseService = courseService;
    }

    //Courses
    @GetMapping("/courses")
    public String adminFunctions(Model model, Course course) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            List<Course> courses = courseRepository.findAll();
            Collections.sort(courses, Comparator.comparing(Course::getName));
            model.addAttribute("courses", courseRepository.findAll());
            model.addAttribute(timeTableService);
            model.addAttribute("course", course);
            model.addAttribute("teachers", staffRepository.findAll().stream().map(Staff::getLastName));
            model.addAttribute("types", Course.types.values());
            model.addAttribute("fields", Course.fieldOfStudy.values());
            model.addAttribute("role", "admin");
            return "courses";
        } else if (userService.accessAllowed(UserService.userType.assistant)) {
            model.addAttribute("courses", courseRepository.findByStaffId(userService.sessionId()));
            model.addAttribute(timeTableService);
            model.addAttribute("course", course);
            model.addAttribute("teachers", staffRepository.findAll().stream().map(Staff::getLastName));
            model.addAttribute("types", Course.types.values());
            model.addAttribute("fields", Course.fieldOfStudy.values());
            model.addAttribute("role", "assistant");
            return "courses";
        }
        return "errorPage";
    }

    @PostMapping("/add/course")
    public String addCourse(@ModelAttribute("course") Course course, @RequestParam("teacher") String name, @RequestParam("type") Course.types type, @RequestParam("field") Course.fieldOfStudy field) {
        Staff staff = staffRepository.findByLastName(name);
        course.setStaff(staff);
        course.setType(type);
        course.setField(field);
        courseRepository.save(course);
        return "redirect:/courses";
    }

    @GetMapping("/edit/courses/{id}")
    public String courseUpdateForm(@PathVariable("id") int id, Model model) {
        Course course = courseRepository.findById(id);
        if (userService.accessAllowed(UserService.userType.admin) || userService.isSelf(UserService.userType.assistant, course.getStaff().getId())) {
            model.addAttribute("teachers", staffRepository.findAll().stream().map(Staff::getLastName));
            model.addAttribute("types", Course.types.values());
            model.addAttribute("course", course);
            model.addAttribute("fields", Course.fieldOfStudy.values());
            return "update_courses";
        }
        return "errorPage";

    }

    @PostMapping("/update/courses/{id}")
    public String updateCourse(@PathVariable("id") int id, @ModelAttribute("course") Course updateCourse, @RequestParam("teacher") String teacher, @RequestParam("type") Course.types type, @RequestParam("field") Course.fieldOfStudy field) {
        courseService.updateCourse(id, updateCourse, type, field, staffRepository.findByLastName(teacher));
        return "redirect:/courses";
    }

    @Transactional
    @PostMapping("/deleteconfirm/courses/{id}")
    public String courseDelete(@PathVariable("id") int id) {
        courseService.deleteById(id);
        return "redirect:/courses";
    }


    //Students courses
    @GetMapping("add/student/course/{id}")
    public String addStudentCourse(@PathVariable("id") int id, Model model) {
        if (userService.accessAllowed(UserService.userType.admin) || userService.isSelf(UserService.userType.student, id)) {
            boolean error1 = model.getAttribute("error") != null && (boolean) model.getAttribute("error");
            int selectedCourse = model.getAttribute("selectedCourseId") == null ? 1 : (int) model.getAttribute("selectedCourseId");
            List<Course> courses = courseService.CoursesNotEnrolled(id);
            model.addAttribute("courses", courses);
            model.addAttribute("warning", "This course has an overlap with one of your other courses!");
            model.addAttribute("error", error1);
            model.addAttribute("selectedId", selectedCourse);
            return "add_student_courses";
        }
        return "errorPage";
    }

    @PostMapping("add/student/course/{id}/anyway")
    public String addCourseAnyway(@PathVariable("id") int id, @RequestParam("course") int courseId, RedirectAttributes redirectAttributes) {
        studentCourseService.add((Student) userRepository.findById(id), courseRepository.findById(courseId));
        boolean error = false;
        redirectAttributes.addFlashAttribute("error", error);
        return "redirect:/show/student/courses/{id}";
    }

    @PostMapping("add/student/course/{id}")
    public String addCourse(@PathVariable("id") int id, @RequestParam("course") int courseId, RedirectAttributes redirectAttributes) {
        Student student = (Student) userRepository.findById(id);
        Course course = courseRepository.findById(courseId);
        List<TimeTable> courseTimes = timetableRepository.findByTeacherCourse(courseRepository.findById(courseId));
        List<Course> studentCourses = studentCourseRepository.findAllByStudent(student).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList());
        List<TimeTable> studentTimes = timetableRepository.findByTeacherCourseIn(studentCourses);
        boolean error = timeTableService.checkCourseOverlap(studentTimes, courseTimes);
        if (error) {
            redirectAttributes.addFlashAttribute("error", error);
            redirectAttributes.addFlashAttribute("selectedCourseId", courseId);
            return "redirect:/add/student/course/{id}";
        } else {
            studentCourseService.add(student, course);
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/show/student/courses/{id}";
        }
    }

    @GetMapping("/show/student/courses/{id}")
    public String showCourses(@PathVariable("id") int id, Model model) {
        String userrole = userService.role(userService.findById(userService.sessionId())).name();
        if (userService.accessAllowed(UserService.userType.admin) || userService.isSelf(UserService.userType.student, id)) {
            List<StudentCourse> courses = studentCourseRepository.findAllByStudent((Student) userRepository.findById(id));
            Collections.sort(courses, Comparator.comparing(x -> x.getTeacherCourse().getName()));
            model.addAttribute("student", userRepository.findById(id).getLastName());
            model.addAttribute("courses", courses);
            model.addAttribute("role", userrole);
            return "student_courses";
        }
        return "errorPage";
    }

    @PostMapping("/delete/student/courses/{id}/{studentid}")
    public String deleteConfirmation(@PathVariable("id") int id, @PathVariable("studentid") int studentid) {
        StudentCourse studentCourse = studentCourseRepository.findByTeacherCourseIdAndStudentId(id, studentid);
        studentCourseRepository.delete(studentCourse);
        return "redirect:/show/student/courses/{studentid}";
    }

    //Teachers courses
    @GetMapping("/show/teachers/courses/{id}")
    public String showTeachersCourses(@PathVariable("id") int id, Model model) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            List<Course> courses = courseRepository.findByStaff((Staff) userRepository.findById(id));
            Collections.sort(courses, Comparator.comparing(Course::getName));

            model.addAttribute("courses", courses);
            model.addAttribute("teacher", userRepository.findById(id).getLastName());
            return "teacher_courses";
        }
        return "errorPage";
    }
}
