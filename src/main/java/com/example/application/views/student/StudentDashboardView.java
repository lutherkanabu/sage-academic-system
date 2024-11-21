/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.student;

/**
 *
 * @author user
 */
import com.example.application.data.Student;
import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "student", layout = MainLayout.class)
@PageTitle("Student Dashboard | SAGE")
@RolesAllowed("ROLE_STUDENT")
public class StudentDashboardView extends VerticalLayout {
    
    public StudentDashboardView(AuthenticatedUser authenticatedUser) {
        setSpacing(true);
        setPadding(true);

        // Get current user
        User user = authenticatedUser.get().orElseThrow();
        
        // Welcome section
        H2 welcome = new H2("Welcome, " + user.getFirstName());
        add(welcome);

        // Create tabs for different sections
        Tabs tabs = new Tabs();
        Tab assignments = new Tab("Assignments");
        Tab grades = new Tab("Grades");
        Tab profile = new Tab("Profile");
        tabs.add(assignments, grades, profile);
        add(tabs);

        // Content area
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setPadding(true);
        
        // Add some sample content
        H3 upcomingTitle = new H3("Upcoming Assignments");
        content.add(upcomingTitle);
        
        add(content);
    }
}
