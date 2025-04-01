/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.lecturer;

/**
 *
 * @author user
 */
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.router.*;
import com.vaadin.flow.component.button.Button;
import jakarta.annotation.security.RolesAllowed;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route(value = "lecturer/grade-response", layout = MainLayout.class)
@PageTitle("Grading Response")
@RolesAllowed("ROLE_LECTURER")
public class GradeResponseView extends VerticalLayout implements HasUrlParameter<String> {
    
    private static final Logger logger = LoggerFactory.getLogger(GradeResponseView.class);

    @Override
    public void setParameter(BeforeEvent event, @WildcardParameter String response) {
        removeAll();
        
        try {
            String decodedResponse = new String(
                Base64.getUrlDecoder().decode(response),
                StandardCharsets.UTF_8
            );

            H2 title = new H2("AI Grading Response");
            
            // Format response
            Pre responseText = new Pre(decodedResponse);
            responseText.getStyle()
                .set("white-space", "pre-wrap")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "1rem")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("width", "100%")
                .set("max-width", "800px")
                .set("margin", "1rem 0");
                
            Button backButton = new Button("Back to Submission", e -> 
                getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
                
            add(title, responseText, backButton);
            
            setAlignItems(Alignment.CENTER);
            setJustifyContentMode(JustifyContentMode.CENTER);
            
        } catch (Exception e) {
            logger.error("Failed to decode response: ", e);
            add(new H2("Error displaying response"), 
                new Paragraph("Failed to decode response data"));
        }
    }
}