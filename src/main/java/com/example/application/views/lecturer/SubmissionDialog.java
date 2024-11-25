/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.lecturer;

/**
 *
 * @author user
 */
import com.example.application.data.Submission;
import com.example.application.services.DocumentProcessingService;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import java.util.Collections;
import java.util.Map;

public class SubmissionDialog extends Dialog {
    private final DocumentProcessingService documentProcessingService;
    private final VerticalLayout content = new VerticalLayout();
    private final ProgressBar similarityBar = new ProgressBar();
    private final Pre textContent = new Pre();

    public SubmissionDialog(Submission submission, DocumentProcessingService documentProcessingService) {
        this.documentProcessingService = documentProcessingService;
        
        setWidth("800px");
        setHeight("600px");
        
        content.setSpacing(true);
        content.setPadding(true);
        
        setupHeader(submission);
        setupContent(submission);
        
        add(content);
    }

    private void setupHeader(Submission submission) {
        H3 title = new H3("Submission Review");
        content.add(title);
        
        // Student info
        content.add("Student: " + submission.getStudent().getUser().getFirstName() + 
                   " " + submission.getStudent().getUser().getLastName());
        content.add("Submitted: " + submission.getSubmissionDate());
        content.add("File: " + submission.getFileName());
    }

    private void setupContent(Submission submission) {
        try {
            String extractedText = documentProcessingService.extractText(
                submission.getFileData(), 
                submission.getFileName()
            );
            
            textContent.setText(extractedText);
            content.add(textContent);
            
            // Add similarity score bar
            similarityBar.setMin(0);
            similarityBar.setMax(100);
            content.add("Similarity Score:");
            content.add(similarityBar);
            
            // Process similarity in background
            new Thread(() -> {
                try {
                    Map<String, Object> analysis = documentProcessingService.analyzeSimilarity(
                        extractedText,
                        Collections.emptyList() // TODO: Add other submissions' texts
                    );
                    
                    getUI().ifPresent(ui -> ui.access(() -> {
                        double score = (double) analysis.get("similarity_score") * 100;
                        similarityBar.setValue(score);
                    }));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (Exception e) {
            content.add("Error processing document: " + e.getMessage());
        }
    }
}
