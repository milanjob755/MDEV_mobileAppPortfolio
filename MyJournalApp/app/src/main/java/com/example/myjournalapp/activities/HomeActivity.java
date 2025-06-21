package com.example.myjournalapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myjournalapp.R;
import com.example.myjournalapp.adapter.NoteAdapter;
import com.example.myjournalapp.model.Note;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<Note> noteList = new ArrayList<>();
    private NoteAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private int editingPosition = -1;

    private FirebaseFirestore db;
    private String userId;

    private final ActivityResultLauncher<Intent> noteActivityLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    loadNotesFromFirestore(); // Always refresh notes from Firestore after adding/editing
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new NoteAdapter(noteList, new NoteAdapter.NoteClickListener() {
            @Override
            public void onNoteClick(int position) {
                editingPosition = position;
                Note note = noteList.get(position);
                Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                intent.putExtra("id", note.getId());
                noteActivityLauncher.launch(intent);
            }

            @Override
            public void onNoteLongClick(int position) {
                Note note = noteList.get(position);
                db.collection("users")
                        .document(userId)
                        .collection("notes")
                        .document(note.getId())
                        .delete()
                        .addOnSuccessListener(unused -> {
                            noteList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(HomeActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show());
            }
        });

        recyclerView = findViewById(R.id.recyclerViewNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        fab = findViewById(R.id.fabAddNote);
        fab.setOnClickListener(v -> {
            editingPosition = -1;
            noteActivityLauncher.launch(new Intent(this, AddNoteActivity.class));
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        loadNotesFromFirestore();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromFirestore(); // Refresh list on resume
    }

    private void loadNotesFromFirestore() {
        db.collection("users")
                .document(userId)
                .collection("notes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    noteList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        Note note = doc.toObject(Note.class);
                        if (note != null) {
                            note.setId(doc.getId());
                            noteList.add(note);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notes: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
