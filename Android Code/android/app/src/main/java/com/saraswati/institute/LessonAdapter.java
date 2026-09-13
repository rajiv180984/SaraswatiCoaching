package com.saraswati.institute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/** Horizontal "Continue learning" card. */
public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.VH> {

    public static class Lesson {
        public final String title, subject;
        public final float progress;
        public final int colorRes;
        public Lesson(String title, String subject, float progress, int colorRes) {
            this.title = title; this.subject = subject; this.progress = progress; this.colorRes = colorRes;
        }
    }

    private final List<Lesson> items;

    public LessonAdapter(List<Lesson> items) { this.items = items; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lesson, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Lesson l = items.get(position);
        h.title.setText(l.title);
        h.subject.setText(l.subject);
        h.progressBar.setProgress((int) (l.progress * 100));
        h.progressLabel.setText(((int) (l.progress * 100)) + "%");
        h.subject.setTextColor(h.itemView.getContext().getColor(l.colorRes));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subject, progressLabel;
        ProgressBar progressBar;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.lesson_title);
            subject = v.findViewById(R.id.lesson_subject);
            progressBar = v.findViewById(R.id.lesson_progress);
            progressLabel = v.findViewById(R.id.lesson_progress_label);
        }
    }
}
