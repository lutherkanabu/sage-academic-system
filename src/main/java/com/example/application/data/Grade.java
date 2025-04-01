/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.data;

/**
 *
 * @author user
 */
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Grade extends AbstractEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private Submission submission;
    
    @Column(columnDefinition = "TEXT")
    private String aiGeneratedRubric;
    
    @Column(columnDefinition = "TEXT")
    private String gradingResults;
    
    private int totalScore;
    private Integer manualGrade;
    private String lecturerFeedback;
    private boolean isPosted = false;
    private LocalDateTime gradedAt;
    private LocalDateTime postedAt;
    private boolean isFinalized;

    // Getters and Setters
    public Submission getSubmission() { return submission; }
    public void setSubmission(Submission submission) { this.submission = submission; }
    
    public String getAiGeneratedRubric() { return aiGeneratedRubric; }
    public void setAiGeneratedRubric(String aiGeneratedRubric) { this.aiGeneratedRubric = aiGeneratedRubric; }
    
    public String getGradingResults() { return gradingResults; }
    public void setGradingResults(String gradingResults) { this.gradingResults = gradingResults; }
    
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    
    public Integer getManualGrade() { return manualGrade; }
    public void setManualGrade(Integer manualGrade) { this.manualGrade = manualGrade; }
    
    public String getLecturerFeedback() { return lecturerFeedback; }
    public void setLecturerFeedback(String lecturerFeedback) { this.lecturerFeedback = lecturerFeedback; }
    
    public boolean isPosted() { return isPosted; }
    public void setPosted(boolean posted) { isPosted = posted; }
    
    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }
    
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    
    public boolean isFinalized() { return isFinalized; }
    public void setFinalized(boolean finalized) { isFinalized = finalized; }
}