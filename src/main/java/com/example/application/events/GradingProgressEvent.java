/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.application.events;

/**
 *
 * @author user
 */
import org.springframework.context.ApplicationEvent;

public class GradingProgressEvent extends ApplicationEvent {
    private final String message;
    private final double progress;
    private final String rubricContent;

    public GradingProgressEvent(Object source, String message, double progress, String rubricContent) {
        super(source);
        this.message = message;
        this.progress = progress;
        this.rubricContent = rubricContent;
    }

    // Add getter for rubricContent
    public String getRubricContent() {
        return rubricContent;
    }

    public String getMessage() {
        return message;
    }

    public double getProgress() {
        return progress;
    }
}


