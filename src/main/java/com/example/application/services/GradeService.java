/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

/**
 *
 * @author user
 */
import com.example.application.data.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GradeService {
    private static final Logger logger = LoggerFactory.getLogger(GradeService.class);
    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    @Transactional
    public Grade postGrade(Long gradeId, int manualGrade, String feedback) {
        logger.info("Posting grade with ID: {}, score: {}", gradeId, manualGrade);
        
        Grade grade = gradeRepository.findById(gradeId)
            .orElseThrow(() -> new RuntimeException("Grade not found with ID: " + gradeId));
            
        grade.setManualGrade(manualGrade);
        grade.setLecturerFeedback(feedback);
        grade.setPosted(true);
        grade.setPostedAt(LocalDateTime.now());
        
        Grade savedGrade = gradeRepository.save(grade);
        logger.info("Successfully posted grade with ID: {}", savedGrade.getId());
        
        return savedGrade;
    }
    
    @Transactional(readOnly = true)
    public List<Grade> getStudentGrades(User student) {
        logger.info("Fetching grades for student: {}", student.getUsername());
        return gradeRepository.findBySubmission_Student_User(student);
    }
}