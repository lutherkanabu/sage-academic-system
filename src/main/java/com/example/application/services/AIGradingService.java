/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

/**
 *
 * @author user
 */


import com.example.application.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import jakarta.annotation.PreDestroy;
import com.example.application.events.*;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.retry.support.RetryTemplate;

@Service
public class AIGradingService {
    private static final Logger logger = LoggerFactory.getLogger(AIGradingService.class);
    private static final int TIMEOUT_SECONDS = 30;
    private static final int MAX_SUBMISSION_LENGTH = 1500;
    private static final String CACHE_NAME = "rubrics";
    private static final String ERROR_PREFIX = "AI Grading Error: ";
    
    private final GeminiService geminiService;
    private final GradeRepository gradeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ExecutorService executorService;
    private final Cache rubricCache;
    private final RetryTemplate retryTemplate;

    public AIGradingService(
            GeminiService geminiService,
            GradeRepository gradeRepository,
            ApplicationEventPublisher eventPublisher,
            RetryTemplate retryTemplate) {
        this.geminiService = geminiService;
        this.gradeRepository = gradeRepository;
        this.eventPublisher = eventPublisher;
        this.executorService = Executors.newFixedThreadPool(4);
        this.rubricCache = new ConcurrentMapCacheManager(CACHE_NAME).getCache(CACHE_NAME);
        this.retryTemplate = retryTemplate;
        
        logger.info("Initialized AI Grading Service");
    }

    public CompletableFuture<String> generateRubricAsync(String assignmentDescription, UI ui) {
    String cacheKey = generateCacheKey(assignmentDescription);
    String cachedRubric = rubricCache.get(cacheKey, String.class);
    
    if (cachedRubric != null) {
        updateProgress(ui, "Retrieved cached rubric", 1.0, cachedRubric);
        return CompletableFuture.completedFuture(cachedRubric);
    }

    return CompletableFuture.supplyAsync(() -> {
        try {
            updateProgress(ui, "Initializing AI service...", 0.2, null);
            
            String rubric = executeWithRetry(() -> geminiService.generateRubric(assignmentDescription));
            
            if (rubric == null || rubric.trim().isEmpty()) {
                throw new RuntimeException("Generated rubric is empty");
            }

            logger.info("Generated rubric: {}", rubric);
            updateProgress(ui, "Storing rubric...", 0.8, rubric);
            rubricCache.put(cacheKey, rubric);
            
            // Final update with complete rubric
            updateProgress(ui, "Rubric ready", 1.0, rubric);
            
            return rubric;
        } catch (Exception e) {
            logger.error("Rubric generation failed: ", e);
            updateError(ui, ERROR_PREFIX + e.getMessage());
            throw new CompletionException(e);
        }
    }, executorService);
}
    
    public String getCachedRubric(String assignmentDescription) {
    String cacheKey = generateCacheKey(assignmentDescription);
    return rubricCache.get(cacheKey, String.class);
}
    public CompletableFuture<Grade> gradeWithRubricAsync(Submission submission, String rubric, UI ui) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                updateProgress(ui, "Grading submission...", 0.3, null);  
                
                String submissionText = truncateSubmission(submission.getFileData());
                String prompt = String.format(
                    "Grade this submission using the following rubric:\n%s\n\n" +
                    "Submission:\n%s\n\n" +
                    "Please provide the grade for each criterion and the total score.",
                    rubric, submissionText
                );

                String gradingResult = executeWithRetry(() -> geminiService.generateRubric(prompt));
                
                if (gradingResult == null || gradingResult.trim().isEmpty()) {
                    throw new RuntimeException("Grading result is empty");
                }

                return processGradingResult(submission, rubric, gradingResult, ui);

            } catch (Exception e) {
                logger.error("Grading failed: ", e);
                updateError(ui, ERROR_PREFIX + e.getMessage());
                throw new CompletionException(e);
            }
        }, executorService);
    }
    
    public CompletableFuture<Grade> directGradeAsync(Submission submission, UI ui) {
    return CompletableFuture.supplyAsync(() -> {
        try {
            updateProgress(ui, "Analyzing submission...", 0.3, null);
            
            String submissionText = submission.getExtractedText();
            
            // If no extracted text, try to extract it
            if (submissionText == null || submissionText.isEmpty()) {
                throw new RuntimeException("No text content available for grading");
            }

            String assignmentDescription = submission.getAssignment().getDescription();
            
            // Combined prompt for grading and plagiarism check
            String prompt = String.format(
                "Grade this submission and check for plagiarism. Provide a score between 0-100 with feedback.\n\n" +
                "Assignment Question: %s\n\n" +
                "Student Submission: %s\n\n" +
                "Your response MUST include:\n" +
                "1. Score: [number between 0-100]\n" +
                "2. Originality Score: [percentage of original content]\n" +
                "3. Feedback:\n" +
                "   - [Point 1 about the submission]\n" +
                "   - [Point 2 about the submission]\n" +
                "4. Plagiarism Analysis:\n" +
                "   - [Note any suspicious sections]\n" +
                "   - [Reasoning for suspicion]\n\n" +
                "Note: Evaluate both the academic quality and originality of the work.",
                assignmentDescription,
                submissionText
            );

            String gradingResult = executeWithRetry(() -> geminiService.generateRubric(prompt));
            if (gradingResult == null || gradingResult.trim().isEmpty()) {
                throw new RuntimeException("Grading result is empty");
            }

            // Process the grade
            updateProgress(ui, "Processing grade...", 0.8, gradingResult);
            Grade grade = processQuickGrade(submission, gradingResult);

            // Send full response to UI
            updateProgress(ui, "Grading complete", 1.0, gradingResult);
            
            return grade;

        } catch (Exception e) {
            logger.error("Quick grading failed: ", e);
            updateError(ui, ERROR_PREFIX + e.getMessage());
            throw new CompletionException(e);
        }
    }, executorService);
}
    
    private Grade processQuickGrade(Submission submission, String gradingResult) {
    try {
        // Extract score from the feedback
        int score = extractScoreFromFeedback(gradingResult);
            
        Grade grade = new Grade();
        grade.setSubmission(submission);
        grade.setGradingResults(gradingResult);
        grade.setTotalScore(score);
        grade.setGradedAt(LocalDateTime.now());
        grade.setFinalized(false);

        return gradeRepository.save(grade);

    } catch (Exception e) {
        logger.error("Error processing quick grade result: ", e);
        throw new RuntimeException("Failed to process quick grade", e);
    }
}
    
    private void displayQuickGradeResult(Grade grade, UI ui) {
        if (ui != null) {
            ui.access(() -> {
                String result = String.format(
                    "Grade: %d/100\n\n%s",
                    grade.getTotalScore(),
                    grade.getGradingResults()
                );
                updateProgress(ui, "Grading complete", 1.0, result);
            });
        }
    }

    private String truncateSubmission(byte[] fileData) {
        String text = new String(fileData);
        return text.length() > MAX_SUBMISSION_LENGTH ? 
            text.substring(0, MAX_SUBMISSION_LENGTH) : text;
    }

    private <T> T executeWithRetry(SupplierWithException<T> operation) {
        return retryTemplate.execute(context -> {
            try {
                return operation.get();
            } catch (Exception e) {
                logger.error("Operation failed (attempt {}): ", context.getRetryCount(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private Grade processGradingResult(Submission submission, String rubric, 
                                     String gradingResult, UI ui) {
        updateProgress(ui, "Processing results...", 0.8, null); 

        try {
            int score = extractScoreFromFeedback(gradingResult);

            Grade grade = new Grade();
            grade.setSubmission(submission);
            grade.setAiGeneratedRubric(rubric);
            grade.setGradingResults(gradingResult);
            grade.setTotalScore(score);
            grade.setGradedAt(LocalDateTime.now());
            grade.setFinalized(false);

            return gradeRepository.save(grade);

        } catch (Exception e) {
            logger.error("Error processing grading result: ", e);
            throw new RuntimeException("Failed to process grading results", e);
        }
    }

    private int extractScoreFromFeedback(String feedback) {
    try {
        if (feedback == null || feedback.trim().isEmpty()) {
            logger.warn("Empty feedback received");
            return 70; // Default score
        }

        String[] lines = feedback.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("score:")) {
                String scoreText = line.split(":")[1].trim()
                    .replaceAll("[^0-9]", ""); // Remove any non-numeric characters
                if (!scoreText.isEmpty()) {
                    int score = Integer.parseInt(scoreText);
                    if (score >= 0 && score <= 100) {
                        return score;
                    }
                }
            }
        }
        logger.warn("No valid score found in feedback, using default");
        return 70;
    } catch (Exception e) {
        logger.error("Error extracting score from feedback: ", e);
        return 70;
    }
}

    private String generateCacheKey(String text) {
        return "rubric_" + text.hashCode();
    }

    private void updateProgress(UI ui, String message, double progress, String rubricContent) {
    if (ui != null) {
        ui.access(() -> eventPublisher.publishEvent(
            new GradingProgressEvent(this, message, progress, rubricContent)));
    }
}

    private void updateError(UI ui, String error) {
        if (ui != null) {
            ui.access(() -> eventPublisher.publishEvent(
                new GradingErrorEvent(this, error)));
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}