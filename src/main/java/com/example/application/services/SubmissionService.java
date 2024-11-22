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

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final StudentRepository studentRepository;
    private final AssignmentRepository assignmentRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
                           StudentRepository studentRepository,
                           AssignmentRepository assignmentRepository) {
        this.submissionRepository = submissionRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional
    public Submission submitAssignment(Long assignmentId, String fileName, 
                                     byte[] fileData, User student) {
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

        return submissionRepository.save(submission);  // No casting needed now
    }

    public List<Submission> getAssignmentSubmissions(Assignment assignment) {
        return submissionRepository.findByAssignment(assignment);
    }
}