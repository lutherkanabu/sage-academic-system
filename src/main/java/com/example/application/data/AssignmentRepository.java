/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.example.application.data;

/**
 *
 * @author user
 */
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.repository.query.Param;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query("SELECT DISTINCT a FROM Assignment a LEFT JOIN FETCH a.lecturer WHERE a.id NOT IN " +
           "(SELECT s.assignment.id FROM Submission s WHERE s.student = :student)")
    List<Assignment> findAvailableAssignmentsForStudent(@Param("student") Student student);

    List<Assignment> findByLecturerOrderByCreatedAtDesc(Lecturer lecturer);
}
