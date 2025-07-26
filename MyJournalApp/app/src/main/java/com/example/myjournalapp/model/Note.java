package com.example.myjournalapp.model;

public class Note {
    private String id;
    private String title;
    private String content;
    private long timestamp;
    private boolean completed;

    // ✅ NEW: Reminder fields
    private boolean hasReminder;
    private long reminderTimeMillis;

    public Note() {} // Required for Firebase or Gson

    // Constructor without reminder
    public Note(String id, String title, String content, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.completed = false;
        this.hasReminder = false;
        this.reminderTimeMillis = 0;
    }

    // Constructor with completed state
    public Note(String id, String title, String content, long timestamp, boolean completed) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.completed = completed;
        this.hasReminder = false;
        this.reminderTimeMillis = 0;
    }

    // Constructor with reminder data
    public Note(String id, String title, String content, long timestamp, boolean completed, boolean hasReminder, long reminderTimeMillis) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
        this.completed = completed;
        this.hasReminder = hasReminder;
        this.reminderTimeMillis = reminderTimeMillis;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public boolean isCompleted() { return completed; }
    public boolean hasReminder() { return hasReminder; }
    public long getReminderTimeMillis() { return reminderTimeMillis; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setHasReminder(boolean hasReminder) { this.hasReminder = hasReminder; }
    public void setReminderTimeMillis(long reminderTimeMillis) { this.reminderTimeMillis = reminderTimeMillis; }
}
