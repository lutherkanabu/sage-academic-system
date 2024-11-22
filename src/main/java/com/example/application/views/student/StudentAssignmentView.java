/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.student;

/**
 *
 * @author user
 */
import com.example.application.data.Assignment;
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.AssignmentService;
import com.example.application.services.SubmissionService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import java.io.InputStream;
import java.util.List;

@Route(value = "student/assignments", layout = MainLayout.class)
@PageTitle("Assignments | SAGE")
@RolesAllowed("ROLE_STUDENT")
public class StudentAssignmentView extends VerticalLayout {
    
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final AuthenticatedUser authenticatedUser;
    
    public StudentAssignmentView(AssignmentService assignmentService,
                                SubmissionService submissionService,
                                AuthenticatedUser authenticatedUser) {
        this.assignmentService = assignmentService;
        this.submissionService = submissionService;
        this.authenticatedUser = authenticatedUser;
        
        addClassName("student-assignment-view");
        setSpacing(true);
        setPadding(true);
        
        add(new H2("Available Assignments"));
        
        refreshAssignments();
    }
    
    private void refreshAssignments() {
        // Remove old content
        getChildren()
            .filter(component -> component instanceof VerticalLayout)
            .forEach(this::remove);

        // Get available assignments
        List<Assignment> assignments = assignmentService.getAvailableAssignmentsForStudent(
            authenticatedUser.get().orElseThrow()
        );

        // Create assignment cards
        assignments.forEach(assignment -> createAssignmentCard(assignment));  // Changed to lambda
    }
    
    private void createAssignmentCard(Assignment assignment) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("assignment-card");
        card.setSpacing(false);
        card.setPadding(true);
        
        H3 title = new H3(assignment.getFormattedTitle());
        Paragraph description = new Paragraph(assignment.getDescription());
        
        // File upload component
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            
        upload.addSucceededListener(event -> {
            try {
                String fileName = event.getFileName();
                InputStream inputStream = buffer.getInputStream();
                byte[] fileData = inputStream.readAllBytes();
                
                submissionService.submitAssignment(
                    assignment.getId(),
                    fileName,
                    fileData,
                    authenticatedUser.get().orElseThrow()
                );
                
                Notification.show("Assignment submitted successfully!")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    
                refreshAssignments();
            } catch (Exception e) {
                Notification.show("Error submitting assignment: " + e.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        card.add(title, description, upload);
        add(card);
    }
}
