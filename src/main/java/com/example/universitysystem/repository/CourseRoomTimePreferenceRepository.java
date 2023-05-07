package com.example.universitysystem.repository;

import com.example.universitysystem.model.CourseRoomTimePreference;
import com.example.universitysystem.model.Room;
import com.example.universitysystem.model.days;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRoomTimePreferenceRepository extends JpaRepository<CourseRoomTimePreference, Integer> {

    List<CourseRoomTimePreference> findByDay(days day);

    List<CourseRoomTimePreference> findByCourse_Staff_Id(int id);

    void deleteByRoom(Room room);

    void deleteByCourseId(int id);
}
