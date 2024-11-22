package com.example.application.views;

import com.example.application.data.User;
import com.example.application.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout {

    private H1 viewTitle;
    private final AuthenticatedUser authenticatedUser;
    private final AccessAnnotationChecker accessChecker;

    public MainLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.DRAWER);
        
        // Only show drawer content if user is authenticated
        if (authenticatedUser.get().isPresent()) {
            addDrawerContent();
        }
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        // Only show toggle button if user is authenticated
        if (authenticatedUser.get().isPresent()) {
            addToNavbar(true, toggle, viewTitle);
        } else {
            // For unauthenticated users, just show the title
            addToNavbar(true, viewTitle);
        }
    }

    private void addDrawerContent() {
        H1 appName = new H1("SAGE");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
    SideNav nav = new SideNav();

    Optional<User> userOptional = authenticatedUser.get();
    if (userOptional.isPresent()) {
        User user = userOptional.get();
        
        // Add common navigation items
        nav.addItem(new SideNavItem("Dashboard", 
                user.getUserType().toString().toLowerCase(),
                new Icon("lumo", "dashboard")));
        
        // Add role-specific navigation items
        switch (user.getUserType()) {
    case STUDENT:
        nav.addItem(new SideNavItem("Assignments", "student/assignments",
            new Icon(VaadinIcon.TASKS)));
        nav.addItem(new SideNavItem("Grades", "student/grades",
            new Icon(VaadinIcon.CHART)));
        break;
        
    case LECTURER:
        nav.addItem(new SideNavItem("Create Assignment", "lecturer/create-assignment",
            new Icon(VaadinIcon.PLUS_CIRCLE)));
        nav.addItem(new SideNavItem("Grade Submissions", "lecturer/grade",
            new Icon(VaadinIcon.CHECK_SQUARE)));
        break;
        
    case ADMIN:
        nav.addItem(new SideNavItem("Users", "admin/users",
            new Icon(VaadinIcon.USERS)));
        nav.addItem(new SideNavItem("Settings", "admin/settings",
            new Icon(VaadinIcon.COG)));
        break;
}
        
        // Add profile navigation item for all users
        nav.addItem(new SideNavItem("Profile", "profile",
            new Icon(VaadinIcon.USER)));  // Changed from .create()
    }

    return nav;
}
    private Footer createFooter() {
        Footer layout = new Footer();

        Optional<User> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            User user = maybeUser.get();

            Avatar avatar = new Avatar(user.getFirstName() + " " + user.getLastName());
            if (user.getProfilePicture() != null) {
                StreamResource resource = new StreamResource("profile-pic",
                        () -> new ByteArrayInputStream(user.getProfilePicture()));
                avatar.setImageResource(resource);
            }
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userName = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(user.getFirstName() + " " + user.getLastName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");
            userName.add(div);
            userName.getSubMenu().addItem("Sign out", e -> {
                authenticatedUser.logout();
            });

            layout.add(userMenu);
        } else {
            // Create a horizontal layout for login and register links
            HorizontalLayout authLinks = new HorizontalLayout();
            authLinks.setSpacing(true);
            authLinks.setJustifyContentMode(JustifyContentMode.CENTER);
            authLinks.setWidthFull();
            
            Anchor loginLink = new Anchor("login", "Sign in");
            Anchor registerLink = new Anchor("register", "Register");
            
            // Style the links
            Stream.of(loginLink, registerLink).forEach(link -> {
                link.getStyle()
                    .set("text-decoration", "none")
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("padding", "var(--lumo-space-s)")
                    .set("font-weight", "500");
            });
            
            authLinks.add(loginLink, registerLink);
            layout.add(authLinks);
        }

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        
        // Only update view title if authenticated
        if (authenticatedUser.get().isPresent()) {
            viewTitle.setText(getCurrentPageTitle());
        }
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}