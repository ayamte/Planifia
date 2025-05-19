package com.example.luna;

import java.io.Serializable;

public class Task_Class implements Serializable {
    private String title;
    private String description;
    private String startTime;
    private String dueDate;
    private String category;
    private String status;
    private String dateTime;
    private String endTime;
    private double priorityScore; // Nouveau champ pour le score de priorité

    // Constructeur existant
    public Task_Class(String title, String description, String startTime, String dueDate, String category, String status, String dateTime, String endTime) {
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.dueDate = dueDate;
        this.category = category;
        this.status = status;
        this.dateTime = dateTime;
        this.endTime = endTime;
        this.priorityScore = 0.0; // Valeur par défaut
    }

    // Constructeur sans argument requis pour Firebase
    public Task_Class() {
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Nouveaux getters et setters pour le score de priorité
    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }
}
