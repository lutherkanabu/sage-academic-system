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
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
        processSubmission(submission);
        
        add(content);
    }

    private void setupHeader(Submission submission) {
        H3 title = new H3("Submission Review");
        content.add(title);
        
        content.add(String.format("Student: %s %s", 
            submission.getStudent().getUser().getFirstName(),
            submission.getStudent().getUser().getLastName()));
        content.add("Submitted: " + submission.getSubmissionDate());
        content.add("File: " + submission.getFileName());
    }

    private void processSubmission(Submission submission) {
        try {
            // Validate file data
            if (submission.getFileData() == null || submission.getFileData().length == 0) {
                throw new IllegalArgumentException("The submission file appears to be empty or corrupted");
            }

            // Extract text
            String extractedText = documentProcessingService.extractText(
                submission.getFileData(), 
                submission.getFileName()
            );
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                H4 contentHeader = new H4("Document Content");
                content.add(contentHeader);
                
                textContent.setText(extractedText);
                textContent.getStyle()
                    .set("max-height", "400px")
                    .set("overflow", "auto")
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("padding", "var(--lumo-space-m)");
                content.add(textContent);
                
                // Setup similarity analysis
                setupSimilarityAnalysis(extractedText);
            } else {
                content.add("No text content could be extracted from the document.");
            }
            
        } catch (Exception e) {
            String errorMessage = "Error processing document: " + e.getMessage();
            content.add(errorMessage);
            
            Notification.show(errorMessage)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void setupSimilarityAnalysis(String extractedText) {
        similarityBar.setMin(0);
        similarityBar.setMax(100);
        similarityBar.setWidth("100%");
        
        H4 similarityHeader = new H4("Similarity Analysis");
        content.add(similarityHeader);
        content.add("Processing similarity score...");
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
                    content.add("Similarity Score: " + analysis.get("similarity_percentage"));
                }));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    content.add("Error calculating similarity: " + e.getMessage());
                }));
            }
        }).start();
    }
}