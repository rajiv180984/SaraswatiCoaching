package com.saraswati.institute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** Question palette — 6-col grid of numbered chips, color-coded by status. */
public class QuestionPaletteAdapter extends RecyclerView.Adapter<QuestionPaletteAdapter.VH> {

    public interface OnJump { void jumpTo(int index); }

    private final int count;
    private final int[] status;
    private int current;
    private final OnJump onJump;

    public QuestionPaletteAdapter(int count, int[] status, int current, OnJump onJump) {
        this.count = count; this.status = status; this.current = current; this.onJump = onJump;
    }

    public void setCurrent(int c) {
        int prev = current; current = c;
        if (prev >= 0) notifyItemChanged(prev);
        if (c >= 0) notifyItemChanged(c);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_palette_chip, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.num.setText(String.valueOf(position + 1));
        int s = status[position];
        int bg, fg;
        switch (s) {
            case 1: bg = R.color.positive;        fg = R.color.white; break;       // answered
            case 2: bg = R.color.negative_light;  fg = R.color.negative; break;    // not answered
            case 3: bg = R.color.saffron;         fg = R.color.white; break;       // marked
            default: bg = R.color.surface_subtle; fg = R.color.text_secondary;     // not visited
        }
        h.itemView.setBackgroundTintList(h.itemView.getContext().getColorStateList(bg));
        h.num.setTextColor(h.itemView.getContext().getColor(fg));
        h.itemView.setOnClickListener(v -> onJump.jumpTo(position));

        // Mark current question with a stronger ring
        h.itemView.setAlpha(position == current ? 1f : 0.92f);
    }

    @Override public int getItemCount() { return count; }

    static class VH extends RecyclerView.ViewHolder {
        TextView num;
        VH(View v) { super(v); num = v.findViewById(R.id.palette_num); }
    }
}
