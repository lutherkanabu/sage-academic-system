package com.example.application.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.application.data.*;

@Service
public class DocumentProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);
    private final SubmissionRepository submissionRepository;

    public DocumentProcessingService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    // Extraction methods
    public String extractText(byte[] fileData, String fileName) throws Exception {
        validateInput(fileData, fileName);
        logger.debug("Processing file: {}, size: {} bytes", fileName, fileData.length);
        
        try {
            if (fileName.toLowerCase().endsWith(".pdf")) {
                return extractPdfText(fileData);
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                return extractDocxText(fileData);
            } else {
                throw new IllegalArgumentException("Unsupported file format. Only PDF and DOCX are supported.");
            }
        } catch (Exception e) {
            logger.error("Error processing document: {}", e.getMessage());
            throw new Exception("Error processing document: " + e.getMessage());
        }
    }

    private void validateInput(byte[] fileData, String fileName) {
        if (fileData == null || fileData.length == 0) {
            logger.error("File data is empty or null");
            throw new IllegalArgumentException("File data cannot be empty");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            logger.error("Filename is empty or null");
            throw new IllegalArgumentException("Filename cannot be empty");
        }
    }

    private String extractPdfText(byte[] fileData) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileData))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("No text content could be extracted from PDF");
            }
            
            logger.debug("Successfully extracted {} characters from PDF", text.length());
            return text;
        } catch (IOException e) {
            logger.error("Failed to process PDF: {}", e.getMessage());
            throw new IOException("Failed to process PDF: " + e.getMessage(), e);
        }
    }

    private String extractDocxText(byte[] fileData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
             XWPFDocument document = new XWPFDocument(bis)) {
            
            try (XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                String text = extractor.getText();
                
                if (text == null || text.trim().isEmpty()) {
                    throw new IOException("No text content could be extracted from Word document");
                }
                
                logger.debug("Successfully extracted {} characters from DOCX", text.length());
                return text;
            }
        } catch (IOException e) {
            logger.error("Failed to process Word document: {}", e.getMessage());
            throw new IOException("Failed to process Word document: " + e.getMessage(), e);
        }
    }

    // Enhanced similarity detection
    @Transactional(readOnly = true)
    public Map<String, Object> analyzeSimilarity(String text, Assignment assignment) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be empty");
        }

        Map<String, Object> results = new HashMap<>();
        List<Map<String, Object>> matches = new ArrayList<>();
        double highestSimilarity = 0.0;

        // Get all other submissions for this assignment
        List<Submission> otherSubmissions = submissionRepository.findByAssignment(assignment);
        
        for (Submission submission : otherSubmissions) {
            try {
                String otherText = extractText(submission.getFileData(), submission.getFileName());
                double similarity = calculateSimilarity(text, otherText);
                
                if (similarity > 0.3) { // Only track significant matches
                    Map<String, Object> match = new HashMap<>();
                    match.put("studentName", submission.getStudent().getUser().getFirstName() + 
                                           " " + submission.getStudent().getUser().getLastName());
                    match.put("similarityScore", similarity);
                    matches.add(match);
                    
                    highestSimilarity = Math.max(highestSimilarity, similarity);
                }
            } catch (Exception e) {
                logger.error("Error processing submission for similarity check: ", e);
            }
        }

        results.put("similarity_score", highestSimilarity);
        results.put("is_potential_plagiarism", highestSimilarity > 0.8);
        results.put("similarity_percentage", String.format("%.2f%%", highestSimilarity * 100));
        results.put("matches", matches);
        
        logger.info("Similarity analysis complete. Highest score: {}", highestSimilarity);
        return results;
    }

    private double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }

        // Normalize texts
        text1 = text1.toLowerCase().replaceAll("\\s+", " ").trim();
        text2 = text2.toLowerCase().replaceAll("\\s+", " ").trim();

        // Split into words
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");

        // Create sets of 3-word sequences (trigrams)
        Set<String> trigrams1 = new HashSet<>();
        Set<String> trigrams2 = new HashSet<>();

        for (int i = 0; i < words1.length - 2; i++) {
            trigrams1.add(words1[i] + " " + words1[i + 1] + " " + words1[i + 2]);
        }
        
        for (int i = 0; i < words2.length - 2; i++) {
            trigrams2.add(words2[i] + " " + words2[i + 1] + " " + words2[i + 2]);
        }

        // Calculate Jaccard similarity coefficient
        Set<String> union = new HashSet<>(trigrams1);
        union.addAll(trigrams2);
        
        Set<String> intersection = new HashSet<>(trigrams1);
        intersection.retainAll(trigrams2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}