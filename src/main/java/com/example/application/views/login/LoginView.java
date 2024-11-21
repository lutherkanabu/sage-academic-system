package com.example.application.views.login;

import com.example.application.security.AuthenticatedUser;
import com.example.application.views.registration.RegistrationView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();
    private final AuthenticatedUser authenticatedUser;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setAction("login");

        // Add title
        H2 title = new H2("SAGE Academic System");
        title.getStyle().set("margin", "0 0 var(--lumo-space-l) 0");
        
        // Create registration link
        Div registrationDiv = new Div();
        registrationDiv.setText("Don't have an account? ");
        RouterLink registerLink = new RouterLink("Register here", RegistrationView.class);
        registrationDiv.add(registerLink);
        registrationDiv.getStyle().set("margin-top", "var(--lumo-space-m)");

        add(
            title,
            login,
            registrationDiv
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            event.forwardTo("");
        }

        login.setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}