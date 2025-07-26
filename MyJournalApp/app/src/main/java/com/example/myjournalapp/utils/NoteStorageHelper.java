package com.example.myjournalapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.myjournalapp.model.Note;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NoteStorageHelper {

    private static final String PREF_NAME = "notes_pref";

    public static void saveNotes(Context context, List<Note> notes) {
        SessionManager sessionManager = new SessionManager(context);
        String username = sessionManager.getCurrentUser();

        if (username == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(notes);
        editor.putString("notes_" + username, json);
        editor.apply();
    }

    // Default version that uses current user from session
    public static ArrayList<Note> getNotes(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String username = sessionManager.getCurrentUser();
        return getNotes(context, username);  // Use overloaded version
    }

    // ✅ Overloaded version that takes username explicitly
    public static ArrayList<Note> getNotes(Context context, String username) {
        if (username == null) return new ArrayList<>();

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString("notes_" + username, null);

        if (json == null) return new ArrayList<>();

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
