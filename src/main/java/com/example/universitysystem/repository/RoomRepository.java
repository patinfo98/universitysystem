package com.example.universitysystem.repository;

import com.example.universitysystem.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findAll();

    Room findById(int id);

}
