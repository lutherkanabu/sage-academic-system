package com.example.application.views.home;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("SAGE Academic System")
@Route("")
public class HomeView extends VerticalLayout {

    public HomeView() {
        setSpacing(true);
        setPadding(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        // Add title
        H1 title = new H1("Welcome to SAGE");
        title.getStyle().set("margin-top", "2em");
        
        // Add description
        H2 subtitle = new H2("Academic Grading System");
        Paragraph description = new Paragraph(
            "An intelligent grading system for modern education."
        );

        // Create action buttons
        Button loginButton = new Button("Sign In", e -> getUI().ifPresent(ui -> ui.navigate("login")));
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button registerButton = new Button("Register", e -> getUI().ifPresent(ui -> ui.navigate("register")));
        registerButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        
        // Add components to layout
        add(
            title,
            subtitle,
            description,
            new HorizontalLayout(loginButton, registerButton)
        );
    }
}