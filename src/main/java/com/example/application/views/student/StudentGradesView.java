/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.student;

/**
 *
 * @author user
 */
import com.example.application.data.*;
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.GradeService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import org.springframework.transaction.annotation.Transactional;

@Route(value = "student/grades", layout = MainLayout.class)
@PageTitle("My Grades | SAGE")
@RolesAllowed("ROLE_STUDENT")
public class StudentGradesView extends VerticalLayout {
    private static final Logger logger = LoggerFactory.getLogger(StudentGradesView.class);
    
    private final GradeService gradeService;
    private final AuthenticatedUser authenticatedUser;
    private final Grid<Grade> gradeGrid;
    
    public StudentGradesView(GradeService gradeService, AuthenticatedUser authenticatedUser) {
        this.gradeService = gradeService;
        this.authenticatedUser = authenticatedUser;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        add(new H2("My Grades"));
        
        gradeGrid = new Grid<>(Grade.class, false);
        configureGrid();
        add(gradeGrid);
        
        loadGrades();
    }
    
    private void configureGrid() {
        gradeGrid.addColumn(grade -> {
            Submission submission = grade.getSubmission();
            if (submission != null && submission.getAssignment() != null) {
                return submission.getAssignment().getTitle();
            }
            return "N/A";
        }).setHeader("Assignment")
          .setSortable(true);
            
        gradeGrid.addColumn(this::formatGrade)
            .setHeader("Grade")
            .setSortable(true);
            
        gradeGrid.addColumn(Grade::getLecturerFeedback)
            .setHeader("Feedback")
            .setAutoWidth(true);
            
        gradeGrid.addColumn(grade -> {
            Submission submission = grade.getSubmission();
            if (submission != null && submission.getSubmissionDate() != null) {
                return submission.getSubmissionDate()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            return "N/A";
        }).setHeader("Submitted")
          .setSortable(true);
            
        gradeGrid.addColumn(grade -> 
            grade.getPostedAt() != null ? 
                grade.getPostedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : 
                "Not posted yet")
            .setHeader("Graded On")
            .setSortable(true);
            
        gradeGrid.setAllRowsVisible(true);
    }
    
    private String formatGrade(Grade grade) {
        if (grade.isPosted() && grade.getManualGrade() != null) {
            return String.format("%d/100", grade.getManualGrade());
        } else if (grade.getTotalScore() > 0) {
            return String.format("%d/100 (AI Generated)", grade.getTotalScore());
        } else {
            return "Pending";
        }
    }
    
    private void loadGrades() {
        try {
            User currentUser = authenticatedUser.get().orElseThrow();
            gradeGrid.setItems(gradeService.getStudentGrades(currentUser));
            logger.info("Successfully loaded grades for student: {}", currentUser.getUsername());
        } catch (Exception e) {
            logger.error("Error loading grades: ", e);
            Notification.show("Error loading grades: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
