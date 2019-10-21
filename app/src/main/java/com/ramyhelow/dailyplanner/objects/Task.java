package com.ramyhelow.dailyplanner.objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Task {

    private String title;
    private String content;
    private String date;
    private String state;
    private String taskId;
    private String projectKey;

    public Task() {}

    public Task(String title, String content, String date, String state, String taskId, String projectKey) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.state = state;
        this.taskId = taskId;
        this.projectKey = projectKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFormattedDate() {

        DateFormat originalFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        DateFormat targetFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        Date date;

        try {
            date = originalFormat.parse(this.date);
        } catch (ParseException e){
            return null;
        }
        return targetFormat.format(date);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTaskId(){
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }


    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> todo =  new HashMap<>();
        todo.put("title", title);
        todo.put("content", content);
        todo.put("date", date);
        todo.put("state", state);
        todo.put("taskId", taskId);
        todo.put("projectKey", projectKey);

        return todo;
    }

}
