/*
 * Room Database Object
 * contains information about a room
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "seats")
    private int seats;
    @Column(name = "building")
    private String building;
}
