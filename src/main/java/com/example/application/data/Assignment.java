/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.data;

/**
 *
 * @author user
 */
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Entity
public class Assignment extends AbstractEntity {
    
    @NotEmpty
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Getters and setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Lecturer getLecturer() {
        return lecturer;
    }
    
    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper method to get formatted title with department
    public String getFormattedTitle() {
        return lecturer.getDepartment() + ": " + title;
    }
}
