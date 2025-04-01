/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

/**
 *
 * @author user
 */
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;

@Service
public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private static final String MODEL_NAME = "gemini-pro";
    
    private final String projectId;
    private final String location;

    public GeminiService(
            @Value("${google.cloud.project-id}") String projectId,
            @Value("${google.cloud.location}") String location) {
        this.projectId = projectId;
        this.location = location;
    }

    public String generateRubric(String prompt) throws IOException {
        logger.info("Generating rubric with prompt: {}", prompt);
        
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(MODEL_NAME, vertexAI);
            
            String fullPrompt = String.format(
                "Create a detailed grading rubric with 4 criteria (0-25 points each) for this assignment.\n" +
                "Format each criterion as:\n" +
                "[Criterion Name] (25 pts): [Description]\n" +
                "- Excellent (21-25): [what merits full points]\n" +
                "- Good (16-20): [what merits good points]\n" +
                "- Fair (11-15): [what needs improvement]\n" +
                "- Poor (0-10): [what is insufficient]\n\n" +
                "Assignment: %s", prompt);

            // Generate content directly with the prompt string
            var response = model.generateContent(fullPrompt);
            
            // Extract text from response using ResponseHandler
            String result = ResponseHandler.getText(response);
            
            if (result != null && !result.trim().isEmpty()) {
                logger.info("Successfully generated rubric");
                return result;
            }
            
            throw new IOException("No content generated from AI model");
        } catch (Exception e) {
            logger.error("Error generating rubric: ", e);
            throw new IOException("Failed to generate rubric: " + e.getMessage(), e);
        }
    }
}