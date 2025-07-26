package com.example.myjournalapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myjournalapp.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup toolbar as app bar
        Toolbar toolbar = findViewById(R.id.toolbarBack);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
        }

        // Handle toolbar back button
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Show username
        txtUsername = findViewById(R.id.txtUsername);
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        String username = prefs.getString("username", "Guest");
        txtUsername.setText("Username: " + username);
    }
}
