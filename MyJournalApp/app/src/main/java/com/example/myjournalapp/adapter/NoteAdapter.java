package com.example.myjournalapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myjournalapp.R;
import com.example.myjournalapp.model.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface NoteClickListener {
        void onNoteClick(int position);
        void onNoteLongClick(int position);
    }

    private final ArrayList<Note> notes;
    private final NoteClickListener listener;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public NoteAdapter(ArrayList<Note> notes, NoteClickListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.txtTitle.setText(note.getTitle());

        // Content preview
        String[] words = note.getContent().split("\\s+");
        String preview = words.length > 3
                ? words[0] + " " + words[1] + " " + words[2] + "..."
                : note.getContent();
        holder.txtContent.setText(preview);

        // Date formatting
        String formattedDate = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                .format(new Date(note.getTimestamp()));
        holder.txtDate.setText(formattedDate);

        // ✅ Handle checkbox toggle
        holder.checkCompleted.setOnCheckedChangeListener(null); // prevent flicker
        holder.checkCompleted.setChecked(note.isCompleted());
        holder.checkCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            note.setCompleted(isChecked);

            // 🔄 Update completion status in Firestore
            db.collection("users")
                    .document(userId)
                    .collection("notes")
                    .document(note.getId())
                    .update("completed", isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtContent, txtDate;
        CheckBox checkCompleted;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtDate = itemView.findViewById(R.id.txtDate);
            checkCompleted = itemView.findViewById(R.id.checkCompleted);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onNoteClick(getAdapterPosition());
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) listener.onNoteLongClick(getAdapterPosition());
                return true;
            });
        }
    }
}
