package com.example.universitysystem.repository;

import com.example.universitysystem.model.Course;
import com.example.universitysystem.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findAll();

    Room findById(int id);


}
