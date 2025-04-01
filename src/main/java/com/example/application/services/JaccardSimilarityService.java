/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.services;

/**
 *
 * @author user
 */
import org.springframework.stereotype.Service;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JaccardSimilarityService {
    private static final Logger logger = LoggerFactory.getLogger(JaccardSimilarityService.class);
    private static final double HIGH_SIMILARITY_THRESHOLD = 0.8;
    
    public double calculateJaccardSimilarity(String text1, String text2) {
        // Convert texts to word sets
        Set<String> words1 = tokenizeText(text1);
        Set<String> words2 = tokenizeText(text2);
        
        // Calculate intersection
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        // Calculate union
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        // Avoid division by zero
        if (union.isEmpty()) {
            return 0.0;
        }
        
        return (double) intersection.size() / union.size();
    }
    
    private Set<String> tokenizeText(String text) {
        // Remove punctuation and convert to lowercase
        String cleanText = text.toLowerCase()
                             .replaceAll("[^a-z\\s]", "")
                             .trim();
        
        // Split into words and convert to Set
        return new HashSet<>(Arrays.asList(cleanText.split("\\s+")));
    }
}
