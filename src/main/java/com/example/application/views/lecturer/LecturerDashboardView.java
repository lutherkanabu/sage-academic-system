/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.lecturer;

/**
 *
 * @author user
 */
import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "lecturer", layout = MainLayout.class)
@PageTitle("Lecturer Dashboard | SAGE")
@RolesAllowed("ROLE_LECTURER")
public class LecturerDashboardView extends VerticalLayout {
    
    public LecturerDashboardView(AuthenticatedUser authenticatedUser) {
        setSpacing(true);
        setPadding(true);

        // Get current user
        User user = authenticatedUser.get().orElseThrow();
        
        // Welcome section
        H2 welcome = new H2("Welcome, " + user.getFirstName());
        add(welcome);

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        Button createAssignment = new Button("Create Assignment");
        Button viewSubmissions = new Button("View Submissions");
        actions.add(createAssignment, viewSubmissions);
        add(actions);

        // Create tabs for different sections
        Tabs tabs = new Tabs();
        Tab assignments = new Tab("Assignments");
        Tab students = new Tab("Students");
        Tab grading = new Tab("Grading");
        Tab profile = new Tab("Profile");
        tabs.add(assignments, students, grading, profile);
        add(tabs);

        // Content area
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        
        // Add some sample content
        H3 pendingTitle = new H3("Pending Submissions");
        content.add(pendingTitle);
        
        add(content);
    }
}
