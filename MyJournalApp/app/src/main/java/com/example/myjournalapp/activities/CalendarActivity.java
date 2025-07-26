package com.example.myjournalapp.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myjournalapp.R;
import com.example.myjournalapp.adapter.NoteAdapter;
import com.example.myjournalapp.model.Note;
import com.example.myjournalapp.utils.NoteStorageHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private NoteAdapter adapter;
    private ArrayList<Note> allNotes;
    private ArrayList<Note> filteredNotes;
    private Note selectedNote;

    private final ActivityResultLauncher<Intent> editNoteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String title = result.getData().getStringExtra("title");
                    String content = result.getData().getStringExtra("content");
                    String id = result.getData().getStringExtra("id");

                    if (id != null && title != null && content != null) {
                        for (int i = 0; i < allNotes.size(); i++) {
                            if (allNotes.get(i).getId().equals(id)) {
                                Note updatedNote = new Note(id, title, content, System.currentTimeMillis());
                                allNotes.set(i, updatedNote);
                                break;
                            }
                        }

                        // Save and refresh
                        NoteStorageHelper.saveNotes(this, allNotes);
                        filterNotesByDate(getSelectedDateKey());
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

        allNotes = NoteStorageHelper.getNotes(this);
        filteredNotes = new ArrayList<>();

        adapter = new NoteAdapter(filteredNotes, new NoteAdapter.NoteClickListener() {
            @Override
            public void onNoteClick(int position) {
                selectedNote = filteredNotes.get(position);
                Intent intent = new Intent(CalendarActivity.this, AddNoteActivity.class);
                intent.putExtra("title", selectedNote.getTitle());
                intent.putExtra("content", selectedNote.getContent());
                intent.putExtra("id", selectedNote.getId()); // important
                editNoteLauncher.launch(intent);
            }

            @Override
            public void onNoteLongClick(int position) {
                Note noteToRemove = filteredNotes.get(position);
                new AlertDialog.Builder(CalendarActivity.this)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Remove from both lists
                            allNotes.removeIf(n -> n.getId().equals(noteToRemove.getId()));
                            filteredNotes.remove(position);
                            adapter.notifyItemRemoved(position);

                            // Save updated notes
                            NoteStorageHelper.saveNotes(CalendarActivity.this, allNotes);
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

        // Load today's notes
        filterNotesByDate(getSelectedDateKey());

        // Back toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbarBack);
        toolbar.setNavigationOnClickListener(v -> finish());
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
        allNotes = NoteStorageHelper.getNotes(this); // reload from storage
        String selectedDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                .format(new Date(calendarView.getDate()));
        filterNotesByDate(selectedDate); // refresh
    }
}
