/*
 * Room Controller
 * methods in connection with Room objects
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.service;

import com.example.universitysystem.model.Room;
import com.example.universitysystem.repository.CourseRoomTimePreferenceRepository;
import com.example.universitysystem.repository.RoomRepository;
import com.example.universitysystem.repository.TimetableRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    private final CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository;
    private final TimetableRepository timetableRepository;

    public RoomService(RoomRepository roomRepository, CourseRoomTimePreferenceRepository courseRoomTimePreferenceRepository, TimetableRepository timetableRepository) {
        this.roomRepository = roomRepository;
        this.courseRoomTimePreferenceRepository = courseRoomTimePreferenceRepository;
        this.timetableRepository = timetableRepository;
    }

    @Transactional
    public void delete(Room room) {
        courseRoomTimePreferenceRepository.deleteByRoom(room);
        timetableRepository.deleteByRoom(room);
        roomRepository.delete(room);
    }

    public Room update(Room roomOld, Room roomNew) {
        roomOld.setName(roomNew.getName());
        roomOld.setBuilding(roomNew.getBuilding());
        roomOld.setSeats(roomNew.getSeats());
        return roomRepository.save(roomOld);
    }

}
