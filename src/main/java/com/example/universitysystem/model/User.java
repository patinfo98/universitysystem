package com.example.universitysystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name="users")
@Inheritance(strategy = InheritanceType.JOINED)

public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "last_name", length = 20, nullable = false )
    private String lastName;

    @Column(name = "first_name", length = 20, nullable = false )
    private String firstName;

    @Column(name = "email", length = 50, nullable = false )
    private String email;

    @Column(name = "password", length = 20, nullable = false )
    private String password;
}
