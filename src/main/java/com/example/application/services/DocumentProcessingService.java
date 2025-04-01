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
    private static final double SIMILARITY_THRESHOLD = 0.7;
    
    private final SubmissionRepository submissionRepository;
    private final JaccardSimilarityService jaccardService;

    public DocumentProcessingService(SubmissionRepository submissionRepository,
                                   JaccardSimilarityService jaccardService) {
        this.submissionRepository = submissionRepository;
        this.jaccardService = jaccardService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> analyzeSimilarity(String currentText, Assignment assignment, Long currentSubmissionId) {
    logger.info("Starting Jaccard similarity analysis for assignment: {}", assignment.getId());
    Map<String, Object> results = new HashMap<>();
    List<Map<String, Object>> matches = new ArrayList<>();
    double highestSimilarity = 0.0;

    // Get other submissions excluding current submission
    List<Submission> otherSubmissions = submissionRepository.findByAssignmentAndIdNot(assignment, currentSubmissionId);
    
    for (Submission submission : otherSubmissions) {
        try {
            String otherText = submission.getExtractedText();
            if (otherText == null || otherText.trim().isEmpty()) {
                continue;
            }

            double similarity = jaccardService.calculateJaccardSimilarity(currentText, otherText);

            if (similarity > SIMILARITY_THRESHOLD) {
                Map<String, Object> match = new HashMap<>();
                match.put("studentName", submission.getStudent().getUser().getFirstName() + 
                        " " + submission.getStudent().getUser().getLastName());
                match.put("similarityScore", String.format("%.4f", similarity));
                match.put("submissionId", submission.getId());
                
                if (similarity > 0.9) {
                    match.put("warning", "Very high similarity detected - potential direct copy");
                }
                
                matches.add(match);
                highestSimilarity = Math.max(highestSimilarity, similarity);
            }
        } catch (Exception e) {
            logger.error("Error processing submission for similarity check: ", e);
        }
    }

    results.put("similarity_score", highestSimilarity);
    results.put("is_potential_plagiarism", highestSimilarity > 0.9);
    results.put("similarity_percentage", String.format("%.2f%%", highestSimilarity * 100));
    results.put("matches", matches);
    results.put("total_submissions_checked", otherSubmissions.size());
    
    logger.info("Completed similarity analysis. Highest similarity: {}", highestSimilarity);
    return results;
}

    public String extractText(byte[] fileData, String fileName) throws Exception {
        validateInput(fileData, fileName);
        logger.debug("Processing file: {}, size: {} bytes", fileName, fileData.length);
        
        try {
            String extractedText;
            if (fileName.toLowerCase().endsWith(".pdf")) {
                extractedText = extractPdfText(fileData);
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                extractedText = extractDocxText(fileData);
            } else {
                throw new IllegalArgumentException("Unsupported file format. Only PDF and DOCX are supported.");
            }

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new IOException("No text content could be extracted from document");
            }
            
            extractedText = extractedText.trim()
                                      .replaceAll("\\s+", " ")
                                      .replaceAll("\\p{C}", "");
            
            logger.debug("Successfully extracted {} characters", extractedText.length());
            return extractedText;

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
    
    @Transactional
    public void processSubmission(Submission submission) throws Exception {
        try {
            String extractedText = extractText(submission.getFileData(), submission.getFileName());
            submission.setExtractedText(extractedText);
            submissionRepository.save(submission);
            logger.info("Successfully processed and stored text for submission ID: {}", submission.getId());
        } catch (Exception e) {
            logger.error("Failed to process submission: {}", e.getMessage());
            throw e;
        }
    }
}
