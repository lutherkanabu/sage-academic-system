package com.example.application.views.home;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Home")
@Route("")
@Menu(order = 0, icon = "line-awesome/svg/pencil-ruler-solid.svg")
@AnonymousAllowed
public class HomeView extends Composite<VerticalLayout> {

    public HomeView() {
        LoginForm loginForm = new LoginForm();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        loginForm.setWidth("100%");
        loginForm.getStyle().set("flex-grow", "1");
        getContent().add(loginForm);
    }
}
