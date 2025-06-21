package com.example.myjournalapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SessionManager {
    private static final String PREF_NAME = "user_pref";
    private static final String CURRENT_USER_KEY = "current_user";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Register a new user
    public void registerUser(String username, String password) {
        editor.putString("user_" + username, password);        // Save credentials
        editor.putString(CURRENT_USER_KEY, username);          // Log in immediately
        editor.apply();
    }

    // Log in if credentials match
    public boolean login(String username, String password) {
        String savedPassword = sharedPreferences.getString("user_" + username, null);
        if (savedPassword != null && savedPassword.equals(password)) {
            editor.putString(CURRENT_USER_KEY, username);      // Set session
            editor.apply();
            return true;
        }
        return false;
    }

    // Check if username exists
    public boolean isUserRegistered(String username) {
        return sharedPreferences.contains("user_" + username);
    }

    // Get currently logged-in user
    public String getCurrentUser() {
        return sharedPreferences.getString(CURRENT_USER_KEY, null);
    }

    // Log out only the session (do not delete users)
    public void logout() {
        editor.remove(CURRENT_USER_KEY);
        editor.apply();
    }

    // For debugging/testing only
    public Map<String, ?> getAllUsers() {
        return sharedPreferences.getAll();
    }
}
