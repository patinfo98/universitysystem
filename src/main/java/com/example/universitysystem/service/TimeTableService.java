package com.example.universitysystem.service;

import com.example.universitysystem.model.*;
import com.example.universitysystem.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeTableService {
    @PersistenceContext
    private EntityManager entityManager;
    private final RoomService roomService;
    private final Random rand = new Random();
    private final List<TimeTable> timeslots = new ArrayList<TimeTable>();
    private final TimetableRepository timetableRepository;
    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final CourseService courseService;
    private final CourseRepository courseRepository;

    public TimeTableService(TimetableRepository timetableRepository, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository, RoomService roomService, CourseService courseService, RoomRepository roomRepository, CourseRepository courseRepository, UserRepository userRepository) {
        this.timetableRepository = timetableRepository;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
        this.roomService = roomService;
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

    public boolean courseFull(CourseRoomTimePreference preference) {
        if (timetableRepository.existsByTeacherCourseId(preference.getCourse().getId())) {
            List<TimeTable> allCourseTimes = timetableRepository.findByTeacherCourseId(preference.getCourse().getId());
            List<Integer> courseTimes = allCourseTimes.stream().map(x -> Duration.between(x.getStart(), x.getEnd()).toHoursPart()).collect(Collectors.toList());
            if (courseTimes.stream().mapToInt(Integer::intValue).sum() == courseRepository.findById(preference.getCourse().getId()).getHoursPerWeek()) {
                return true;
            }
        }
        return false;
    }

    public void createTimeTable() {
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

        for (days day : days.values()) {
            List<Course> courses = courseService.findNotFull();
            for (Course course : courses) {
                for (int i = 8; i <= 12; i++) {
                    LocalTime time = LocalTime.of(i, 0);
                    List<TimeTable> overlapCourses = hasOverlap(time, timeLeft(course) > 2 ? time.plusHours(timeLeft(course) / 2) : time.plusHours(timeLeft(course)), day);
                    if (overlapCourses.isEmpty()) {
                        save(course.getId(), time, timeLeft(course) > 2 ? time.plusHours(timeLeft(course) / 2) : time.plusHours(timeLeft(course)), freeRoom(time, timeLeft(course) > 2 ? time.plusHours(timeLeft(course) / 2) : time.plusHours(timeLeft(course)), overlapCourses).get(0), day);
                        break;
                    } else {
                        List<Course.fieldOfStudy> fields = overlapCourses.stream().map(x -> x.getTeacherCourse().getField()).collect(Collectors.toList());
                        if (!fields.contains(course.getField())) {
                            Room room;
                            if (!freeRoom(time, time.plusHours(timeLeft(course)), overlapCourses).isEmpty()) {
                                save(course.getId(), time, timeLeft(course) > 2 ? time.plusHours(timeLeft(course) / 2) : time.plusHours(timeLeft(course)), freeRoom(time, timeLeft(course) > 2 ? time.plusHours(timeLeft(course) / 2) : time.plusHours(timeLeft(course)), overlapCourses).get(0), day);
                                break;
                            }
                        }
                    }

                }

            }
        }
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

    public boolean sameStudy(Course course) {
        return (boolean) entityManager.createNamedQuery("sameStudy").setParameter("field", course.getField()).getSingleResult();
    }

    public boolean checkCourseOverlap(List<TimeTable> studentTimes, List<TimeTable> courseTimes) {
        for (TimeTable c1 : studentTimes) {
            for (TimeTable c2 : courseTimes) {
                if (c1.getStart().isBefore(c2.getEnd()) && c1.getEnd().isAfter(c2.getStart())) {
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
}










