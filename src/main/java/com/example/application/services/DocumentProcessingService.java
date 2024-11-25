package com.example.application.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class DocumentProcessingService {
   
   public String extractText(byte[] fileData, String fileName) throws Exception {
       if (fileData == null || fileData.length == 0) {
           throw new IllegalArgumentException("File data cannot be empty");
       }
       
       if (fileName == null || fileName.trim().isEmpty()) {
           throw new IllegalArgumentException("Filename cannot be empty");
       }

       if (fileName.toLowerCase().endsWith(".pdf")) {
           return extractPdfText(fileData);
       } else if (fileName.toLowerCase().endsWith(".docx")) {
           return extractDocxText(fileData);
       }
       throw new IllegalArgumentException("Unsupported file format. Only PDF and DOCX are supported.");
   }

   private String extractPdfText(byte[] fileData) throws Exception {
       try (PDDocument document = PDDocument.load(new ByteArrayInputStream(fileData))) {
           if (document.getNumberOfPages() < 1) {
               throw new RuntimeException("PDF document appears to be empty or corrupted");
           }
           
           PDFTextStripper stripper = new PDFTextStripper();
           String text = stripper.getText(document);
           
           if (text == null || text.trim().isEmpty()) {
               throw new RuntimeException("No text content could be extracted from PDF");
           }
           
           return text;
       } catch (IOException e) {
           throw new RuntimeException("Failed to process PDF: " + e.getMessage(), e);
       }
   }

   private String extractDocxText(byte[] fileData) throws Exception {
       try (ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
            XWPFDocument document = new XWPFDocument(bis)) {
           
           try (XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
               String text = extractor.getText();
               
               if (text == null || text.trim().isEmpty()) {
                   throw new RuntimeException("No text content could be extracted from Word document");
               }
               
               return text;
           }
       } catch (IOException e) {
           throw new RuntimeException("Failed to process Word document: " + e.getMessage(), e);
       }
   }

   public Map<String, Object> analyzeSimilarity(String text, List<String> otherTexts) {
       if (text == null || text.trim().isEmpty()) {
           throw new IllegalArgumentException("Input text cannot be empty");
       }
       
       Map<String, Object> results = new HashMap<>();
       double maxSimilarity = 0;
       
       if (otherTexts != null && !otherTexts.isEmpty()) {
           for (String otherText : otherTexts) {
               if (otherText != null && !otherText.trim().isEmpty()) {
                   double similarity = calculateSimilarity(text, otherText);
                   maxSimilarity = Math.max(maxSimilarity, similarity);
               }
           }
       }
       
       results.put("similarity_score", maxSimilarity);
       results.put("is_potential_plagiarism", maxSimilarity > 0.8);
       
       return results;
   }

   private double calculateSimilarity(String text1, String text2) {
       if (text1 == null || text2 == null) {
           return 0.0;
       }
       
       Set<String> set1 = new HashSet<>(Arrays.asList(text1.toLowerCase().split("\\s+")));
       Set<String> set2 = new HashSet<>(Arrays.asList(text2.toLowerCase().split("\\s+")));
       
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