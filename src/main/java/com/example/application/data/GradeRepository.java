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

public interface GradeRepository extends JpaRepository<Grade, Long> {
    @Query("SELECT DISTINCT g FROM Grade g " +
           "LEFT JOIN FETCH g.submission s " +
           "LEFT JOIN FETCH s.assignment a " +
           "LEFT JOIN FETCH s.student st " +
           "LEFT JOIN FETCH st.user " +
           "WHERE s.student.user = :user")
    List<Grade> findBySubmission_Student_User(@Param("user") User user);

    @Query("SELECT g FROM Grade g " +
           "LEFT JOIN FETCH g.submission s " +
           "LEFT JOIN FETCH s.assignment " +
           "WHERE g.submission = :submission")
    Grade findBySubmission(@Param("submission") Submission submission);
}