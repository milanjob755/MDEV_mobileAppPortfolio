package com.example.myjournalapp.activities;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myjournalapp.R;
import com.example.myjournalapp.model.Note;
import com.example.myjournalapp.utils.NoteStorageHelper;
import com.example.myjournalapp.utils.ReminderReceiver;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Calendar;

public class AddNoteActivity extends AppCompatActivity {

    EditText edtTitle, edtContent;
    Button btnSave;
    TimePicker timePicker;
    CheckBox chkSetReminder;
    String noteId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        // Permissions for notifications and exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return;
            }
        }

        // Bind Views
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        chkSetReminder = findViewById(R.id.chkSetReminder);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSave);

        MaterialToolbar toolbar = findViewById(R.id.toolbarBack);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Hide TimePicker initially
        timePicker.setVisibility(View.GONE);

        // Toggle time picker visibility
        chkSetReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timePicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Load existing note
        Intent intent = getIntent();
        noteId = intent.getStringExtra("id");
        String existingTitle = intent.getStringExtra("title");
        String existingContent = intent.getStringExtra("content");

        if (existingTitle != null) edtTitle.setText(existingTitle);
        if (existingContent != null) edtContent.setText(existingContent);

        // Save Button Logic (saves note + reminder)
        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String content = edtContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean reminderChecked = chkSetReminder.isChecked();
            long reminderTimeMillis = 0;

            if (reminderChecked) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendar.set(Calendar.MINUTE, timePicker.getMinute());
                calendar.set(Calendar.SECOND, 0);

                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }

                reminderTimeMillis = calendar.getTimeInMillis();

                // Set alarm
                Intent reminderIntent = new Intent(getApplicationContext(), ReminderReceiver.class);
                reminderIntent.putExtra("reminder_title", title);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getApplicationContext(), 0, reminderIntent, PendingIntent.FLAG_IMMUTABLE);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeMillis, pendingIntent);

                createNotificationChannel(); // required on Android 8+
            }

            Note newNote = new Note(
                    noteId != null ? noteId : String.valueOf(System.currentTimeMillis()),
                    title,
                    content,
                    System.currentTimeMillis(),
                    false, // completed
                    reminderChecked,
                    reminderTimeMillis
            );

            ArrayList<Note> notes = NoteStorageHelper.getNotes(this);
            if (noteId != null) {
                for (int i = 0; i < notes.size(); i++) {
                    if (notes.get(i).getId().equals(noteId)) {
                        notes.set(i, newNote);
                        break;
                    }
                }
            } else {
                notes.add(newNote);
            }

            NoteStorageHelper.saveNotes(this, notes);

            Toast.makeText(this, "Note saved" + (reminderChecked ? " with reminder!" : ""), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = "Reminders";
            String description = "Reminder Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("reminder_channel", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
