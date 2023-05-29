/*
 * TimeTableService
 * methods in connection with the TimeTable objects
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.service;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeTableService {

    private final TimetableRepository timetableRepository;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public TimeTableService(TimetableRepository timetableRepository, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository, CourseService courseService, RoomRepository roomRepository, CourseRepository courseRepository, UserRepository userRepository) {
        this.timetableRepository = timetableRepository;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
        this.courseService = courseService;
        this.roomRepository = roomRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;


    }

    public List<TimeTable> findByTeacherCourseStaff_Id(int id) {
        return timetableRepository.findByTeacherCourse_Staff_Id(id);
    }

    public int timeLeft(Course course) {
        List<TimeTable> table = timetableRepository.findByTeacherCourseId(course.getId());
        int timeUsed = table.stream().map(x -> Duration.between(x.getStart(), x.getEnd()).toHoursPart()).collect(Collectors.toList()).stream().mapToInt(Integer::intValue).sum();
        int time = Duration.ofHours(course.getHoursPerWeek()).minus(Duration.ofHours(timeUsed)).toHoursPart();
        return time;
    }

    @Transactional
    public void save(int id, LocalTime start, LocalTime end, Room room, days day) {
        TimeTable table = new TimeTable();
        table.setTeacherCourse(courseRepository.findById(id));
        table.setDay(day);
        table.setRoom(room);
        table.setStart(start);
        table.setEnd(end);
        timetableRepository.save(table);
    }

    @Transactional
    public void saveDateIncluded(int id, LocalTime start, LocalTime end, Room room, days day, LocalDate date) {
        TimeTable table = new TimeTable();
        table.setTeacherCourse(courseRepository.findById(id));
        table.setDay(day);
        table.setRoom(room);
        table.setStart(start);
        table.setEnd(end);
        table.setDate(date);
        timetableRepository.save(table);
    }

    public boolean courseFull(CourseRoomTimePreference preference) {
        if (timetableRepository.existsByTeacherCourseId(preference.getCourse().getId())) {
            List<TimeTable> allCourseTimes = timetableRepository.findByTeacherCourseId(preference.getCourse().getId());
            List<Integer> courseTimes = allCourseTimes.stream().map(x -> Duration.between(x.getStart(), x.getEnd()).toHoursPart()).collect(Collectors.toList());
            return courseTimes.stream().mapToInt(Integer::intValue).sum() == courseRepository.findById(preference.getCourse().getId()).getHoursPerWeek();
        }
        return false;
    }

    public boolean createTimeTable() {
        userRepository.updateFirstLogin();
        timetableRepository.deleteAll();
        List<CourseRoomTimePreference> preferences = courseRoomTimePreferenceRepository.findAll();
        for (CourseRoomTimePreference preference : preferences) {
            if (!courseFull(preference)) {
                List<TimeTable> overlapCourses = hasOverlap(preference.getTime(), preference.getEndtime(), preference.getDay());
                if (overlapCourses.isEmpty()) {
                    save(preference.getCourse().getId(), preference.getTime(), preference.getTime().plusHours(timeLeft(courseRepository.findById(preference.getCourse().getId()))), preference.getRoom(), preference.getDay());
                } else {
                    List<Course.fieldOfStudy> fields = overlapCourses.stream().map(x -> x.getTeacherCourse().getField()).collect(Collectors.toList());
                    if (!fields.contains(preference.getCourse().getField())) {
                        Room room = preference.getRoom();
                        List<Integer> usedRooms = overlapCourses.stream().map(TimeTable::getRoom).map(Room::getId).collect(Collectors.toList());
                        if (!usedRooms.contains(room.getId())) {
                            save(preference.getCourse().getId(), preference.getTime(), preference.getTime().plusHours(timeLeft(courseRepository.findById(preference.getCourse().getId()))), preference.getRoom(), preference.getDay());
                        } else if (!freeRoom(preference.getTime(), preference.getEndtime(), overlapCourses).isEmpty()) {
                            TimeTable timeSlot = new TimeTable();
                            save(preference.getCourse().getId(), preference.getTime(), preference.getTime().plusHours(timeLeft(courseRepository.findById(preference.getCourse().getId()))), freeRoom(preference.getTime(), preference.getEndtime(), overlapCourses).get(0), preference.getDay());
                        }
                    }
                }
            }
        }
        int k = 9;
        for (int j = 0; j < 3; j++) {
            k += 3;
            for (days day : days.values()) {
                List<Course> courses = courseService.findNotFull();
                for (Course course : courses) {
                    for (int i = 8; i <= k; i++) {
                        LocalTime time = LocalTime.of(i, 0);
                        int left = timeLeft(course);
                        List<TimeTable> overlapCourses = hasOverlap(time, left > 2 ? time.plusHours(left / 2) : time.plusHours(left), day);
                        if (overlapCourses.isEmpty()) {
                            save(course.getId(), time, left > 2 ? time.plusHours(left / 2) : time.plusHours(left), freeRoom(time, left > 2 ? time.plusHours(left / 2) : time.plusHours(left), overlapCourses).get(0), day);
                            break;
                        } else {
                            List<Course.fieldOfStudy> fields = overlapCourses.stream().map(x -> x.getTeacherCourse().getField()).collect(Collectors.toList());
                            if (!fields.contains(course.getField())) {
                                if (!freeRoom(time, left > 2 ? time.plusHours(left / 2) : time.plusHours(left), overlapCourses).isEmpty()) {
                                    save(course.getId(), time, left > 2 ? time.plusHours(left / 2) : time.plusHours(left), freeRoom(time, left > 2 ? time.plusHours(left / 2) : time.plusHours(left), overlapCourses).get(0), day);
                                    break;
                                }
                            }
                        }

                    }

                }
            }
        }
        LocalDate date = LocalDate.of(2023, 5, 29);
        int i = -1;
        for (days day : days.values()) {
            List<TimeTable> times = timetableRepository.findByDay(day);
            i++;
            for (TimeTable time : times) {
                time.setDate(date.plusDays(i));
                timetableRepository.save(time);


                for (int j = 7; j < 22; j += 7) {
                    TimeTable table = new TimeTable();
                    table.setDate(date.plusDays(i + j));
                    table.setDay(day);
                    table.setRoom(time.getRoom());
                    table.setStart(time.getStart());
                    table.setEnd(time.getEnd());
                    table.setTeacherCourse(time.getTeacherCourse());
                    timetableRepository.save(table);
                }

            }
        }
        return courseService.findNotFull().isEmpty();
    }


    public List<TimeTable> hasOverlap(LocalTime startTime, LocalTime endTime, days day) {
        return (List<TimeTable>) entityManager.createNamedQuery("overlapEntries").setParameter("end", endTime).setParameter("start", startTime).setParameter("day", day).getResultList();

    }

    public List<Room> freeRoom(LocalTime start, LocalTime end, List<TimeTable> tables) {
        List<Integer> rooms = roomRepository.findAll().stream().map(Room::getId).collect(Collectors.toList());
        List<Integer> table = tables.stream().map(TimeTable::getRoom).map(Room::getId).collect(Collectors.toList());
        rooms.removeAll(table);
        return roomRepository.findAllById(rooms);

    }

    public boolean checkCourseOverlap(List<TimeTable> studentTimes, List<TimeTable> courseTimes) {
        for (TimeTable c1 : studentTimes) {
            for (TimeTable c2 : courseTimes) {
                if (c1.getStart().isBefore(c2.getEnd()) && c1.getEnd().isAfter(c2.getStart()) && c1.getDate().equals(c2.getDate())) {
                    boolean x = true;
                    System.out.println(x);
                    return true;
                }
            }
        }
        boolean x = false;
        System.out.println(x);
        return false;
    }

    public boolean preferenceNotUsed(int id) {
        List<CourseRoomTimePreference> preferences = courseRoomTimePreferenceRepository.findByCourse_Staff_Id(id);
        List<TimeTable> actual = findByTeacherCourseStaff_Id(id);
        if (!preferences.isEmpty() || preferences == null) {
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

    public LocalTime[][] alltimes() {
        LocalTime[][] allTimes = new LocalTime[15][2];
        for (int i = 0; i < 15; i++) {
            allTimes[i][0] = LocalTime.of(i + 8, 0);
            allTimes[i][1] = LocalTime.of(i + 9, 0);
        }
        return allTimes;
    }
}










