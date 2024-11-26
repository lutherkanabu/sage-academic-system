package com.example.application.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DocumentProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);

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

    // Basic similarity detection - will be enhanced in next phase
    public Map<String, Object> analyzeSimilarity(String text, List<String> otherTexts) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input text cannot be empty");
        }
        
        Map<String, Object> results = new HashMap<>();
        double maxSimilarity = 0;
        String mostSimilarText = null;
        
        if (otherTexts != null && !otherTexts.isEmpty()) {
            for (String otherText : otherTexts) {
                if (otherText != null && !otherText.trim().isEmpty()) {
                    double similarity = calculateSimilarity(text, otherText);
                    if (similarity > maxSimilarity) {
                        maxSimilarity = similarity;
                        mostSimilarText = otherText;
                    }
                }
            }
        }
        
        results.put("similarity_score", maxSimilarity);
        results.put("is_potential_plagiarism", maxSimilarity > 0.8);
        results.put("similarity_percentage", String.format("%.2f%%", maxSimilarity * 100));
        
        logger.debug("Similarity analysis complete. Score: {}", maxSimilarity);
        return results;
    }

    private double calculateSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        // Convert to word sets for comparison
        Set<String> set1 = new HashSet<>(Arrays.asList(text1.toLowerCase().split("\\W+")));
        Set<String> set2 = new HashSet<>(Arrays.asList(text2.toLowerCase().split("\\W+")));
        
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return (double) intersection.size() / union.size();
    }
}