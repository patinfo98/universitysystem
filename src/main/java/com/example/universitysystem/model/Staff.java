package com.example.universitysystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teachers")
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="user_id")
public class Staff extends User {
    public enum role {admin, assistant};
    @Column(name = "role")
    private role role;
}
