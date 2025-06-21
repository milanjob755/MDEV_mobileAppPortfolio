package com.example.myjournalapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myjournalapp.R;
import com.example.myjournalapp.adapter.NoteAdapter;
import com.example.myjournalapp.model.Note;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private ArrayList<Note> allNotes = new ArrayList<>();
    private ArrayList<Note> filteredNotes = new ArrayList<>();
    private Note selectedNote;

    private FirebaseFirestore db;
    private String userId;

    private final ActivityResultLauncher<Intent> editNoteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String title = result.getData().getStringExtra("title");
                    String content = result.getData().getStringExtra("content");
                    String id = result.getData().getStringExtra("id");

                    if (id != null && title != null && content != null) {

                        Note updatedNote = new Note(id, title, content, System.currentTimeMillis(), false);
                        db.collection("users")
                                .document(userId)
                                .collection("notes")
                                .document(id)
                                .set(updatedNote)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                                    loadNotesFromFirestore();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerViewByDate);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new NoteAdapter(filteredNotes, new NoteAdapter.NoteClickListener() {
            @Override
            public void onNoteClick(int position) {
                selectedNote = filteredNotes.get(position);
                Intent intent = new Intent(CalendarActivity.this, AddNoteActivity.class);
                intent.putExtra("title", selectedNote.getTitle());
                intent.putExtra("content", selectedNote.getContent());
                intent.putExtra("id", selectedNote.getId()); // Pass ID for editing
                editNoteLauncher.launch(intent);
            }

            @Override
            public void onNoteLongClick(int position) {
                Note noteToRemove = filteredNotes.get(position);
                new AlertDialog.Builder(CalendarActivity.this)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            db.collection("users")
                                    .document(userId)
                                    .collection("notes")
                                    .document(noteToRemove.getId())
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(CalendarActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                                        loadNotesFromFirestore();
                                    });
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d%02d%02d", year, month + 1, dayOfMonth);
            filterNotesByDate(selectedDate);
        });

        loadNotesFromFirestore();

        MaterialToolbar toolbar = findViewById(R.id.toolbarBack);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadNotesFromFirestore() {
        db.collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allNotes.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Note note = doc.toObject(Note.class);
                        if (note != null) {
                            note.setId(doc.getId()); // Set Firestore document ID
                            allNotes.add(note);
                        }
                    }
                    filterNotesByDate(getSelectedDateKey());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void filterNotesByDate(String dateKey) {
        filteredNotes.clear();
        for (Note note : allNotes) {
            String noteDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                    .format(new Date(note.getTimestamp()));
            if (noteDate.equals(dateKey)) {
                filteredNotes.add(note);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String getSelectedDateKey() {
        return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date(calendarView.getDate()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromFirestore();
    }
}
