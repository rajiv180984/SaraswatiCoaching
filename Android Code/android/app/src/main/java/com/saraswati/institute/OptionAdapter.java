package com.saraswati.institute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/** MCQ option row — radio + label + select state. */
public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.VH> {

    public interface OnPick { void onPick(int index); }

    private List<String> items;
    private int selected;
    private final OnPick onPick;

    public OptionAdapter(List<String> items, int selected, OnPick onPick) {
        this.items = items; this.selected = selected; this.onPick = onPick;
    }

    public void setItems(List<String> items, int selected) {
        this.items = items; this.selected = selected; notifyDataSetChanged();
    }
    public void setSelected(int selected) {
        int prev = this.selected; this.selected = selected;
        if (prev >= 0) notifyItemChanged(prev);
        if (selected >= 0) notifyItemChanged(selected);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.label.setText(items.get(position));
        h.letter.setText(String.valueOf((char) ('A' + position)));
        boolean isSel = position == selected;
        h.itemView.setSelected(isSel);
        h.radio.setSelected(isSel);
        h.itemView.setOnClickListener(v -> onPick.onPick(position));
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView label, letter;
        View radio;
        VH(View v) {
            super(v);
            label  = v.findViewById(R.id.option_label);
            letter = v.findViewById(R.id.option_letter);
            radio  = v.findViewById(R.id.option_radio);
        }
    }
}
