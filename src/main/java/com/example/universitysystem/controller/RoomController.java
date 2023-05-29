/*
 * Room Controller
 * methods in connection with the display of room information
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.controller;

import com.example.universitysystem.model.Room;
import com.example.universitysystem.repository.RoomRepository;
import com.example.universitysystem.service.RoomService;
import com.example.universitysystem.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class RoomController {
    private final UserService userService;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    public RoomController(UserService userService, RoomRepository roomRepository, RoomService roomService) {
        this.userService = userService;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
    }

    @GetMapping("/rooms")
    public String showRooms(Model model, Room room) {
        if (userService.accessAllowed(UserService.userType.admin)) {
            List<Room> rooms = roomRepository.findAll();
            Collections.sort(rooms, Comparator.comparing(Room::getName));
            model.addAttribute("rooms", rooms);
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
        if (userService.accessAllowed(UserService.userType.admin)) {
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
    public String deleteRoom(@PathVariable("id") int id) {
        roomService.delete(roomRepository.findById(id));
        return "redirect:/rooms";
    }
}
