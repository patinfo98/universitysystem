package com.example.universitysystem.controller;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.CourseRoomTimePreferenceRepository;
import com.example.universitysystem.repository.StudentCourseRepository;
import com.example.universitysystem.service.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    private final CourseService courseService;

    private final StaffService staffService;
    private UserService.userType role;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;

    @Autowired
    public UserController(StudentCourseRepository studentCourseRepository, UserService userService, RoomService roomService, StudentCourseService studentCourseService, StudentService studentService, TimeTableService timeTableService, CourseService courseService, StaffService staffService, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.userService = userService;
        this.roomService = roomService;
        this.studentCourseService = studentCourseService;
        this.studentService = studentService;
        this.timeTableService = timeTableService;
        this.courseService = courseService;
        this.staffService = staffService;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
    }

    @GetMapping("/students")
    public String adminFunctions(Model model, Student student) {
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("student", student);
        return "students";
    }

    @GetMapping("/add_students")
    public String showAddStudent(Student student, Model model) {
        model.addAttribute("student", student);
        return "add_students";
    }

    @PostMapping("/add")
    public String addStudent(@ModelAttribute("student") Student student) {
        userService.saveStudent(student);
        return "redirect:/students";
    }

    @GetMapping("/edit/{id}")
    public String studentUpdateForm(@PathVariable("id") int id, Model model) {
        Student student = (Student) userService.findById(id);
        model.addAttribute("student", student);
        return "update_students";
    }

    @PostMapping("/update/students/{id}")
    public String updateStudent(@PathVariable("id") int id, @ModelAttribute("student") Student updateStudent) {
        Student oldStudent = (Student) userService.findById(id);
        oldStudent.setFirstName(updateStudent.getFirstName());
        oldStudent.setLastName(updateStudent.getLastName());
        oldStudent.setEmail(updateStudent.getEmail());
        oldStudent.setPassword(updateStudent.getPassword());
        oldStudent.setStudentNumber(updateStudent.getStudentNumber());
        userService.saveStudent(oldStudent);
        return "redirect:/students";
    }

    @GetMapping("/delete/{id}")
    public String studentDeleteForm(@PathVariable("id") int id, Model model) {
        Student student = (Student) userService.findById(id);
        model.addAttribute("student", student);
        return "delete_students";
    }

    @PostMapping("/deleteconfirm/{id}")
    public String studentDelete(@PathVariable("id") int id) {
        studentService.deleteById(id);
        return "redirect:/students";
    }

    @GetMapping("add/student/course/{id}")
    public String addStudentCourse(@PathVariable("id") int id, Model model) {
        List<Course> courses = courseService.findAll();
        List<Course> coursesStudent = studentCourseRepository.findAllByStudent((Student) userService.findById(id)).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList());
        courses.removeAll(coursesStudent);
        model.addAttribute("courses", courses);
        return "add_student_courses";
    }

    @PostMapping("add/student/course/{id}")
    public String addCourse(@PathVariable("id") int id, @RequestParam("course") int courseId, Model model, RedirectAttributes redirectAttributes) {
        Student student = (Student) userService.findById(id);
        Course course = courseService.findById(courseId);
        studentCourseService.add(student, course);
        redirectAttributes.addAttribute("courseId", courseId);
        return "redirect:/show/student/courses/{id}/{courseId}";
    }

    @GetMapping("/show/student/courses/{id}/{courseId}")
    public String showCoursesCheckWarning(@PathVariable("id") int id, @PathVariable("courseId") int courseid, Model model) {
        List<StudentCourse> courses = studentCourseService.getByStudent((Student) userService.findById(id));
        model.addAttribute("student", userService.findById(id).getLastName());
        model.addAttribute("courses", courses);
        model.addAttribute("overlap", studentCourseService.overlap(courseService.findById(courseid),(Student) userService.findById(id)));
        model.addAttribute("courseId", courseid);
        return "student_courses";
    }

    @GetMapping("/show/student/courses/{id}")
    public String showCourses(@PathVariable("id") int id, Model model) {
        List<StudentCourse> courses = studentCourseService.getByStudent((Student) userService.findById(id));
        model.addAttribute("student", userService.findById(id).getLastName());
        model.addAttribute("courses", courses);
        return "student_courses";
    }

    @GetMapping("/delete/student/courses/{id}/{studentid}")
    public String deleteStudentCourses(@PathVariable("id") int id, @PathVariable("studentid") int studentid, Model model) {
        StudentCourse course = studentCourseService.findByCourseIdAndStudent(id, studentid);
        model.addAttribute("course", course);
        return "delete_student_courses";
    }

    @PostMapping("/delete/student/courses/{id}/{studentid}")
    public String deleteConfirmation(@PathVariable("id") int id, @PathVariable("studentid") int studentid) {
        StudentCourse studentCourse = studentCourseService.findByCourseIdAndStudent(id, studentid);
        studentCourseService.deleteByStudentCourse(studentCourse);
        return "redirect:/show/student/courses/{studentid}";
    }


    @GetMapping("/teachers")
    public String teacherDisplay(Model model, Staff staff) {
        model.addAttribute("staffs", staffService.findAllStaff());
        model.addAttribute("roles", Staff.role.values());
        model.addAttribute("staff", staff);
        return "teachers";
    }

    @GetMapping("/add_teacher")
    public String showAddTeacher(Staff staff, Model model) {
        model.addAttribute("staff", staff);
        model.addAttribute("roles", Staff.role.values());
        return "add_teachers";
    }

    @PostMapping("/add_teacher")
    public String addTeacher(@ModelAttribute("teacher") Staff staff, @ModelAttribute("role") Staff.role role) {
        staff.setRole(role);
        userService.saveStaff(staff);
        return "redirect:/teachers";
    }

    @GetMapping("/edit/teachers/{id}")
    public String teacherUpdateForm(@PathVariable("id") int id, Model model) {
        Staff staff = (Staff) userService.findById(id);
        model.addAttribute("staff", staff);
        model.addAttribute("staff", staff);
        model.addAttribute("roles", Staff.role.values());
        return "update_staff";
    }

    @PostMapping("/update/teachers/{id}")
    public String updateTeacher(@PathVariable("id") int id, @ModelAttribute("staff") Staff updateStuff, @ModelAttribute("role") Staff.role role) {
        Staff oldStaff = (Staff) userService.findById(id);
        oldStaff.setFirstName(updateStuff.getFirstName());
        oldStaff.setLastName(updateStuff.getLastName());
        oldStaff.setEmail(updateStuff.getEmail());
        oldStaff.setPassword(updateStuff.getPassword());
        oldStaff.setRole(role);
        userService.saveStaff(oldStaff);
        return "redirect:/teachers";
    }

    @GetMapping("/delete/teachers/{id}")
    public String teacherDeleteForm(@PathVariable("id") int id, Model model) {
        Staff staff = (Staff) userService.findById(id);
        model.addAttribute("staff", staff);
        return "delete_teachers";
    }

    @PostMapping("/deleteconfirm/teachers/{id}")
    public String teacherDelete(@PathVariable("id") int id) {
        staffService.deleteById(id);
        return "redirect:/teachers";
    }

    @GetMapping("/show/teachers/courses/{id}")
    public String showTeachersCourses(@PathVariable("id") int id, Model model) {
        List<Course> courses = courseService.findByStaff((Staff) userService.findById(id));
        model.addAttribute("courses", courses);
        model.addAttribute("teacher", userService.findById(id).getLastName());
        return "teacher_courses";
    }

    @PostMapping("/createTable/{id}")
    public String createTable(@PathVariable("id") int id) {
        timeTableService.createTimeTable();
        return "redirect:/plan/admin/{id}";
    }


    @GetMapping("/courses")
    public String adminFunctions(Model model) {
        model.addAttribute("courses", courseService.findAll());
        model.addAttribute(timeTableService);
        return "courses";
    }

    @GetMapping("/courses/assistant/{id}")
    public String assistantCourses(Model model, @PathVariable("id") int id) {
        model.addAttribute("courses", courseService.findByStaff((Staff) userService.findById(id)));
        return "courses";
    }


    @GetMapping("/add_courses")
    public String showAddCourse(Course course, Model model) {
        model.addAttribute("course", course);
        model.addAttribute("teachers", staffService.findAllStaff().stream().map(Staff::getLastName));
        model.addAttribute("types", Course.types.values());
        model.addAttribute("fields", Course.fieldOfStudy.values());
        return "add_courses";
    }

    @PostMapping("/add/course")
    public String addCourse(@ModelAttribute("course") Course course, @RequestParam("teacher") String name, @RequestParam("type") Course.types type, @RequestParam("field") Course.fieldOfStudy field) {
        Staff staff = staffService.findByLastName(name);
        course.setStaff(staff);
        course.setType(type);
        course.setField(field);
        courseService.saveCourse(course);
        return "redirect:/courses";
    }

    @GetMapping("/edit/courses/{id}")
    public String courseUpdateForm(@PathVariable("id") int id, Model model) {
        Course course = courseService.findById(id);
        model.addAttribute("teachers", staffService.findAllStaff().stream().map(Staff::getLastName));
        model.addAttribute("types", Course.types.values());
        model.addAttribute("course", course);
        model.addAttribute("fields", Course.fieldOfStudy.values());
        return "update_courses";
    }

    @PostMapping("/update/courses/{id}")
    public String updateCourse(@PathVariable("id") int id, @ModelAttribute("course") Course updateCourse, @RequestParam("teacher") String teacher, @RequestParam("type") Course.types type, @RequestParam("field") Course.fieldOfStudy field) {
        updateCourse.setStaff(staffService.findByLastName(teacher));
        Course oldCourse = courseService.findById(id);
        oldCourse.setName(updateCourse.getName());
        oldCourse.setDescription(updateCourse.getDescription());
        oldCourse.setStaff(updateCourse.getStaff());
        oldCourse.setHoursPerWeek(updateCourse.getHoursPerWeek());
        oldCourse.setType(type);
        oldCourse.setField(field);
        courseService.saveCourse(oldCourse);
        return "redirect:/courses";
    }

    @GetMapping("/delete/courses/{id}")
    public String courseDeleteForm(@PathVariable("id") int id, Model model) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);
        return "delete_courses";
    }

    @Transactional
    @PostMapping("/deleteconfirm/courses/{id}")
    public String courseDelete(@PathVariable("id") int id) {
        courseService.deleteById(id);
        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/add_preferences")
    public String showAddPreferences(@PathVariable("id") int id, Model model) {
        model.addAttribute("days", days.values());
        model.addAttribute("rooms", roomService.findAll());
        model.addAttribute("hoursperweek", courseService.findById(id).getHoursPerWeek());


        return "add_preferences";
    }

    @PostMapping("/add/preference/{id}")
    public String addPreferences(@PathVariable("id") int id, @RequestParam("time") LocalTime time, @RequestParam("room") int roomId, @RequestParam("day") days day) {
        courseService.saveRoomTimePreference(roomService.findById(roomId), time, day, id);
        return "redirect:/courses";
    }


    @GetMapping("/rooms")
    public String showRooms(Model model) {
        model.addAttribute("rooms", roomService.findAll());

        return "rooms";
    }

    @GetMapping("/add_room")
    public String showaddRoom(Room room, Model model) {
        model.addAttribute("room", room);
        return "add_room";
    }

    @PostMapping("/add/room")
    public String addRoom(@ModelAttribute("room") Room room) {
        roomService.add(room);
        return "redirect:/rooms";
    }

    @GetMapping("/edit/rooms/{id}")
    public String showUpdateRoom(@PathVariable("id") int id, Model model) {
        model.addAttribute("room", roomService.findById(id));
        return "update_room";
    }

    @PostMapping("/update/room/{id}")
    public String updateRoom(@PathVariable("id") int id, @ModelAttribute("room") Room room) {
        roomService.update(roomService.findById(id), room);
        return "redirect:/rooms";
    }


    @GetMapping("/")
    public String showLogin() {
        return "index";
    }

    @PostMapping("/")
    public String login(RedirectAttributes redirectAttributes, Model model, @RequestParam("email") String email, @RequestParam("password") String password) {
        if (userService.loginValid(email, password)) {
            User user = userService.findByEmail(email);
            redirectAttributes.addAttribute("id", user.getId());
            role = userService.role(user);
            switch (role) {
                case admin:
                    return "redirect:/plan/admin/{id}";
                case assistant:
                    return "redirect:/plan/assistant/{id}";
                case student:
                    return "redirect:/plan/student/{id}";
                default:
                    return "redirect:/";
            }

        } else {
            model.addAttribute("error", "Invalid email or password");
            return "index";
        }
    }

    @GetMapping("/plan/student/{id}")
    public String customTable(@PathVariable int id, Model model) {
        List<TimeTable> table = timeTableService.findByCourses(studentCourseService.getByStudent((Student) userService.findById(id)).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList()));
        Collections.sort(table, Comparator.comparing(TimeTable::getDay).thenComparing(TimeTable::getStart));
        model.addAttribute("table", table);
        model.addAttribute("slot", new TimeTable());
        model.addAttribute("role", "student");
        return "plan";
        //for assistants and admins implement  too
    }

    @GetMapping("/plan/admin/{id}")
    public String teacherTable(@PathVariable int id, Model model) {
        List<TimeTable> table = timeTableService.findAll();
        Collections.sort(table, Comparator.comparing(TimeTable::getDay).thenComparing(TimeTable::getStart));
        model.addAttribute("table", table);
        model.addAttribute("slot", new TimeTable());
        model.addAttribute("role", "admin");
        model.addAttribute("preferenceNotUsed", preferenceNotUsed(id));
        return "plan";
    }

    @GetMapping("/plan/assistant/{id}")
    public String assistantTable(@PathVariable int id, Model model) {
        List<TimeTable> table = timeTableService.findByTeacherCourseStaff_Id(id);
        Collections.sort(table, Comparator.comparing(TimeTable::getDay).thenComparing(TimeTable::getStart));
        model.addAttribute("table", table);
        model.addAttribute("slot", new TimeTable());
        model.addAttribute("role", "assistant");
        return "plan";
    }

    public boolean preferenceNotUsed(int id) {
        List<CourseRoomTimePreference> preferences = courseRoomTimePreferenceRepository.findByCourse_Staff_Id(id);
        List<TimeTable> actual = timeTableService.findByTeacherCourseStaff_Id(id);
        if(!preferences.isEmpty() || preferences==null) {
            for (TimeTable slot : actual) {
                boolean in = false;
                for (CourseRoomTimePreference preference : preferences) {
                    if (slot.getStart().equals(preference.getTime()) && slot.getRoom().getId() == preference.getRoom().getId() && slot.getTeacherCourse().getId() == preference.getCourse().getId()) {
                        in = true;
                        break;
                    }
                }
                if (!in) {
                    return true;
                }
            }
        }
        return false;
    }


    @PostMapping("/api/students")
    @ResponseBody
    public Student saveStudent(@RequestBody Student student) {
        return userService.saveStudent(student);
    }

    @PostMapping("/api/admins")
    @ResponseBody
    public Staff saveStaff(@RequestBody Staff[] staffs) {
        for (Staff staff : staffs) {
            userService.saveStaff(staff);
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
        return roomService.add(room);
    }


}

