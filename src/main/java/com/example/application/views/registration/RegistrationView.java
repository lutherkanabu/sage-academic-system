/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.registration;

/**
 *
 * @author user
 */
import com.example.application.data.UserType;
import com.example.application.services.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "register", layout = MainLayout.class)
@PageTitle("Register | SAGE")
@AnonymousAllowed
public class RegistrationView extends VerticalLayout {
    private final UserService userService;

    private final RadioButtonGroup<UserType> userTypeSelect;
    private final TextField username;
    private final TextField firstName;
    private final TextField lastName;
    private final EmailField email;
    private final PasswordField password;
    private final PasswordField confirmPassword;
    private final TextField studentNumber;
    private final TextField staffNumber;
    private final TextField department;
    private final FormLayout formLayout;

    public RegistrationView(UserService userService) {
        this.userService = userService;

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H2 title = new H2("Register");
        
        userTypeSelect = new RadioButtonGroup<>();
        userTypeSelect.setLabel("Register as");
        userTypeSelect.setItems(UserType.STUDENT, UserType.LECTURER);
        userTypeSelect.setValue(UserType.STUDENT);

        username = new TextField("Username");
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        email = new EmailField("Email");
        password = new PasswordField("Password");
        confirmPassword = new PasswordField("Confirm Password");
        studentNumber = new TextField("Student Number");
        staffNumber = new TextField("Staff Number");
        department = new TextField("Department");

        formLayout = new FormLayout();
        formLayout.setMaxWidth("500px");
        formLayout.add(
            userTypeSelect,
            username,
            firstName,
            lastName,
            email,
            password,
            confirmPassword
        );

        // Show/hide fields based on user type
        userTypeSelect.addValueChangeListener(event -> updateFormFields());
        updateFormFields(); // Initial setup

        Button registerButton = new Button("Register", e -> register());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(title, formLayout, registerButton);
        
        // Set some spacing
        setSpacing(true);
        setPadding(true);
        setMaxWidth("600px");
    }

    private void updateFormFields() {
        formLayout.remove(studentNumber, staffNumber, department);
        if (userTypeSelect.getValue() == UserType.STUDENT) {
            formLayout.add(studentNumber);
        } else {
            formLayout.add(staffNumber, department);
        }
    }

    private void register() {
        if (!password.getValue().equals(confirmPassword.getValue())) {
            Notification.show("Passwords don't match");
            return;
        }

        try {
            userService.registerUser(
                username.getValue(),
                email.getValue(),
                firstName.getValue(),
                lastName.getValue(),
                password.getValue(),
                userTypeSelect.getValue(),
                studentNumber.getValue(),
                staffNumber.getValue(),
                department.getValue()
            );

            Notification.show("Registration successful! Please log in.");
            getUI().ifPresent(ui -> ui.navigate("login"));
        } catch (Exception e) {
            Notification.show("Registration failed: " + e.getMessage());
        }
    }
}