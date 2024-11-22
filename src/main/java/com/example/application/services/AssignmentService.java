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
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository,
                           LecturerRepository lecturerRepository,
                           StudentRepository studentRepository) {
        this.assignmentRepository = assignmentRepository;
        this.lecturerRepository = lecturerRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public Assignment createAssignment(String title, String description, User lecturer) {
        Lecturer lecturerDetails = lecturerRepository.findByUser(lecturer);
        
        Assignment assignment = new Assignment();
        assignment.setTitle(title);
        assignment.setDescription(description);
        assignment.setLecturer(lecturerDetails);
        
        return assignmentRepository.save(assignment);  // No casting needed now
    }
    
    @Transactional(readOnly = true) 
    public List<Assignment> getAvailableAssignmentsForStudent(User student) {
        Student studentDetails = studentRepository.findByUser(student);
        return assignmentRepository.findAvailableAssignmentsForStudent(studentDetails);
    }

    public List<Assignment> getLecturerAssignments(User lecturer) {
        Lecturer lecturerDetails = lecturerRepository.findByUser(lecturer);
        return assignmentRepository.findByLecturerOrderByCreatedAtDesc(lecturerDetails);
    }
}
