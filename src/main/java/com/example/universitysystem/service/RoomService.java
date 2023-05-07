package com.example.universitysystem.service;

import com.example.universitysystem.model.Room;
import com.example.universitysystem.repository.CourseRoomTimePreferenceRepository;
import com.example.universitysystem.repository.RoomRepository;
import com.example.universitysystem.repository.TimetableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Room add(Room room) {
        return roomRepository.save(room);
    }

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

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public Room findById(int id) {
        return roomRepository.findById(id);
    }

}
