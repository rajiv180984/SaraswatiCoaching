package com.saraswati.institute;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Stub for the bottom-nav tabs that aren't built yet. */
public class PlaceholderFragment extends Fragment {

    private final String label;

    public PlaceholderFragment() { this("Coming soon"); }
    public PlaceholderFragment(String label) { this.label = label; }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        FrameLayout f = new FrameLayout(requireContext());
        f.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        f.setBackgroundResource(R.color.surface_minimal);

        TextView tv = new TextView(requireContext());
        tv.setText(label + "\n— see README for build notes —");
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(16f);
        tv.setTextColor(getResources().getColor(R.color.text_secondary, null));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        f.addView(tv, lp);
        return f;
    }
}
