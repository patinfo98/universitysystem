/*
 * TimeTable Controller
 * methods in connection with the display of TimeTable objects
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.controller;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.TimeTable;
import com.example.universitysystem.model.days;
import com.example.universitysystem.repository.CourseRepository;
import com.example.universitysystem.repository.RoomRepository;
import com.example.universitysystem.repository.TimetableRepository;
import com.example.universitysystem.service.CourseService;
import com.example.universitysystem.service.TimeTableService;
import com.example.universitysystem.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TimeTableController {
    private final UserService userService;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final CourseService courseService;
    private final TimetableRepository timetableRepository;
    private final TimeTableService timeTableService;

    public TimeTableController(UserService userService, CourseRepository courseRepository, RoomRepository roomRepository, CourseService courseService, TimetableRepository timetableRepository, TimeTableService timeTableService) {
        this.userService = userService;
        this.courseRepository = courseRepository;
        this.roomRepository = roomRepository;
        this.courseService = courseService;

        this.timetableRepository = timetableRepository;
        this.timeTableService = timeTableService;
    }

    @GetMapping("/courses/{id}/add_preferences")
    public String showAddPreferences(@PathVariable("id") int id, Model model) {
        if (userService.accessAllowed(UserService.userType.admin) || userService.isSelf(UserService.userType.assistant, courseRepository.findById(id).getStaff().getId())) {
            model.addAttribute("days", days.values());
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("hoursperweek", courseRepository.findById(id).getHoursPerWeek());
            return "add_preferences";
        } else return "errorPage";
    }

    @PostMapping("/add/preference/{id}")
    public String addPreferences(@PathVariable("id") int id, @RequestParam("time") LocalTime time, @RequestParam("room") int roomId, @RequestParam("day") days day, @RequestParam("time2") LocalTime time2) {
        courseService.saveRoomTimePreference(roomRepository.findById(roomId), time, time2, day, id);
        return "redirect:/courses";
    }


    @GetMapping("courses/{id}/coursetimes")
    public String courseTimes(@PathVariable("id") int id, Model model) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            boolean error1 = model.getAttribute("error") != null && (boolean) model.getAttribute("error");
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

    @PostMapping("/delete/times/{id}/{id2}")
    public String delete(@PathVariable("id") int id, @PathVariable("id2") int id2) {
        timetableRepository.deleteById(id);
        return "redirect:/courses/{id2}/coursetimes";
    }

    @PostMapping("/add/times/{id}")
    public String addtimes(@PathVariable("id") int id, @RequestParam("time") LocalTime time, @RequestParam("room") int roomId, @RequestParam("time2") LocalTime time2, @RequestParam("date") LocalDate date, RedirectAttributes redirectAttributes) {
        List<TimeTable> timeOverlap = timeTableService.hasOverlap(time, time2, days.valueOf(date.getDayOfWeek().name().toLowerCase()));
        List<TimeTable> realOverlap = new ArrayList<TimeTable>();
        for(TimeTable i: timeOverlap){
            if(i.getDate().isEqual(date)){
                realOverlap.add(i);
            }
        }
        List<Integer> roomOverlap = realOverlap.stream().filter(x -> x.getRoom().getId() == roomId).map(x->x.getRoom().getId()).collect(Collectors.toList());
        List<Course.fieldOfStudy> fieldOverlap = realOverlap.stream().filter(x -> x.getTeacherCourse().getField() == courseRepository.findById(id).getField()).map(x -> x.getTeacherCourse().getField()).collect(Collectors.toList());
        boolean error = false;
        if (realOverlap.isEmpty() || (fieldOverlap.isEmpty() && roomOverlap.isEmpty())) {
            timeTableService.saveDateIncluded(id, time, time2, roomRepository.findById(roomId), days.valueOf(date.getDayOfWeek().name().toLowerCase()), date);
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/courses/{id}/coursetimes";
        } else {
            error = true;
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/courses/{id}/coursetimes";
        }


    }

    @PostMapping("/createTable")
    public String createTable(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("valid", timeTableService.createTimeTable());
        return "redirect:/plan";
    }

}
