/*
 * Student Database Object
 * contains information about students
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
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
public class Student extends User {


    @Column(name = "student_number", length = 8)
    private int studentNumber;

}
