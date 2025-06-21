package com.example.myjournalapp.model;

public class Note {
    private String id;
    private String title;
    private String content;
    private long timestamp;
    private boolean completed; // ✅ New field for checkbox state

    public Note() {}  // Required for Firebase or Gson

    // Constructor without checkbox
    public Note(String id, String title, String content, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.completed = false;
    }

    // Constructor with checkbox
    public Note(String id, String title, String content, long timestamp, boolean completed) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.completed = completed;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public boolean isCompleted() { return completed; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
