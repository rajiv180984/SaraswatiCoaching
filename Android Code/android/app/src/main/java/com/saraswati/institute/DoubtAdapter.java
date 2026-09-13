package com.saraswati.institute;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/** Displays the Q&A doubt list. */
public class DoubtAdapter extends RecyclerView.Adapter<DoubtAdapter.VH> {

    private final List<DoubtItem> items;

    public DoubtAdapter(List<DoubtItem> items) {
        this.items = items;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_doubt_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(items.get(position));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final View     subjectDot;
        final TextView subjectLabel, statusBadge, questionText, doubtMeta;
        final View     sectionAnswered;
        final TextView answeredBy, answerText, sectionPending;

        VH(View v) {
            super(v);
            subjectDot      = v.findViewById(R.id.subject_dot);
            subjectLabel    = v.findViewById(R.id.subject_label);
            statusBadge     = v.findViewById(R.id.status_badge);
            questionText    = v.findViewById(R.id.question_text);
            doubtMeta       = v.findViewById(R.id.doubt_meta);
            sectionAnswered = v.findViewById(R.id.section_answered);
            answeredBy      = v.findViewById(R.id.answered_by);
            answerText      = v.findViewById(R.id.answer_text);
            sectionPending  = v.findViewById(R.id.section_pending);
        }

        void bind(DoubtItem d) {
            android.content.Context ctx = itemView.getContext();

            // Subject dot colour
            subjectDot.setBackgroundTintList(
                ColorStateList.valueOf(ctx.getColor(d.subjectColor)));
            subjectLabel.setText(d.subject);
            questionText.setText(d.question);

            // Meta line
            String meta = d.chapter.isEmpty()
                ? d.postedAt
                : d.chapter + "  ·  " + d.postedAt;
            doubtMeta.setText(meta);

            if (d.isAnswered) {
                statusBadge.setText(ctx.getString(R.string.doubt_status_answered));
                statusBadge.setTextColor(ctx.getColor(R.color.positive));
                statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ctx.getColor(R.color.positive_light)));

                sectionAnswered.setVisibility(View.VISIBLE);
                sectionPending.setVisibility(View.GONE);
                answeredBy.setText(ctx.getString(R.string.doubt_answered_by, d.answeredBy));
                answerText.setText(d.answerText);
            } else {
                statusBadge.setText(ctx.getString(R.string.doubt_status_pending));
                statusBadge.setTextColor(ctx.getColor(R.color.saffron_dark));
                statusBadge.setBackgroundTintList(
                    ColorStateList.valueOf(ctx.getColor(R.color.saffron_light)));

                sectionAnswered.setVisibility(View.GONE);
                sectionPending.setVisibility(View.VISIBLE);
            }
        }
    }
}
