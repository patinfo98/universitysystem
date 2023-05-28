package com.example.universitysystem.controller;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.*;
import com.example.universitysystem.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.hibernate.usertype.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    @Autowired
    private HttpSession session;
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

    private int sessionId(){
        return (int)session.getAttribute("userId");
    }

    private boolean accessAllowed(UserService.userType role){
        if(session.getAttribute("userId")!= null && userService.role(userService.findById(sessionId())).equals(role)) {
            return true;
        }
        return false;
        }

     private boolean isSelf (UserService.userType role, int id){
         return (accessAllowed(role) && sessionId()==id);
     }

    @GetMapping("/students")
    public String adminFunctions(Model model, Student student) {
        if(accessAllowed(UserService.userType.admin)){
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("student", student);
            return "students";
        }
        return "errorPage";
    }

    @GetMapping("/add_students")
    public String showAddStudent(Student student, Model model) {
        if(accessAllowed(UserService.userType.admin)) {
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
        if(accessAllowed(UserService.userType.admin)) {
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
        oldStudent.setPassword(updateStudent.getPassword());
        oldStudent.setStudentNumber(updateStudent.getStudentNumber());
        userRepository.save(oldStudent);
        return "redirect:/students";
    }

    @PostMapping("/deleteconfirm/{id}")
    public String studentDelete(@PathVariable("id") int id) {
        studentService.deleteById(id);
        return "redirect:/students";
    }

    @GetMapping("add/student/course/{id}")
    public String addStudentCourse(@PathVariable("id") int id, Model model) {
        if(accessAllowed(UserService.userType.admin) || isSelf(UserService.userType.student, id)) {
            boolean error1 = model.getAttribute("error") == null ? false : (boolean) model.getAttribute("error");
            int selectedCourse = model.getAttribute("selectedCourseId") == null ? 1 : (int) model.getAttribute("selectedCourseId");
            List<Course> courses = courseRepository.findAll();
            List<Course> coursesStudent = studentCourseRepository.findAllByStudent((Student) userRepository.findById(id)).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList());
            courses.removeAll(coursesStudent);
            model.addAttribute("courses", courses);
            model.addAttribute("warning", "This course has an overlap with one of your other courses!");
            model.addAttribute("error", error1);
            model.addAttribute("selectedId", selectedCourse);
            return "add_student_courses";
        }
        return "errorPage";
    }
    @PostMapping("add/student/course/{id}/anyway")
    public String addCourseAnyway(@PathVariable("id") int id, @RequestParam("course") int courseId, Model model, RedirectAttributes redirectAttributes) {
        studentCourseService.add((Student)userRepository.findById(id), courseRepository.findById(courseId));
        boolean error = false;
        redirectAttributes.addFlashAttribute("error", error);
        return "redirect:/show/student/courses/{id}";
    }


    @PostMapping("add/student/course/{id}")
    public String addCourse(@PathVariable("id") int id, @RequestParam("course") int courseId, Model model, RedirectAttributes redirectAttributes) {
        Student student = (Student) userRepository.findById(id);
        Course course = courseRepository.findById(courseId);
        List<TimeTable> courseTimes = timetableRepository.findByTeacherCourse(courseRepository.findById(courseId));
        List<Course> studentCourses = studentCourseRepository.findAllByStudent(student).stream().map(x -> x.getTeacherCourse()).collect(Collectors.toList());
        List<TimeTable> studentTimes = timetableRepository.findByTeacherCourseIn(studentCourses);
        boolean error = timeTableService.checkCourseOverlap(studentTimes, courseTimes);
        if(error){
            redirectAttributes.addFlashAttribute("error", error);
            redirectAttributes.addFlashAttribute("selectedCourseId", courseId);
            return "redirect:/add/student/course/{id}";
                }
        else {
            studentCourseService.add(student, course);
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/show/student/courses/{id}";
        }
    }


    @GetMapping("/show/student/courses/{id}")
    public String showCourses(@PathVariable("id") int id, Model model) {
        UserService.userType userrole = userService.role(userService.findById(sessionId()));
        if(accessAllowed(UserService.userType.admin) || isSelf(UserService.userType.student, id)) {
            List<StudentCourse> courses = studentCourseRepository.findAllByStudent((Student) userRepository.findById(id));
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


    @GetMapping("/teachers")
    public String teacherDisplay(Model model, Staff staff) {
        if(accessAllowed(UserService.userType.admin)) {
            model.addAttribute("staffs", staffRepository.findAll());
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
        if(accessAllowed(UserService.userType.admin)) {
            Staff staff = (Staff) userRepository.findById(id);
            model.addAttribute("staff", staff);
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
        oldStaff.setPassword(updateStuff.getPassword());
        oldStaff.setRole(role);
        userRepository.save(oldStaff);
        return "redirect:/teachers";
    }

    @PostMapping("/deleteconfirm/teachers/{id}")
    public String teacherDelete(@PathVariable("id") int id) {
        staffService.deleteById(id);
        return "redirect:/teachers";
    }

    @GetMapping("/show/teachers/courses/{id}")
    public String showTeachersCourses(@PathVariable("id") int id, Model model) {
        if(accessAllowed(UserService.userType.admin)) {
            List<Course> courses = courseRepository.findByStaff((Staff) userRepository.findById(id));
            model.addAttribute("courses", courses);
            model.addAttribute("teacher", userRepository.findById(id).getLastName());
            return "teacher_courses";
        }
        return "errorPage";
    }

    @PostMapping("/createTable")
    public String createTable() {
        timeTableService.createTimeTable();
        return "redirect:/plan/admin";
    }


    @GetMapping("/courses")
    public String adminFunctions(Model model, Course course) {
        if(accessAllowed(UserService.userType.admin)){
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute(timeTableService);
        model.addAttribute("course", course);
        model.addAttribute("teachers", staffRepository.findAll().stream().map(Staff::getLastName));
        model.addAttribute("types", Course.types.values());
        model.addAttribute("fields", Course.fieldOfStudy.values());
        model.addAttribute("role", UserService.userType.admin);
        return "courses";}
        else if(accessAllowed(UserService.userType.assistant)){
            model.addAttribute("courses", courseRepository.findByStaffId(sessionId()));
            model.addAttribute(timeTableService);
            model.addAttribute("course", course);
            model.addAttribute("teachers", staffRepository.findAll().stream().map(Staff::getLastName));
            model.addAttribute("types", Course.types.values());
            model.addAttribute("fields", Course.fieldOfStudy.values());
            model.addAttribute("role", UserService.userType.assistant);
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
        if(accessAllowed(UserService.userType.admin) || isSelf(UserService.userType.assistant, course.getStaff().getId())){
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
        updateCourse.setStaff(staffRepository.findByLastName(teacher));
        Course oldCourse = courseRepository.findById(id);
        oldCourse.setName(updateCourse.getName());
        oldCourse.setDescription(updateCourse.getDescription());
        oldCourse.setStaff(updateCourse.getStaff());
        oldCourse.setHoursPerWeek(updateCourse.getHoursPerWeek());
        oldCourse.setType(type);
        oldCourse.setField(field);
        courseRepository.save(oldCourse);
        return "redirect:/courses";
    }

    @Transactional
    @PostMapping("/deleteconfirm/courses/{id}")
    public String courseDelete(@PathVariable("id") int id) {
        courseService.deleteById(id);
        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/add_preferences")
    public String showAddPreferences(@PathVariable("id") int id, Model model) {
        if (accessAllowed(UserService.userType.admin) || isSelf(UserService.userType.assistant, courseRepository.findById(id).getStaff().getId())) {
            model.addAttribute("days", days.values());
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("hoursperweek", courseRepository.findById(id).getHoursPerWeek());
            return "add_preferences";
        }
        else return "errorPage";
    }



    @PostMapping("/add/preference/{id}")
    public String addPreferences(@PathVariable("id") int id, @RequestParam("time") LocalTime time, @RequestParam("room") int roomId, @RequestParam("day") days day, @RequestParam("time2") LocalTime time2) {
        courseService.saveRoomTimePreference(roomRepository.findById(roomId), time, time2, day, id);
        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/add_times")
    public String changeTime(@PathVariable("id") int id, Model model) {
        if (accessAllowed(UserService.userType.admin) || isSelf(UserService.userType.assistant, courseRepository.findById(id).getStaff().getId())) {
            model.addAttribute("days", days.values());
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("hoursperweek", courseRepository.findById(id).getHoursPerWeek());
            return "edit_timetable";
        }
        return "errorPage";
    }

    @GetMapping("courses/{id}/coursetimes")
    public String courseTimes(@PathVariable("id") int id, Model model) {
        if(accessAllowed(UserService.userType.admin)) {
            boolean error1 = model.getAttribute("error") == null ? false : (boolean) model.getAttribute("error");
            model.addAttribute("times", timetableRepository.findByTeacherCourseId(id));
            model.addAttribute("days", days.values());
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("hoursperweek", timeTableService.timeLeft(courseRepository.findById(id)));
            model.addAttribute("warning", "A course with this time/room combination already exists!");
            model.addAttribute("error", error1);
            System.out.println(error1);
            return "coursetimes";
        }
        return "errorPage";
    }

    @PostMapping("/edit/timetable/{id}")
    public String editTime(@PathVariable("id") int courseid,  @RequestParam("time") LocalTime time, @RequestParam("time2") LocalTime time2, @RequestParam("room") int roomId, @RequestParam("day") days day ){
        timeTableService.save(courseid, time, time2, roomRepository.findById(roomId), day);
        return "redirect:/courses";
    }






    @GetMapping("/rooms")
    public String showRooms(Model model, Room room) {
        if(accessAllowed(UserService.userType.admin)) {
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("room", room);
            return "rooms";
        }
        return "errorPage";
    }

    @PostMapping("/add/room")
    public String addRoom(@ModelAttribute("room") Room room) {
        roomRepository.save(room);
        return "redirect:/rooms";
    }

    @GetMapping("/edit/rooms/{id}")
    public String showUpdateRoom(@PathVariable("id") int id, Model model) {
        if(accessAllowed(UserService.userType.admin)) {
            model.addAttribute("room", roomRepository.findById(id));
            return "update_room";
        }
        return "errorPage";
    }

    @PostMapping("/update/room/{id}")
    public String updateRoom(@PathVariable("id") int id, @ModelAttribute("room") Room room) {
        roomService.update(roomRepository.findById(id), room);
        return "redirect:/rooms";
    }


    @PostMapping("/delete/room/{id}")
    public String deleteRoom(@PathVariable("id") int id){
        roomService.delete(roomRepository.findById(id));
        return "redirect:/rooms";
    }


    @GetMapping("/")
    public String showLogin() {
        return "index";
    }

    @PostMapping("/")
    public String login(RedirectAttributes redirectAttributes, Model model, @RequestParam("email") String email, @RequestParam("password") String password) {
        if (userService.loginValid(email, password)) {
            User user = userRepository.findByEmail(email);
            session.setAttribute("userId", user.getId());
            redirectAttributes.addAttribute("id", user.getId());
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

    @GetMapping("/plan/student")
    public String customTable(Model model) {
        if(accessAllowed(UserService.userType.student)) {
            int userid = (int) session.getAttribute("userId");
            List<TimeTable> table = timetableRepository.findByTeacherCourseIn(studentCourseRepository.findAllByStudent((Student) userRepository.findById(userid)).stream().map(StudentCourse::getTeacherCourse).collect(Collectors.toList()));
            Collections.sort(table, Comparator.comparing(TimeTable::getDay).thenComparing(TimeTable::getStart));
            model.addAttribute("table", table);
            model.addAttribute("slot", new TimeTable());
            model.addAttribute("role", "student");
            model.addAttribute("id", userid);
            return "plan";
        }
        return "errorPage";
    }

    @GetMapping("/plan/admin")
    public String teacherTable(Model model) {
        if(accessAllowed(UserService.userType.admin)){
            int id = sessionId();
            User user = userRepository.findById(id);
            if(user.getFirstLogin()){
                model.addAttribute("firstLogin", true);
                user.setFirstLogin(false);
                userRepository.save(user);
            }
        List<TimeTable> table = timetableRepository.findAll();
        Collections.sort(table, Comparator.comparing(TimeTable::getDay).thenComparing(TimeTable::getStart));
        model.addAttribute("table", table);
        model.addAttribute("slot", new TimeTable());
        model.addAttribute("role", "admin");
        model.addAttribute("preferenceNotUsed", preferenceNotUsed(id));
        return "plan";
        }
        return "errorPage";
    }

    @GetMapping("/plan/assistant")
    public String assistantTable(Model model) {
        if(accessAllowed(UserService.userType.assistant)) {
            int id = sessionId();
            User user = userRepository.findById(id);
            if (user.getFirstLogin()) {
                model.addAttribute("firstLogin", true);
                user.setFirstLogin(false);
                userRepository.save(user);
            }
            List<TimeTable> table = timeTableService.findByTeacherCourseStaff_Id(id);
            Collections.sort(table, Comparator.comparing(TimeTable::getDay).thenComparing(TimeTable::getStart));
            model.addAttribute("table", table);
            model.addAttribute("slot", new TimeTable());
            model.addAttribute("role", "assistant");
            model.addAttribute("preferenceNotUsed", preferenceNotUsed(id));
            return "plan";
        }
        return "errorPage";
    }

    @PostMapping("/delete/times/{id}/{id2}")
    public String delete(@PathVariable("id") int id, @PathVariable("id2") int id2){
        timetableRepository.deleteById(id);
        return "redirect:/courses/{id2}/coursetimes";
    }

    @PostMapping("/add/times/{id}")
    public String addtimes(@PathVariable("id") int id, @RequestParam("time") LocalTime time, @RequestParam("room") int roomId, @RequestParam("day") days day, @RequestParam("time2") LocalTime time2, RedirectAttributes redirectAttributes) {
        List<TimeTable> timeOverlap = timeTableService.hasOverlap(time,time2, day);
        List<Integer> roomOverlap = timeOverlap.stream().map(x -> x.getRoom().getId()).collect(Collectors.toList());
        List<Course.fieldOfStudy> fieldOverlap = timeOverlap.stream().map(x -> x.getTeacherCourse().getField()).collect(Collectors.toList());
        boolean error = false;
        if(timeOverlap.isEmpty() || (!fieldOverlap.contains(courseRepository.findById(id).getField()) && !roomOverlap.contains(roomRepository.findById(roomId)))){
            timeTableService.save(id, time, time2, roomRepository.findById(roomId), day);
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/courses/{id}/coursetimes";
        }
        else {
            error=true;
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/courses/{id}/coursetimes";
        }



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

