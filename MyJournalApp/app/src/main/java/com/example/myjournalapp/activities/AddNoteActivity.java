package com.example.myjournalapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myjournalapp.R;
import com.example.myjournalapp.model.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddNoteActivity extends AppCompatActivity {

    EditText edtTitle, edtContent;
    Button btnSave;

    String noteId = null;

    FirebaseFirestore db;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get existing note data if editing
        noteId = getIntent().getStringExtra("id");
        String existingTitle = getIntent().getStringExtra("title");
        String existingContent = getIntent().getStringExtra("content");

        if (existingTitle != null) edtTitle.setText(existingTitle);
        if (existingContent != null) edtContent.setText(existingContent);

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String content = edtContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Use null for ID (Firestore auto-generates it), and set current timestamp
            Note note = new Note(null, title, content, System.currentTimeMillis(), false);

            if (noteId == null) {
                // 🔹 Add new note
                db.collection("users")
                        .document(userId)
                        .collection("notes")
                        .add(note)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                // Update existing note
                db.collection("users")
                        .document(userId)
                        .collection("notes")
                        .document(noteId)
                        .set(note)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
