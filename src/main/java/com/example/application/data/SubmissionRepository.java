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
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    @Query("SELECT DISTINCT s FROM Submission s " +
           "JOIN FETCH s.student st " +
           "JOIN FETCH st.user " +
           "JOIN FETCH s.assignment " +
           "WHERE s.assignment = :assignment")
    List<Submission> findByAssignment(@Param("assignment") Assignment assignment);
    
    @Query("SELECT s FROM Submission s " +
           "JOIN FETCH s.student st " +
           "JOIN FETCH st.user " +
           "JOIN FETCH s.assignment " +
           "WHERE s.id = :id")
    Optional<Submission> findByIdWithDetails(@Param("id") Long id);
    
    Optional<Submission> findByAssignmentAndStudent(Assignment assignment, Student student);
    boolean existsByAssignmentAndStudent(Assignment assignment, Student student);
}