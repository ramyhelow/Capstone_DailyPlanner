package com.ramyhelow.dailyplanner.objects;

import java.util.HashMap;

public class Note {

    private String mTitle;
    private String mContent;
    private String mId;

    public Note() {}

    public Note(String title, String content, String id) {
        this.mTitle = title;
        this.mContent = content;
        this.mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content){
        mContent = content;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getId() {
        return mId;
    }

    public HashMap<String,String> toFirebaseObject() {
        HashMap<String,String> note =  new HashMap<>();
        note.put("title", mTitle);
        note.put("content", mContent);
        note.put("id", mId);
        return note;
    }
}
