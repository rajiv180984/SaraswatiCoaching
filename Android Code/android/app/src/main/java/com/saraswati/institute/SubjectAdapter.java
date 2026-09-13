package com.saraswati.institute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/** 2-col grid of subject tiles on the home screen. */
public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.VH> {

    public static class Subject {
        public final String name, meta;
        public final int colorRes;
        public Subject(String name, String meta, int colorRes) { this.name = name; this.meta = meta; this.colorRes = colorRes; }
    }

    private final List<Subject> items;

    public SubjectAdapter(List<Subject> items) { this.items = items; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subject, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Subject s = items.get(position);
        h.name.setText(s.name);
        h.meta.setText(s.meta);
        h.dot.setBackgroundTintList(h.itemView.getContext().getColorStateList(s.colorRes));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, meta;
        View dot;
        VH(View v) {
            super(v);
            name = v.findViewById(R.id.subject_name);
            meta = v.findViewById(R.id.subject_meta);
            dot  = v.findViewById(R.id.subject_dot);
        }
    }
}
