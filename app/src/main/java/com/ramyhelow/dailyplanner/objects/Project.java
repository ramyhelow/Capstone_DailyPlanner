package com.ramyhelow.dailyplanner.objects;

import java.util.HashMap;

public class Project {

    private String title;
    private String id;

    public Project() {}

    public Project(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> project =  new HashMap<>();
        project.put("title", title);
        project.put("id", id);

        return project;
    }
}

