package com.saraswati.institute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Variant A — "Card-heavy" home dashboard.
 *
 * Sections (top → bottom):
 *   1. Greeting + streak
 *   2. Hero "Live now" saffron card
 *   3. Continue learning (horizontal RecyclerView)
 *   4. My subjects (2-col grid)
 *   5. Upcoming tests (vertical list)
 */
public class HomeFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);

        // Greeting
        TextView greet = view.findViewById(R.id.greeting);
        TextView meta  = view.findViewById(R.id.greeting_meta);
        greet.setText(getString(R.string.home_greeting, getString(R.string.demo_student_name)));
        meta.setText(getString(R.string.home_class, getString(R.string.demo_class), getString(R.string.demo_section))
            + "  •  " + getString(R.string.home_streak, 12));

        // Live now card → tap opens live class (placeholder → Test attempt for demo)
        view.findViewById(R.id.live_card).setOnClickListener(v -> {
            if (getActivity() instanceof HomeActivity) ((HomeActivity) getActivity()).openTestAttempt();
        });

        // Continue learning — horizontal
        RecyclerView continueRv = view.findViewById(R.id.continue_rv);
        continueRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        continueRv.setAdapter(new LessonAdapter(buildContinueData()));

        // Subjects — 2-col grid
        RecyclerView subjectsRv = view.findViewById(R.id.subjects_rv);
        subjectsRv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        subjectsRv.setAdapter(new SubjectAdapter(buildSubjectData()));

        // Upcoming tests — vertical list
        RecyclerView testsRv = view.findViewById(R.id.tests_rv);
        testsRv.setLayoutManager(new LinearLayoutManager(getContext()));
        testsRv.setAdapter(new TestAdapter(buildTestData(), v -> {
            if (getActivity() instanceof HomeActivity) ((HomeActivity) getActivity()).openTestAttempt();
        }));
    }

    // ─────────── Mock data (replace with API) ───────────

    private List<LessonAdapter.Lesson> buildContinueData() {
        return new ArrayList<>(Arrays.asList(
            new LessonAdapter.Lesson("Trigonometry — Ratios", "Mathematics", 0.65f, R.color.subject_math),
            new LessonAdapter.Lesson("Cell structure", "Science", 0.30f, R.color.subject_science),
            new LessonAdapter.Lesson("The Road Not Taken", "English", 0.85f, R.color.subject_english)
        ));
    }

    private List<SubjectAdapter.Subject> buildSubjectData() {
        return new ArrayList<>(Arrays.asList(
            new SubjectAdapter.Subject("Mathematics", "12 chapters", R.color.subject_math),
            new SubjectAdapter.Subject("Science", "10 chapters", R.color.subject_science),
            new SubjectAdapter.Subject("English", "8 chapters", R.color.subject_english),
            new SubjectAdapter.Subject("Social Science", "14 chapters", R.color.subject_social),
            new SubjectAdapter.Subject("Hindi", "9 chapters", R.color.subject_hindi),
            new SubjectAdapter.Subject("Sanskrit", "7 chapters", R.color.subject_sanskrit)
        ));
    }

    private List<TestAdapter.Test> buildTestData() {
        return new ArrayList<>(Arrays.asList(
            new TestAdapter.Test("Mathematics — Chapter 6 Test", "Tomorrow, 10:00 AM", "30 questions"),
            new TestAdapter.Test("Science — Half-yearly Mock", "Fri, 14 May", "60 questions"),
            new TestAdapter.Test("English — Grammar Drill", "Mon, 17 May", "20 questions")
        ));
    }
}
