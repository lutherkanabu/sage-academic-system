/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.views.lecturer;

/**
 *
 * @author user
 */

import com.example.application.data.*;
import com.example.application.services.*;
import com.example.application.events.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.shared.Registration;
import org.springframework.context.event.EventListener;
import org.json.JSONObject;
import java.util.Map;
import java.util.List;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.VaadinSession;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.atmosphere.annotation.AnnotationUtil.logger;

public class SubmissionDialog extends Dialog {
    private final DocumentProcessingService documentProcessingService;
    private final AIGradingService aiGradingService;
    private final GradeService gradeService;
    private final GradeRepository gradeRepository;
    private final VerticalLayout content = new VerticalLayout();
    private final VerticalLayout similarityContent = new VerticalLayout();
    private final VerticalLayout gradingContent = new VerticalLayout();
    private final Pre textContent = new Pre();
    private String extractedText;
    private Submission currentSubmission;
    private ProgressBar progressBar;
    private Span statusLabel;
    private Button rubricGradeButton;
    private volatile String storedRubric = null; 


    public SubmissionDialog(Submission submission, 
                      DocumentProcessingService documentProcessingService,
                      AIGradingService aiGradingService,
                      GradeService gradeService,  GradeRepository gradeRepository) {
            this.documentProcessingService = documentProcessingService;
            this.aiGradingService = aiGradingService;
            this.gradeService = gradeService;
            this.gradeRepository = gradeRepository;
            this.currentSubmission = submission;
        
        setupDialog();
        setupHeader(submission);
        processSubmission(submission);
        
    }

    private void setupDialog() {
        setWidth("800px");
        setHeight("600px");
        content.setSpacing(true);
        content.setPadding(true);
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
            if (submission.getFileData() == null || submission.getFileData().length == 0) {
                throw new IllegalArgumentException("Empty submission file");
            }

            extractedText = documentProcessingService.extractText(
                submission.getFileData(), 
                submission.getFileName()
            );
            
            if (extractedText != null && !extractedText.trim().isEmpty()) {
                setupDocumentContent();
                setupGradingButtons();
                setupContentAreas();
            } else {
                content.add("No text content could be extracted.");
            }
        } catch (Exception e) {
            handleError("Error processing document: " + e.getMessage());
        }
    }
    
    private void setupDocumentContent() {
        H4 contentHeader = new H4("Document Content");
        textContent.setText(extractedText);
        textContent.getStyle()
            .set("max-height", "300px")
            .set("overflow", "auto")
            .set("background", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)");
        content.add(contentHeader, textContent);
    }

    private void setupGradingButtons() {
        HorizontalLayout buttons = new HorizontalLayout();

        // Update Enter Grade button creation
        Button enterGradeButton = new Button("Enter Grade", e -> {
            Grade grade = new Grade();
            grade.setSubmission(currentSubmission);
            GradePostDialog dialog = new GradePostDialog(grade, gradeService, gradeRepository);
            dialog.open();
        });
        enterGradeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button generateRubricButton = new Button("Generate Rubric", e -> generateRubric());
        Button directGradeButton = new Button("Quick Grade", e -> performDirectGrading());
        Button similarityButton = new Button("Check Similarity", 
            e -> setupSimilarityAnalysis(extractedText, currentSubmission));

        rubricGradeButton = new Button("Grade with Rubric", e -> gradeWithRubric());
        rubricGradeButton.setEnabled(false);

        buttons.add(enterGradeButton, generateRubricButton, rubricGradeButton, directGradeButton, similarityButton);
        content.add(buttons);
}

    private void setupContentAreas() {
        content.add(gradingContent, similarityContent);
    }

    private void generateRubric() {
    setupProgress("Generating rubric...");
    
    aiGradingService.generateRubricAsync(
        currentSubmission.getAssignment().getDescription(),
        UI.getCurrent()
    ).thenAccept(rubric -> {
        getUI().ifPresent(ui -> ui.access(() -> {
            try {
                String encodedRubric = Base64.getUrlEncoder()
                    .encodeToString(rubric.getBytes(StandardCharsets.UTF_8));
                // Store the rubric first
                storedRubric = rubric;
                // Enable the button
                rubricGradeButton.setEnabled(true);
                // Navigate to show the rubric
                ui.navigate("lecturer/grade-response/" + encodedRubric);
            } catch (Exception e) {
                handleError("Failed to display rubric: " + e.getMessage());
            }
        }));
    }).exceptionally(error -> {
        handleError("Rubric generation failed: " + error.getMessage());
        return null;
    });
}
    // To check if we have a stored rubric
    private boolean hasStoredRubric() {
        return storedRubric != null && !storedRubric.trim().isEmpty();
}

    private void gradeWithRubric() {
    if (!hasStoredRubric()) {
        handleError("Please generate a rubric first");
        return;
    }
    
    setupProgress("Grading with rubric...");
    aiGradingService.gradeWithRubricAsync(
        currentSubmission,
        storedRubric,
        UI.getCurrent()
    ).thenAccept(grade -> {
        getUI().ifPresent(ui -> ui.access(() -> {
            try {
                String encodedResult = Base64.getUrlEncoder()
                    .encodeToString(grade.getGradingResults().getBytes(StandardCharsets.UTF_8));
                ui.navigate("lecturer/grade-response/" + encodedResult);
            } catch (Exception e) {
                handleError("Failed to display grading results: " + e.getMessage());
            }
        }));
    }).exceptionally(error -> {
        handleError("Rubric grading failed: " + error.getMessage());
        return null;
    });
}

    private void performDirectGrading() {
    setupProgress("Quick grading...");
    aiGradingService.directGradeAsync(
        currentSubmission,
        UI.getCurrent()
    ).thenAccept(grade -> {
        getUI().ifPresent(ui -> ui.access(() -> {
            try {
                String encodedResult = Base64.getUrlEncoder()
                    .encodeToString(grade.getGradingResults().getBytes(StandardCharsets.UTF_8));
                ui.navigate("lecturer/grade-response/" + encodedResult);
            } catch (Exception e) {
                handleError("Failed to display grading results: " + e.getMessage());
            }
        }));
    }).exceptionally(error -> {
        handleError("Quick grading failed: " + error.getMessage());
        return null;
    });
}

    private void setupProgress(String message) {
        gradingContent.removeAll();
        progressBar = new ProgressBar(0, 1);
        statusLabel = new Span(message);
        
        VerticalLayout progressLayout = new VerticalLayout(
            new H4("Progress"),
            progressBar,
            statusLabel
        );
        gradingContent.add(progressLayout);
    }

    private void displayRubric(String rubric) {
    logger.info("Displaying rubric: {}", rubric); 
    getUI().ifPresent(ui -> ui.access(() -> {
        gradingContent.removeAll();
        H4 rubricHeader = new H4("Generated Rubric");
        
        Pre rubricContent = new Pre(rubric);
        rubricContent.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("max-height", "300px")
            .set("overflow", "auto");
            
        gradingContent.add(rubricHeader, rubricContent);
        
        // Enable the Grade with Rubric button
        rubricGradeButton.setEnabled(true);
        storedRubric = rubric;
    }));
}

    private void displayGradingResults(Grade grade) {
        displayResults(grade, "Rubric-Based Grading Results");
    }

    private void displayDirectGradingResults(Grade grade) {
        displayResults(grade, "Quick Grading Results");
    }

    private void displayResults(Grade grade, String headerText) {
    gradingContent.removeAll();
    
    Button manualGradeButton = new Button("Enter Grade", e -> {
        GradePostDialog dialog = new GradePostDialog(grade, gradeService, gradeRepository);
        dialog.open();
    });
    manualGradeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    
    // Add the manual grade button at the top
    gradingContent.add(manualGradeButton);

    // Then show AI results if they exist
    if (grade.getGradingResults() != null) {
        try {
            JSONObject results = new JSONObject(grade.getGradingResults());
            
            Div resultsDiv = new Div();
            resultsDiv.getStyle()
                .set("padding", "1rem")
                .set("margin-top", "1rem")
                .set("background", "var(--lumo-contrast-5pct)");
                
            H3 scoreHeader = new H3("AI Score: " + grade.getTotalScore() + "/100");
            resultsDiv.add(scoreHeader);
            
            if (results.has("feedback")) {
                Paragraph feedback = new Paragraph(results.getString("feedback"));
                feedback.getStyle().set("margin-top", "1rem");
                resultsDiv.add(feedback);
            }
            
            gradingContent.add(resultsDiv);
            
        } catch (Exception e) {
            gradingContent.add(new Span("Error displaying AI results"));
        }
    }
}

    private void stylePreContent(Pre content) {
        content.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("max-height", "400px") // Increased height
            .set("width", "100%")       // Full width
            .set("overflow-y", "auto")  // Vertical scroll
            .set("overflow-x", "auto")  // Horizontal scroll 
            .set("white-space", "pre-wrap") 
            .set("font-family", "monospace")
            .set("margin", "var(--lumo-space-m) 0");
    }

    private Div createResultsContainer() {
        Div container = new Div();
        container.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("padding", "var(--lumo-space-m)")
            .set("margin-bottom", "var(--lumo-space-m)");
        return container;
    }

    private void displayDetailedResults(JSONObject results, Div container) {
        if (results.has("criteriaGrades")) {
            results.getJSONArray("criteriaGrades").forEach(item -> {
                JSONObject criteriaGrade = (JSONObject)item;
                Div criteriaDiv = new Div();
                criteriaDiv.getStyle()
                    .set("margin", "var(--lumo-space-m) 0")
                    .set("padding", "var(--lumo-space-s)")
                    .set("border-left", "3px solid var(--lumo-primary-color)");
                
                criteriaDiv.add(
                    new H6(criteriaGrade.getString("criterionName") + 
                         " - Score: " + criteriaGrade.getInt("score")),
                    new Paragraph(criteriaGrade.getString("feedback"))
                );
                
                container.add(criteriaDiv);
            });
        }

        addFeedbackSection(results, "overallFeedback", "Overall Feedback");
        addFeedbackSection(results, "justification", "Score Justification");
        addImprovementsList(results);
    }

    private void addFeedbackSection(JSONObject results, String key, String header) {
        if (results.has(key)) {
            gradingContent.add(
                new H5(header),
                new Paragraph(results.getString(key))
            );
        }
    }

    private void addImprovementsList(JSONObject results) {
        if (results.has("improvements")) {
            H5 improvementsHeader = new H5("Suggested Improvements");
            Div improvementsDiv = new Div();
            results.getJSONArray("improvements").forEach(item -> {
                improvementsDiv.add(new Paragraph("• " + item.toString()));
            });
            gradingContent.add(improvementsHeader, improvementsDiv);
        }
    }

    @EventListener
    public void handleGradingProgress(GradingProgressEvent event) {
        logger.info("Handling progress event: {} - {}", event.getMessage(), event.getProgress());
        // Use UI.getCurrent() safely
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                if (progressBar != null && statusLabel != null) {
                    progressBar.setValue(event.getProgress());
                    statusLabel.setText(event.getMessage());

                    // Handle completion
                    if (event.getProgress() >= 1.0) {
                        // Clear progress indicators
                        gradingContent.removeAll();
                        
                        // Display the generated content
                        if (event.getRubricContent() != null) {
                            // For rubric generation
                            H4 header = new H4("Generated Rubric");
                            Pre content = new Pre(event.getRubricContent());
                            stylePreContent(content);
                            gradingContent.add(header, content);
                            
                            // Enable the Grade with Rubric button
                            rubricGradeButton.setEnabled(true);
                            storedRubric = event.getRubricContent();
                        }

                        Notification.show("Process completed successfully", 
                            3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }
                }
            });
        }
    }
    
    @EventListener
    public void handleGradingError(GradingErrorEvent event) {
        logger.error("Grading error: {}", event.getError());
        getUI().ifPresent(ui -> ui.access(() -> {
            if (progressBar != null && statusLabel != null) {
                Notification.show("Error: " + event.getError(), 
                    5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                statusLabel.setText("Error: " + event.getError());
                progressBar.setValue(0);
            }
        }));
    }

    private void handleError(String message) {
        Notification.show(message, 5000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
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
                submission.getAssignment(),
                submission.getId()  // Pass current submission ID to exclude it
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
    
    // Create main layout
    VerticalLayout resultLayout = new VerticalLayout();
    resultLayout.setSpacing(true);
    resultLayout.setPadding(true);
    
    // Overall similarity score
    double similarityScore = (double) analysis.get("similarity_score");
    String similarityPercentage = (String) analysis.get("similarity_percentage");
    boolean isPotentialPlagiarism = (boolean) analysis.get("is_potential_plagiarism");
    
    // Create similarity indicator
    H3 scoreHeader = new H3(String.format("Similarity Score: %s", similarityPercentage));
    scoreHeader.getStyle().set("color", similarityScore > 0.9 ? "var(--lumo-error-color)" : 
                                      similarityScore > 0.7 ? "var(--lumo-warning-color)" : 
                                      "var(--lumo-success-color)");
    resultLayout.add(scoreHeader);
    
    if (isPotentialPlagiarism) {
        Div warningDiv = new Div();
        warningDiv.setText("⚠️ High similarity detected - Review recommended");
        warningDiv.getStyle()
            .set("color", "var(--lumo-error-color)")
            .set("font-weight", "bold")
            .set("margin", "var(--lumo-space-m) 0");
        resultLayout.add(warningDiv);
    }
    
    // Display matches if any
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> matches = (List<Map<String, Object>>) analysis.get("matches");
    if (matches != null && !matches.isEmpty()) {
        Grid<Map<String, Object>> matchesGrid = new Grid<>();
        matchesGrid.setAllRowsVisible(true);
        
        matchesGrid.addColumn(map -> map.get("studentName"))
            .setHeader("Student")
            .setAutoWidth(true);
            
        matchesGrid.addColumn(map -> map.get("similarityScore"))
            .setHeader("Similarity")
            .setAutoWidth(true);
        
        matchesGrid.setItems(matches);
        resultLayout.add(new H4("Similar Submissions"), matchesGrid);
    } else {
        resultLayout.add(new Span("No significant similarities found"));
    }
    
    similarityContent.add(resultLayout);
}

private Div createSimilarityIndicator(String percentage, double score) {
    Div indicator = new Div();
    indicator.setText("Similarity Score: " + percentage);
    indicator.getStyle()
        .set("padding", "var(--lumo-space-s)")
        .set("border-radius", "var(--lumo-border-radius-m)")
        .set("font-weight", "bold")
        .set("text-align", "center");
    
    // Color coding based on similarity level
    if (score > 0.8) {
        indicator.getStyle()
            .set("background-color", "var(--lumo-error-color)")
            .set("color", "var(--lumo-base-color)");
    } else if (score > 0.5) {
        indicator.getStyle()
            .set("background-color", "var(--lumo-warning-color)")
            .set("color", "var(--lumo-base-color)");
    } else {
        indicator.getStyle()
            .set("background-color", "var(--lumo-success-color)")
            .set("color", "var(--lumo-base-color)");
    }
    
    return indicator;
}

private Dialog createSequenceDialog(List<Map<String, Object>> sequences) {
    Dialog dialog = new Dialog();
    dialog.setHeaderTitle("Matched Sequences");
    
    VerticalLayout content = new VerticalLayout();
    content.setSpacing(true);
    content.setPadding(true);
    
    // Create a grid for sequences
    Grid<Map<String, Object>> sequenceGrid = new Grid<>();
    sequenceGrid.setAllRowsVisible(true);
    
    sequenceGrid.addColumn(map -> map.get("sequence"))
        .setHeader("Matched Text")
        .setAutoWidth(true);
        
    sequenceGrid.addColumn(map -> map.get("length"))
        .setHeader("Length (words)")
        .setAutoWidth(true);
    
    sequenceGrid.setItems(sequences);
    
    content.add(sequenceGrid);
    dialog.add(content);
    
    // Add close button
    Button closeButton = new Button("Close", e -> dialog.close());
    closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    dialog.getFooter().add(closeButton);
    
    return dialog;
}
}