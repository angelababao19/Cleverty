package com.example.cleverty;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cleverty.model.Subject;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.Holder> {

    // New Interface to pass clicks back to the Homepage
    public interface OnSubjectInteractionListener {
        void onSubjectClicked(Subject s);
        void onPinClicked(Subject s);
        void onUnpinClicked(Subject s);
        void onEditClicked(Subject s);
        void onDeleteClicked(Subject s);
    }

    private final List<Subject> list;
    private final OnSubjectInteractionListener listener;

    SubjectAdapter(List<Subject> list, OnSubjectInteractionListener l) {
        this.list = list;
        this.listener = l;
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView pinIcon;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.subjectName);
            pinIcon = itemView.findViewById(R.id.pinIcon);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_subject, p, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int i) {
        final Subject s = list.get(i);
        h.name.setText(s.getTitle());

        // Show or hide the pin icon
        h.pinIcon.setVisibility(s.isPinned() ? View.VISIBLE : View.GONE);

        // Normal tap - open flash-cards
        h.itemView.setOnClickListener(v -> listener.onSubjectClicked(s));

        // Long-press - Show the popup menu
        h.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(s.isPinned() ? "Unpin" : "Pin");
            popup.getMenu().add("Edit");
            popup.getMenu().add("Delete");

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "Pin":
                        listener.onPinClicked(s);
                        return true;
                    case "Unpin":
                        listener.onUnpinClicked(s);
                        return true;
                    case "Edit":
                        listener.onEditClicked(s);
                        return true;
                    case "Delete":
                        listener.onDeleteClicked(s);
                        return true;
                }
                return false;
            });

            popup.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
