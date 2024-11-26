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
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.util.Map;
import java.util.List;

public class SubmissionDialog extends Dialog {
    private final DocumentProcessingService documentProcessingService;
    private final VerticalLayout content = new VerticalLayout();
    private final VerticalLayout similarityContent = new VerticalLayout();
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

            String extractedText = documentProcessingService.extractText(
                submission.getFileData(), 
                submission.getFileName()
            );
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                // Document content section
                H4 contentHeader = new H4("Document Content");
                textContent.setText(extractedText);
                textContent.getStyle()
                    .set("max-height", "300px")
                    .set("overflow", "auto")
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("padding", "var(--lumo-space-m)");
                content.add(contentHeader, textContent);
                
                // Similarity analysis section
                setupSimilarityAnalysis(extractedText, submission);
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

    private void setupSimilarityAnalysis(String extractedText, Submission submission) {
        H4 similarityHeader = new H4("Similarity Analysis");
        similarityContent.addClassName("similarity-content");
        content.add(similarityHeader, similarityContent);
        similarityContent.add("Processing similarity analysis...");

        // Process similarity in background
        new Thread(() -> {
            try {
                Map<String, Object> analysis = documentProcessingService.analyzeSimilarity(
                    extractedText,
                    submission.getAssignment()
                );
                
                getUI().ifPresent(ui -> ui.access(() -> {
                    updateSimilarityDisplay(analysis);
                }));
            } catch (Exception e) {
                getUI().ifPresent(ui -> ui.access(() -> {
                    Notification.show("Error analyzing similarity: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }));
            }
        }).start();
    }

    private void updateSimilarityDisplay(Map<String, Object> analysis) {
        similarityContent.removeAll();
        
        double similarityScore = (double) analysis.get("similarity_score");
        String similarityPercentage = (String) analysis.get("similarity_percentage");
        boolean isPotentialPlagiarism = (boolean) analysis.get("is_potential_plagiarism");
        
        // Create similarity indicator
        Div indicator = new Div();
        indicator.setText("Similarity Score: " + similarityPercentage);
        indicator.getStyle()
            .set("padding", "var(--lumo-space-s)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("font-weight", "bold");
        
        // Color code based on similarity level
        if (similarityScore > 0.8) {
            indicator.getStyle()
                .set("background-color", "var(--lumo-error-color)")
                .set("color", "white");
        } else if (similarityScore > 0.5) {
            indicator.getStyle()
                .set("background-color", "var(--lumo-warning-color)")
                .set("color", "white");
        } else {
            indicator.getStyle()
                .set("background-color", "var(--lumo-success-color)")
                .set("color", "white");
        }
        
        similarityContent.add(indicator);
        
        // Add matches if any
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> matches = (List<Map<String, Object>>) analysis.get("matches");
        if (matches != null && !matches.isEmpty()) {
            H5 matchesHeader = new H5("Similar Submissions");
            similarityContent.add(matchesHeader);
            
            matches.forEach(match -> {
                String studentName = (String) match.get("studentName");
                double matchScore = (double) match.get("similarityScore");
                
                Div matchDiv = new Div();
                matchDiv.setText(String.format("%s - %.2f%% similarity",
                    studentName, matchScore * 100));
                similarityContent.add(matchDiv);
            });
        }
    }
}