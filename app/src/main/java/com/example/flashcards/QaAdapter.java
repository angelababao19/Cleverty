package com.example.flashcards;

import android.text.*;
import android.view.*;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class QaAdapter extends RecyclerView.Adapter<QaAdapter.VH> {

    static class Row { String q = "", a = ""; }
    private final List<Row> rows = new ArrayList<>();

    QaAdapter(){ rows.add(new Row()); }   // start with one empty row

    static class VH extends RecyclerView.ViewHolder {
        EditText qet, aet;
        VH(@NonNull View itemView) {
            super(itemView);
            qet = itemView.findViewById(R.id.questionEt);
            aet = itemView.findViewById(R.id.answerEt);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vType) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_qa, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Row row = rows.get(pos);
        h.qet.setText(row.q);
        h.aet.setText(row.a);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int start, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                row.q = h.qet.getText().toString().trim();
                row.a = h.aet.getText().toString().trim();
                // if BOTH fields are non-empty AND this is the last row → add new empty row
                if (!row.q.isEmpty() && !row.a.isEmpty() && pos == rows.size()-1) {
                    rows.add(new Row());
                    notifyItemInserted(rows.size()-1);
                }
            }
        };
        h.qet.addTextChangedListener(watcher);
        h.aet.addTextChangedListener(watcher);
    }

    @Override public int getItemCount() { return rows.size(); }

    /* return only COMPLETE rows (ignore last empty one if user left it blank) */
    List<Row> getCompletedCards(){
        List<Row> out = new ArrayList<>();
        for (Row r : rows) if (!r.q.isEmpty() && !r.a.isEmpty()) out.add(r);
        return out;
    }
}