package com.example.planifia;

import java.io.Serializable;

public class Event_Class implements Serializable {
    private static final long serialVersionUID = 1L;  // Add this line

    private String title;
    private String description;
    private String dueDate;
    private String startTime;
    private String endTime;
    private String category;
    private String location;

    // No-argument constructor for Firebase
    public Event_Class() {
    }

    // Existing constructor
    public Event_Class(String title, String description, String dueDate, String startTime, String endTime, String category, String location) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
        this.location = location;
    }

    // Make sure you have all getters and setters
    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getCategory() { return category; }
    public String getLocation() { return location; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setCategory(String category) { this.category = category; }
    public void setLocation(String location) { this.location = location; }
}