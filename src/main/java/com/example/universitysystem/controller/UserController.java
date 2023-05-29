package com.example.universitysystem.controller;
import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.*;
import com.example.universitysystem.service.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
    private UserService.userType role;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;

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
            List <Student> students = studentRepository.findAll();
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
    public String addStudent(@ModelAttribute("student") Student student) {
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
    public String updateStudent(@PathVariable("id") int id, @ModelAttribute("student") Student updateStudent) {
        Student oldStudent = (Student) userRepository.findById(id);
        oldStudent.setFirstName(updateStudent.getFirstName());
        oldStudent.setLastName(updateStudent.getLastName());
        oldStudent.setEmail(updateStudent.getEmail());
        oldStudent.setPassword(updateStudent.getPassword().isEmpty() ? oldStudent.getPassword() : updateStudent.getPassword());
        oldStudent.setStudentNumber(updateStudent.getStudentNumber());
        userRepository.save(oldStudent);
        return "redirect:/students";
    }

    @PostMapping("/deleteconfirm/{id}")
    public String studentDelete(@PathVariable("id") int id) {
        studentService.deleteById(id);
        return "redirect:/students";
    }

    @GetMapping("/plan/student")
    public String customTable(Model model) {
        if (userService.accessAllowed(UserService.userType.student)) {
            int userid = userService.sessionId();
            List<TimeTable> table = timetableRepository.findByTeacherCourseIn(studentCourseRepository.findAllByStudent((Student) userRepository.findById(userid)).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList()));
            Collections.sort(table, Comparator.comparing(TimeTable::getDate).thenComparing(TimeTable::getStart).thenComparing(x -> x.getTeacherCourse().getName()));
            model.addAttribute("table", table);
            model.addAttribute("slot", new TimeTable());
            model.addAttribute("role", "student");
            model.addAttribute("id", userid);
            return "plan";
        }
        return "errorPage";
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
    public String addTeacher(@ModelAttribute("teacher") Staff staff, @ModelAttribute("role") Staff.role role) {
        staff.setRole(role);
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
    public String updateTeacher(@PathVariable("id") int id, @ModelAttribute("staff") Staff updateStuff, @ModelAttribute("role") Staff.role role) {
        Staff oldStaff = (Staff) userRepository.findById(id);
        oldStaff.setFirstName(updateStuff.getFirstName());
        oldStaff.setLastName(updateStuff.getLastName());
        oldStaff.setEmail(updateStuff.getEmail());
        oldStaff.setPassword(updateStuff.getPassword().isEmpty() ? oldStaff.getPassword() : updateStuff.getPassword());
        oldStaff.setRole(role);
        userRepository.save(oldStaff);
        return "redirect:/teachers";
    }

    @PostMapping("/deleteconfirm/teachers/{id}")
    public String teacherDelete(@PathVariable("id") int id) {
        staffService.deleteById(id);
        return "redirect:/teachers";
    }

    @GetMapping("/plan/admin")
    public String teacherTable(Model model) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            int id = userService.sessionId();
            User user = userRepository.findById(id);
            if (user.getFirstLogin()) {
                model.addAttribute("firstLogin", true);
                user.setFirstLogin(false);
                userRepository.save(user);
            }
            List<TimeTable> table = timetableRepository.findAll();
            Collections.sort(table, Comparator.comparing(TimeTable::getDate).thenComparing(TimeTable::getStart).thenComparing(x -> x.getTeacherCourse().getName()));
            model.addAttribute("table", table);
            model.addAttribute("slot", new TimeTable());
            model.addAttribute("role", "admin");
            model.addAttribute("preferenceNotUsed", timeTableService.preferenceNotUsed(id));
            return "plan";
        }
        return "errorPage";
    }

    @GetMapping("/plan/assistant")
    public String assistantTable(Model model) {
        if (userService.accessAllowed(UserService.userType.assistant)) {
            int id = userService.sessionId();
            User user = userRepository.findById(id);
            if (user.getFirstLogin()) {
                model.addAttribute("firstLogin", true);
                user.setFirstLogin(false);
                userRepository.save(user);
            }
            List<TimeTable> table = timeTableService.findByTeacherCourseStaff_Id(id);
            Collections.sort(table, Comparator.comparing(TimeTable::getDate).thenComparing(TimeTable::getStart).thenComparing(x -> x.getTeacherCourse().getName()));
            model.addAttribute("table", table);
            model.addAttribute("slot", new TimeTable());
            model.addAttribute("role", "assistant");
            model.addAttribute("preferenceNotUsed", timeTableService.preferenceNotUsed(id));
            return "plan";
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
            role = userService.role(user);
            switch (role) {
                case admin:
                    return "redirect:/plan/admin";
                case assistant:
                    return "redirect:/plan/assistant";
                case student:
                    return "redirect:/plan/student";
                default:
                    return "redirect:/";
            }

        } else {
            model.addAttribute("error", "Invalid email or password");
            return "index";
        }
    }
}


