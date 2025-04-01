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
public class GradingErrorEvent extends ApplicationEvent {
    private final String error;

    public GradingErrorEvent(Object source, String error) {
        super(source);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
