package com.saraswati.institute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/** Vertical list of upcoming tests. */
public class TestAdapter extends RecyclerView.Adapter<TestAdapter.VH> {

    public static class Test {
        public final String title, when, count;
        public Test(String title, String when, String count) { this.title = title; this.when = when; this.count = count; }
    }

    private final List<Test> items;
    private final View.OnClickListener onClick;

    public TestAdapter(List<Test> items, View.OnClickListener onClick) {
        this.items = items; this.onClick = onClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Test t = items.get(position);
        h.title.setText(t.title);
        h.when.setText(t.when);
        h.count.setText(t.count);
        h.itemView.setOnClickListener(onClick);
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, when, count;
        VH(View v) {
            super(v);
            title = v.findViewById(R.id.test_title);
            when  = v.findViewById(R.id.test_when);
            count = v.findViewById(R.id.test_count);
        }
    }
}
