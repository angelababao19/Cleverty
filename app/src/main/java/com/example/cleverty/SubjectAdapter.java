package com.example.cleverty;

import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cleverty.model.*;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.Holder> {

    public interface OnSubjectClick { void clicked(Subject s); }
    private final List<Subject> list;
    private final OnSubjectClick listener;

    SubjectAdapter(List<Subject> list, OnSubjectClick l){
        this.list = list;
        this.listener = l;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name;
        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.subjectName);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_subject, p, false);
        return new Holder(v);
    }

    /* helper: true if subject is at the top (pinned) */
    private boolean isPinned(Subject s){
        return Library.getInstance().getSubjects().indexOf(s) == 0;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        Subject s = list.get(i);
        h.name.setText(s.getTitle());

        /* normal tap - open flash-cards */
        h.itemView.setOnClickListener(v -> listener.clicked(s));

        /* long-press - Pin / Unpin / Edit / Delete */
        h.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            boolean pinned = isPinned(s);
            popup.getMenu().add(pinned ? "Unpin" : "Pin");   // 1st
            popup.getMenu().add("Edit");                      // 2nd
            popup.getMenu().add("Delete");                    // 3rd
            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                switch (title) {
                    case "Pin":
                        Library.getInstance().pinSubject(s);
                        notifyDataSetChanged();
                        return true;
                    case "Unpin":
                        Library.getInstance().getSubjects().remove(s);
                        Library.getInstance().getSubjects().add(s);
                        notifyDataSetChanged();
                        return true;
                    case "Edit":
                        Intent intent = new Intent(v.getContext(), AddCardActivity.class);
                        intent.putExtra("SUBJECT_POSITION", Library.getInstance().getSubjects().indexOf(s));
                        v.getContext().startActivity(intent);
                        return true;
                    case "Delete":
                        Library.getInstance().getSubjects().remove(s);
                        notifyDataSetChanged();
                        ((Homepage)v.getContext()).refreshEmptyState();
                        return true;
                }
                return false;
            });
            popup.show();
            return true;
        });
    }

    @Override public int getItemCount() { return list.size(); }
}