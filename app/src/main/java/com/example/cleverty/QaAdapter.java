package com.example.cleverty;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class QaAdapter extends RecyclerView.Adapter<QaAdapter.Holder> {

    static class Row {
        String q = "";
        String a = "";
    }

    private final List<Row> rows = new ArrayList<>();
    private boolean isEditMode = false; // Flag to control behavior

    /**
     *  === FIX: THIS IS THE NEW METHOD YOU NEED ===
     *  Adds a new row with pre-filled data. Used when loading an existing subject.
     */
    public void addRow(String q, String a) {
        Row newRow = new Row();
        newRow.q = q;
        newRow.a = a;
        rows.add(newRow);
        // Notify the adapter that an item has been inserted at the end of the list.
        notifyItemInserted(rows.size() - 1);
    }

    /**
     *  === FIX: THIS IS THE SECOND NEW METHOD YOU NEED ===
     *  Sets the adapter to edit mode.
     */
    public void setEditMode(boolean isEditing) {
        this.isEditMode = isEditing;
    }

    // Default constructor
    public QaAdapter() {
        // The logic to add the first blank row is now handled in AddCardActivity
    }

    public List<Row> getCompletedCards() {
        List<Row> out = new ArrayList<>();
        for (Row r : rows) {
            if (r.q != null && !r.q.trim().isEmpty() && r.a != null && !r.a.trim().isEmpty()) {
                out.add(r);
            }
        }
        return out;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qa, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        // This logic remains the same.
        h.question.removeTextChangedListener(h.questionWatcher);
        h.answer.removeTextChangedListener(h.answerWatcher);

        h.questionWatcher.updatePosition(position);
        h.answerWatcher.updatePosition(position);

        Row r = rows.get(position);
        h.question.setText(r.q);
        h.answer.setText(r.a);

        h.question.addTextChangedListener(h.questionWatcher);
        h.answer.addTextChangedListener(h.answerWatcher);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    /* ---------------- ViewHolder ---------------- */
    class Holder extends RecyclerView.ViewHolder {
        EditText question, answer;
        Watcher questionWatcher, answerWatcher;

        Holder(@NonNull View v) {
            super(v);
            // Ensure your item_qa.xml has IDs: questionInput and answerInput
            question = v.findViewById(R.id.questionEt);
            answer = v.findViewById(R.id.answerEt);
            questionWatcher = new Watcher(true);
            answerWatcher = new Watcher(false);
        }
    }

    /* -------------- Reusable TextWatcher --------------- */
    private class Watcher implements TextWatcher {
        private int position;
        private final boolean isQuestion;

        Watcher(boolean isQuestion) {
            this.isQuestion = isQuestion;
        }

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (position >= rows.size()) return;

            if (isQuestion) {
                rows.get(position).q = s.toString();
            } else {
                rows.get(position).a = s.toString();
            }

            boolean isLastRow = (position == rows.size() - 1);
            Row currentRow = rows.get(position);

            // A new row will now only be added if BOTH fields in the last row are non-empty.
            if (isLastRow && !currentRow.q.trim().isEmpty() && !currentRow.a.trim().isEmpty()) {
                rows.add(new Row());
                notifyItemInserted(rows.size());
            }
        }
    }
}
