/*
 * User Controller
 * methods affecting the display of user information
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.controller;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.*;
import com.example.universitysystem.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final StudentCourseRepository studentCourseRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final StudentCourseService studentCourseService;
    private final StudentService studentService;
    private final TimeTableService timeTableService;

    private final CourseRepository courseRepository;
    private final CourseService courseService;

    private final TimetableRepository timetableRepository;
    private final StaffService staffService;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;
    private UserService.userType role;

    public UserController(StudentCourseRepository studentCourseRepository, UserService userService, RoomService roomService, StudentCourseService studentCourseService, StudentService studentService, TimeTableService timeTableService, CourseRepository courseRepository, CourseService courseService, TimetableRepository timetableRepository, StaffService staffService, UserRepository userRepository, StaffRepository staffRepository, StudentRepository studentRepository, RoomRepository roomRepository, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.userService = userService;
        this.roomService = roomService;
        this.studentCourseService = studentCourseService;
        this.studentService = studentService;
        this.timeTableService = timeTableService;
        this.courseRepository = courseRepository;
        this.courseService = courseService;
        this.timetableRepository = timetableRepository;
        this.staffService = staffService;
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
    }


    //student handling
    @GetMapping("/students")
    public String adminFunctions(Model model, Student student) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            List<Student> students = studentRepository.findAll();
            Collections.sort(students, Comparator.comparing(Student::getStudentNumber));
            model.addAttribute("students", students);
            model.addAttribute("student", student);
            return "students";
        }
        return "errorPage";
    }

    @GetMapping("/add_students")
    public String showAddStudent(Student student, Model model) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            model.addAttribute("student", student);
            return "add_students";
        }
        return "errorPage";
    }

    @PostMapping("/add")
    public String addStudent(@ModelAttribute("student") Student student, @ModelAttribute("password") String password) {
        student.setPassword(password);
        userService.save(student);
        return "redirect:/students";
    }

    @GetMapping("/edit/{id}")
    public String studentUpdateForm(@PathVariable("id") int id, Model model) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            Student student = (Student) userRepository.findById(id);
            model.addAttribute("student", student);
            return "update_students";
        }
        return "errorPage";
    }

    @PostMapping("/update/students/{id}")
    public String updateStudent(@PathVariable("id") int id, @ModelAttribute("student") Student updateStudent, @ModelAttribute("password") String password) {
        studentService.updateStudent(updateStudent, id);
        return "redirect:/students";
    }

    @PostMapping("/deleteconfirm/{id}")
    public String studentDelete(@PathVariable("id") int id) {
        studentService.deleteById(id);
        return "redirect:/students";
    }

    //staff handling
    @GetMapping("/teachers")
    public String teacherDisplay(Model model, Staff staff) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            List<Staff> teachers = staffRepository.findAll();
            Collections.sort(teachers, Comparator.comparing(Staff::getLastName));
            model.addAttribute("staffs", teachers);
            model.addAttribute("roles", Staff.role.values());
            model.addAttribute("staff", staff);
            return "teachers";
        }
        return "errorPage";
    }

    @PostMapping("/add_teacher")
    public String addTeacher(@ModelAttribute("teacher") Staff staff, @ModelAttribute("role") Staff.role role, @ModelAttribute("password") String password) {
            staff.setRole(role);
            staff.setPassword(password);
            staff.setFirstLogin(true);
            userRepository.save(staff);
            return "redirect:/teachers";
    }

    @GetMapping("/edit/teachers/{id}")
    public String teacherUpdateForm(@PathVariable("id") int id, Model model) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            Staff staff = (Staff) userRepository.findById(id);
            model.addAttribute("staff", staff);
            model.addAttribute("roles", Staff.role.values());
            return "update_staff";
        }
        return "errorPage";
    }

    @PostMapping("/update/teachers/{id}")
    public String updateTeacher(@PathVariable("id") int id, @ModelAttribute("staff") Staff updateStuff, @ModelAttribute("role") Staff.role role, @ModelAttribute("password") String password) {
        staffService.updateStaff(updateStuff, id, role);
        return "redirect:/teachers";
    }

    @PostMapping("/deleteconfirm/teachers/{id}")
    public String teacherDelete(@PathVariable("id") int id) {
        staffService.deleteById(id);
        return "redirect:/teachers";
    }

    @GetMapping("/plan")
    public String teacherTable(Model model) {
        try{
        int id = userService.sessionId();
        User user = userRepository.findById(id);
        List<TimeTable> table = new ArrayList<TimeTable>();
        if (userService.accessAllowed(UserService.userType.admin) || userService.accessAllowed(UserService.userType.assistant)) {
            Staff staff = (Staff) user;
            if (staff.getFirstLogin()) {
                model.addAttribute("firstLogin", true);
                staff.setFirstLogin(false);
                userRepository.save(user);
            }
        }
        if (userService.accessAllowed(UserService.userType.admin)) {
            table = timetableRepository.findAll();
            model.addAttribute("role", "admin");

        }
        if (userService.accessAllowed(UserService.userType.assistant)) {
            model.addAttribute("role", "assistant");
            table = timeTableService.findByTeacherCourseStaff_Id(id);
        }
        if (userService.accessAllowed(UserService.userType.student)) {
            table = timetableRepository.findByTeacherCourseIn(studentCourseRepository.findAllByStudent((Student) userRepository.findById(id)).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList()));
            model.addAttribute("role", "student");
            model.addAttribute("id", id);
        }


        if ((userService.accessAllowed(UserService.userType.admin) || userService.accessAllowed(UserService.userType.assistant) || userService.accessAllowed(UserService.userType.student))) {
            List<LocalDate> dates = timetableRepository.findAll().stream().map(TimeTable::getDate).collect(Collectors.toList());
            TreeSet<LocalDate> allDates = new TreeSet<LocalDate>(dates);
            Collections.sort(table, Comparator.comparing(TimeTable::getDate).thenComparing(TimeTable::getStart).thenComparing(x -> x.getTeacherCourse().getName()));
            model.addAttribute("table", table);
            model.addAttribute("slot", new TimeTable());
            model.addAttribute("preferenceNotUsed", timeTableService.preferenceNotUsed(id));
            model.addAttribute("dates", allDates);
            model.addAttribute("times", timeTableService.alltimes());
            model.addAttribute("valid", model.getAttribute("valid"));
            return "plan";}
        } catch (Exception e){
            return "errorPage";
        }
        return "errorPage";
    }


    //login handling
    @GetMapping("/")
    public String showLogin() {
        return "index";
    }

    @PostMapping("/")
    public String login(Model model, @RequestParam("email") String email, @RequestParam("password") String password) {

            if (userService.loginValid(email, password)) {
                User user = userRepository.findByEmail(email);
                userService.initiateSession(user.getId());
                return "redirect:/plan";
            } else {
                model.addAttribute("error", "Invalid email or password");
                return "index";
            }
    }
}


