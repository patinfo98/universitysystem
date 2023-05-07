package com.example.universitysystem.service;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.CourseRepository;
import com.example.universitysystem.repository.CourseRoomTimePreferenceRepository;
import com.example.universitysystem.repository.RoomRepository;
import com.example.universitysystem.repository.TimetableRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class TimeTableService {
    private final RoomService roomService;
    private final Random rand = new Random();
    private final List<TimeTable> timeslots = new ArrayList<TimeTable>();
    private final TimetableRepository timetableRepository;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;

    private final RoomRepository roomRepository;
    private final CourseService courseService;
    private final CourseRepository courseRepository;

    public TimeTableService(TimetableRepository timetableRepository, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository, RoomService roomService, CourseService courseService, RoomRepository roomRepository, CourseRepository courseRepository) {
        this.timetableRepository = timetableRepository;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
        this.roomService = roomService;
        this.courseService = courseService;
        this.roomRepository = roomRepository;
        this.courseRepository = courseRepository;


    }


    public List<TimeTable> findAll() {
        return timetableRepository.findAll();
    }

    public List<TimeTable> findByCourses(List<Course> courses) {
        return timetableRepository.findByTeacherCourseIn(courses);
    }

    public List<TimeTable> findByTeacherCourseStaff_Id(int id) {
        return timetableRepository.findByTeacherCourse_Staff_Id(id);
    }


    public boolean existsOverlap(CourseRoomTimePreference preference, List<TimeTable> timeTable) {
        timeslots.clear();
        LocalTime start = preference.getTime();
        Duration time = Duration.ofHours(preference.getCourse().getHoursPerWeek());
        for (TimeTable timeslot : timeTable) {
            LocalTime start2 = timeslot.getStart();
            Duration time2 = Duration.ofHours(timeslot.getTeacherCourse().getHoursPerWeek());
            if ((((start.isBefore(start2) || start.equals(start2)) && start.plus(time).isAfter(start2)) || ((start2.isBefore(start) || start.equals(start2)) && start2.plus(time2).isAfter(start)))) {
                timeslots.add(timeslot);
            }
        }
        return !timeslots.isEmpty();
    }

    public boolean overlap(List<TimeTable> timeslot, LocalTime start, Course course) {
        timeslots.clear();
        Duration time = Duration.ofHours(course.getHoursPerWeek());
        for (TimeTable slot : timeslot) {
            LocalTime start2 = slot.getStart();
            Duration time2 = Duration.ofHours(slot.getTeacherCourse().getHoursPerWeek());
            if ((((start.isBefore(start2) || start.equals(start2)) && start.plus(time).isAfter(start2)) || ((start2.isBefore(start) || start.equals(start2)) && start2.plus(time2).isAfter(start)))) {
                timeslots.add(slot);
            }
        }
        return !timeslots.isEmpty();
    }

    public void save(int id, LocalTime start, Room room, days day){
        timetableRepository.deleteByTeacherCourseId(id);
        TimeTable table = new TimeTable();
        table.setTeacherCourse(courseService.findById(id));
        table.setDay(day);
        table.setRoom(room);
        table.setStart(start);
        table.setEnd(start.plus(Duration.ofHours(courseService.findById(id).getHoursPerWeek())));
        timetableRepository.save(table);
    }


    //räume checken
    //wenn overlap dann unterscheidung ob selbes studium wenn ja dann weiter wenn nein anderer raum
    public void createTimeTable() {
        timetableRepository.deleteAll();
        for (days day : days.values()) {
            for (CourseRoomTimePreference preference : courseRoomTimePreferenceRepository.findByDay(day)) {
                if (!timetableRepository.existsByTeacherCourseId(preference.getCourse().getId())) {
                    if (!existsOverlap(preference, timetableRepository.findByDay(day))) {
                        TimeTable timeSlot = new TimeTable();
                        timeSlot.setTeacherCourse(preference.getCourse());
                        timeSlot.setDay(preference.getDay());
                        timeSlot.setStart(preference.getTime());
                        timeSlot.setRoom(preference.getRoom());
                        timeSlot.setEnd(timeSlot.getStart().plus(Duration.ofHours(timeSlot.getTeacherCourse().getHoursPerWeek())));
                        timetableRepository.save(timeSlot);
                    } else {
                        boolean sameStudyExists = false;
                        for (TimeTable timeslot : timeslots) {
                            if (timeslot.getTeacherCourse().getField().equals(preference.getCourse().getField())) {
                                sameStudyExists = true;
                                break;
                            }
                        }
                        if (!sameStudyExists) {
                            Room room = preference.getRoom();
                            List<Integer> used = timeslots.stream().map(TimeTable::getRoom).map(Room::getId).collect(Collectors.toList());
                            if (!used.contains(room.getId())) {
                                TimeTable timeSlot = new TimeTable();
                                timeSlot.setTeacherCourse(preference.getCourse());
                                timeSlot.setDay(preference.getDay());
                                timeSlot.setStart(preference.getTime());
                                timeSlot.setRoom(room);
                                timeSlot.setEnd(timeSlot.getStart().plus(Duration.ofHours(timeSlot.getTeacherCourse().getHoursPerWeek())));
                                timetableRepository.save(timeSlot);
                            }
                        }
                    }
                }
            }
        }
        for (days day : days.values()) {

            List<Course> courses = courseRepository.findCoursesNotInTimetable();
            List<Room> rooms = roomRepository.findAll();
            for (Course course : courses) {
                for (int i = 8; i <= 14; i++) {
                    LocalTime time = LocalTime.of(i, 0);
                    if (!overlap(timetableRepository.findByDay(day), time, course)) {

                        Room room = rooms.get(rand.nextInt(0, rooms.size()));
                        TimeTable slot = new TimeTable();
                        slot.setTeacherCourse(course);
                        slot.setDay(day);
                        slot.setStart(time);
                        slot.setRoom(room);
                        slot.setEnd(slot.getStart().plus(Duration.ofHours(slot.getTeacherCourse().getHoursPerWeek())));

                        timetableRepository.save(slot);
                        break;
                    } else {
                        boolean sameStudyExists = false;
                        for (TimeTable timeslot : timeslots) {
                            if (timeslot.getTeacherCourse().getField().equals(course.getField())) {
                                sameStudyExists = true;
                                break;
                            }
                        }
                        if (!sameStudyExists && timeslots != null && !timeslots.isEmpty()) {
                            Room room;
                            List<Integer> blocked = timeslots.stream().map(TimeTable::getRoom).map(Room::getId).collect(Collectors.toList());
                            Collections.sort(blocked);
                            List<Integer> all = roomRepository.findAll().stream().map(Room::getId).collect(Collectors.toList());
                            Collections.sort(all);
                            if (!blocked.equals(all)) {
                                room = rooms.get(rand.nextInt(0, rooms.size()));

                                while (blocked.contains(room.getId())) {
                                    room = rooms.get(rand.nextInt(0, rooms.size()));
                                }
                                System.out.println(room.getName());
                                TimeTable timeSlot2 = new TimeTable();
                                timeSlot2.setTeacherCourse(course);
                                timeSlot2.setDay(day);
                                timeSlot2.setStart(time);
                                timeSlot2.setRoom(room);
                                timeSlot2.setEnd(timeSlot2.getStart().plus(Duration.ofHours(timeSlot2.getTeacherCourse().getHoursPerWeek())));
                                timetableRepository.save(timeSlot2);
                                break;
                            }
                        }
                    }

                }


            }
        }
    }
}








