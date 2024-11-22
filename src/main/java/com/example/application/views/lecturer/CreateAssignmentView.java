/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.lecturer;

/**
 *
 * @author user
 */
import com.example.application.security.AuthenticatedUser;
import com.example.application.services.AssignmentService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "lecturer/create-assignment", layout = MainLayout.class)
@PageTitle("Create Assignment | SAGE")
@RolesAllowed("ROLE_LECTURER") 
public class CreateAssignmentView extends VerticalLayout {
    
    private final AssignmentService assignmentService;
    private final AuthenticatedUser authenticatedUser;
    
    private final TextField titleField;
    private final TextArea descriptionField;
    
    public CreateAssignmentView(AssignmentService assignmentService,
                               AuthenticatedUser authenticatedUser) {
        this.assignmentService = assignmentService;
        this.authenticatedUser = authenticatedUser;
        
        addClassName("create-assignment-view");
        setSpacing(true);
        setPadding(true);
        setMaxWidth("800px");
        setAlignItems(Alignment.STRETCH);
        
        add(new H2("Create New Assignment"));
        
        titleField = new TextField("Assignment Title");
        titleField.setRequired(true);
        titleField.setWidthFull();
        titleField.setHelperText("Enter a clear, descriptive title for your assignment");
        
        descriptionField = new TextArea("Description");
        descriptionField.setRequired(true);
        descriptionField.setWidthFull();
        descriptionField.setMinHeight("200px");
        descriptionField.setHelperText("Provide detailed instructions and requirements for the assignment");
        
        Button createButton = new Button("Create Assignment", e -> createAssignment());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setWidthFull();
        
        // Information text about file submissions
        Paragraph fileInfo = new Paragraph("Students will be able to submit PDF and Word documents for this assignment.");
        fileInfo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        add(
            titleField,
            descriptionField,
            fileInfo,
            createButton
        );
        
        // Center the form on the page
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }
    
    private void createAssignment() {
        try {
            if (titleField.getValue().trim().isEmpty()) {
                Notification.show("Please enter an assignment title")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            if (descriptionField.getValue().trim().isEmpty()) {
                Notification.show("Please enter an assignment description")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            
            assignmentService.createAssignment(
                titleField.getValue().trim(),
                descriptionField.getValue().trim(),
                authenticatedUser.get().orElseThrow()
            );
            
            Notification notification = new Notification(
                "Assignment created successfully!", 
                3000, 
                Notification.Position.TOP_CENTER
            );
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();
            
            // Clear the form
            titleField.clear();
            descriptionField.clear();
            
        } catch (Exception e) {
            Notification.show("Error creating assignment: " + e.getMessage())
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
