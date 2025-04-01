/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.lecturer;

/**
 *
 * @author user
 */
import com.example.application.data.Grade;
import com.example.application.data.GradeRepository;
import com.example.application.services.GradeService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.time.LocalDateTime;

public class GradePostDialog extends Dialog {
    private final Grade grade;
    private final GradeService gradeService;
    private final GradeRepository gradeRepository;
    
    public GradePostDialog(Grade grade, GradeService gradeService, GradeRepository gradeRepository) {
        this.grade = grade;
        this.gradeService = gradeService;
        this.gradeRepository = gradeRepository;
        
        setWidth("400px");
        
        IntegerField gradeField = new IntegerField("Grade");
        gradeField.setMin(0);
        gradeField.setMax(100);
        gradeField.setValue(grade.getManualGrade() != null ? grade.getManualGrade() : grade.getTotalScore());
        
        TextArea feedbackField = new TextArea("Feedback");
        feedbackField.setWidthFull();
        feedbackField.setValue(grade.getLecturerFeedback() != null ? grade.getLecturerFeedback() : "");
        
        Button postButton = new Button("Post Grade", e -> {
            try {
                // First ensure the grade is saved and has an ID
                if (grade.getId() == null) {
                    grade.setGradedAt(LocalDateTime.now());
                    gradeRepository.save(grade);
                }
                
                // Now post the grade
                gradeService.postGrade(
                    grade.getId(),
                    gradeField.getValue(),
                    feedbackField.getValue()
                );
                
                Notification.show("Grade posted successfully")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                close();
            } catch (Exception ex) {
                Notification.show("Failed to post grade: " + ex.getMessage())
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        
        postButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        VerticalLayout layout = new VerticalLayout(gradeField, feedbackField, postButton);
        layout.setPadding(true);
        layout.setSpacing(true);
        
        add(layout);
    }
}