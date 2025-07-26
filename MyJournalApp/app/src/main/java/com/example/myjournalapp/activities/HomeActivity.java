package com.example.myjournalapp.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myjournalapp.R;
import com.example.myjournalapp.adapter.NoteAdapter;
import com.example.myjournalapp.model.Note;
import com.example.myjournalapp.utils.NoteStorageHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public class HomeActivity extends AppCompatActivity {

    private ArrayList<Note> noteList;
    private NoteAdapter adapter;
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Force overflow menu to always show
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            @SuppressLint("SoonBlockedPrivateApi") Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_home);

        // Setup views
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fabAddNote);
        recyclerView = findViewById(R.id.recyclerViewNotes);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

        loadAndDisplayNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplayNotes();
    }

    // ✅ SORTING logic + setting adapter
    private void loadAndDisplayNotes() {
        noteList = NoteStorageHelper.getNotes(this);

        // ✅ Sort: reminders first, by soonest time
        Collections.sort(noteList, (n1, n2) -> {
            boolean r1 = n1.hasReminder();
            boolean r2 = n2.hasReminder();

            if (r1 && r2) {
                return Long.compare(n1.getReminderTimeMillis(), n2.getReminderTimeMillis());
            } else if (r1) {
                return -1;
            } else if (r2) {
                return 1;
            } else {
                return Long.compare(n2.getTimestamp(), n1.getTimestamp()); // latest notes first
            }
        });

        adapter = new NoteAdapter(noteList, new NoteAdapter.NoteClickListener() {
            @Override
            public void onNoteClick(int position) {
                Note note = noteList.get(position);
                Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
                intent.putExtra("id", note.getId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                startActivity(intent);
            }

            @Override
            public void onNoteLongClick(int position) {
                noteList.remove(position);
                NoteStorageHelper.saveNotes(HomeActivity.this, noteList);
                adapter.notifyItemRemoved(position);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}
