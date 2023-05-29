/*
 * Teacher Database Object
 * contains information about the staff
 * Author:      Patrick Foessl
 * Last Change: 29.05.2023
 */

package com.example.universitysystem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "teachers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Staff extends User {
    @Column(name = "role")
    private role role;

    @Column(name = "firstLogin", length = 20, nullable = false)
    private Boolean firstLogin;

    public enum role {admin, assistant}
}
