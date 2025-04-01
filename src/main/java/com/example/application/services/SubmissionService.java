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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SubmissionService {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);
    
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final DocumentProcessingService documentProcessingService; 

    public SubmissionService(SubmissionRepository submissionRepository,
                           StudentRepository studentRepository,
                           AssignmentRepository assignmentRepository,
                           DocumentProcessingService documentProcessingService) {
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.documentProcessingService = documentProcessingService;
    }

    @Transactional
    public Submission submitAssignment(Long assignmentId, String fileName, 
                                     byte[] fileData, User student) {
        if (fileData == null || fileData.length == 0) {
            logger.error("Attempted to submit empty file data");
            throw new IllegalArgumentException("File data cannot be empty");
        }
        
        logger.info("Processing submission for assignment {} by student {}", 
                   assignmentId, student.getUsername());

        Student studentDetails = studentRepository.findByUser(student);
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (submissionRepository.existsByAssignmentAndStudent(assignment, studentDetails)) {
            throw new RuntimeException("Assignment already submitted");
        }

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(studentDetails);
        submission.setFileName(fileName);
        submission.setFileData(fileData);

        try {
            // Extract and store text during submission
            String extractedText = documentProcessingService.extractText(fileData, fileName);
            submission.setExtractedText(extractedText);
        } catch (Exception e) {
            logger.error("Failed to extract text from submission: {}", e.getMessage());
            throw new RuntimeException("Failed to process submission: " + e.getMessage());
        }

        logger.info("Saving submission to database...");
        submission = submissionRepository.save(submission);
        logger.info("Submission saved successfully with ID: {}", submission.getId());

        return submission;
    }

    @Transactional(readOnly = true)
    public List<Submission> getAssignmentSubmissions(Assignment assignment) {
        logger.info("Fetching submissions for assignment ID: {}", assignment.getId());
        return submissionRepository.findByAssignment(assignment);
    }
    
    @Transactional(readOnly = true)
    public Submission getSubmissionById(Long submissionId) {
        logger.info("Fetching submission by ID: {}", submissionId);
        return submissionRepository.findByIdWithDetails(submissionId)
            .orElseThrow(() -> new RuntimeException("Submission not found"));
    }
}