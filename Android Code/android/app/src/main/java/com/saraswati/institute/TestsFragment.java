package com.saraswati.institute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests tab — shows Online and Offline tests grouped by subject,
 * with score and percentile for completed tests.
 */
public class TestsFragment extends Fragment {

    // ── Subject config ─────────────────────────────────────────────────────────
    private static final String[] SUBJECTS = {
        "All", "Mathematics", "Science", "English", "Social", "Hindi", "Sanskrit"
    };

    // ── State ──────────────────────────────────────────────────────────────────
    private boolean showOnline    = true;
    private String  activeSubject = "All";

    private TestListAdapter adapter;
    private final List<TestItem> allItems = new ArrayList<>();

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        buildDemoData();

        // RecyclerView
        RecyclerView rv = root.findViewById(R.id.tests_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(false);
        adapter = new TestListAdapter(buildFilteredList(), item -> {
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).openTestAttempt();
            }
        });
        rv.setAdapter(adapter);

        // Online / Offline toggle
        MaterialButtonToggleGroup toggle = root.findViewById(R.id.toggle_mode);
        toggle.check(R.id.btn_online);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showOnline = (checkedId == R.id.btn_online);
                refresh();
            }
        });

        // Subject chips (added programmatically so we can re-use color resources)
        ChipGroup chipGroup = root.findViewById(R.id.chip_group_subject);
        for (int i = 0; i < SUBJECTS.length; i++) {
            final String subject = SUBJECTS[i];
            Chip chip = new Chip(requireContext());
            chip.setText(subject);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            chip.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    activeSubject = subject;
                    refresh();
                }
            });
            chipGroup.addView(chip);
        }
    }

    // ── Filtering & grouping ───────────────────────────────────────────────────

    private void refresh() {
        adapter.setItems(buildFilteredList());
    }

    /**
     * Build the display list: for each subject (or the selected one),
     * emit a section header followed by matching test items.
     */
    private List<TestItem> buildFilteredList() {
        String[] subjects = { "Mathematics", "Science", "English", "Social", "Hindi", "Sanskrit" };

        List<TestItem> result = new ArrayList<>();
        for (String subject : subjects) {
            if (!"All".equals(activeSubject) && !activeSubject.equals(subject)) continue;

            List<TestItem> group = new ArrayList<>();
            for (TestItem t : allItems) {
                if (t.type == TestItem.TYPE_TEST
                        && t.subject.equals(subject)
                        && t.isOnline == showOnline) {
                    group.add(t);
                }
            }
            if (!group.isEmpty()) {
                result.add(new TestItem(subject));   // section header
                result.addAll(group);
            }
        }
        return result;
    }

    // ── Demo data ──────────────────────────────────────────────────────────────

    private void buildDemoData() {
        allItems.clear();

        // ┌─────────────── ONLINE TESTS ───────────────┐

        // Mathematics — online
        allItems.add(upcoming("o1", "Mathematics Chapter 6 Mock Test",
            "Mathematics", R.color.subject_math, "28 May · 30 Q · 60 min", true));
        allItems.add(completed("o2", "Algebra & Equations Unit Test",
            "Mathematics", R.color.subject_math, "20 May · 25 Q · 45 min", true,
            88, 100, 91.5f, 6, 248));
        allItems.add(completed("o3", "Geometry — Triangles Full Test",
            "Mathematics", R.color.subject_math, "12 May · 40 Q · 90 min", true,
            102, 160, 76.3f, 22, 248));

        // Science — online
        allItems.add(upcoming("o4", "Science Half-Yearly Mock Test",
            "Science", R.color.subject_science, "30 May · 50 Q · 90 min", true));
        allItems.add(completed("o5", "Cell Structure & Division",
            "Science", R.color.subject_science, "18 May · 20 Q · 30 min", true,
            56, 80, 83.4f, 14, 215));

        // English — online
        allItems.add(upcoming("o6", "English Grammar Chapter Test",
            "English", R.color.subject_english, "25 May · 35 Q · 60 min", true));
        allItems.add(completed("o7", "Reading Comprehension Practice",
            "English", R.color.subject_english, "10 May · 15 Q · 20 min", true,
            42, 60, 88.2f, 9, 195));

        // Social — online
        allItems.add(upcoming("o8", "History — Ancient India Test",
            "Social", R.color.subject_social, "22 May · 30 Q · 45 min", true));
        allItems.add(completed("o9", "Geography & Climate Unit Test",
            "Social", R.color.subject_social, "5 May · 25 Q · 40 min", true,
            76, 100, 79.1f, 18, 185));

        // Hindi — online
        allItems.add(upcoming("o10", "Hindi Vyakaran Mock Test",
            "Hindi", R.color.subject_hindi, "27 May · 20 Q · 30 min", true));
        allItems.add(completed("o11", "Hindi Sahitya — Kavita Test",
            "Hindi", R.color.subject_hindi, "8 May · 15 Q · 25 min", true,
            48, 60, 85.7f, 11, 172));

        // Sanskrit — online
        allItems.add(upcoming("o12", "Sanskrit Grammar Basics",
            "Sanskrit", R.color.subject_sanskrit, "29 May · 20 Q · 30 min", true));
        allItems.add(completed("o13", "Sandhi & Samas Practice",
            "Sanskrit", R.color.subject_sanskrit, "2 May · 15 Q · 20 min", true,
            38, 60, 72.8f, 25, 140));

        // ┌─────────────── OFFLINE TESTS ──────────────┐

        // Mathematics — offline
        allItems.add(upcoming("p1", "Mathematics Chapter 5 Offline Test",
            "Mathematics", R.color.subject_math, "26 May · 30 Q · 60 min", false));
        allItems.add(completed("p2", "Number System Practice Paper",
            "Mathematics", R.color.subject_math, "14 May · 25 Q · 45 min", false,
            78, 100, 82.6f, 16, 220));

        // Science — offline
        allItems.add(upcoming("p3", "Physics — Motion & Force Paper",
            "Science", R.color.subject_science, "24 May · 40 Q · 75 min", false));
        allItems.add(completed("p4", "Chemical Reactions Offline Test",
            "Science", R.color.subject_science, "9 May · 30 Q · 50 min", false,
            96, 120, 89.0f, 8, 210));

        // English — offline
        allItems.add(upcoming("p5", "Creative Writing & Essays Paper",
            "English", R.color.subject_english, "21 May · 20 Q · 40 min", false));
        allItems.add(completed("p6", "Prose & Poetry Analysis",
            "English", R.color.subject_english, "3 May · 20 Q · 35 min", false,
            52, 80, 74.5f, 24, 190));

        // Social — offline
        allItems.add(upcoming("p7", "Civics & Constitution Practice",
            "Social", R.color.subject_social, "16 May · 25 Q · 40 min", false));
        allItems.add(completed("p8", "Economics Basics Paper",
            "Social", R.color.subject_social, "1 May · 20 Q · 30 min", false,
            65, 80, 71.5f, 28, 180));

        // Hindi — offline
        allItems.add(upcoming("p9", "Hindi Nibandh Practice Paper",
            "Hindi", R.color.subject_hindi, "19 May · 15 Q · 25 min", false));
        allItems.add(completed("p10", "Hindi Kavita — Bhav Spasht",
            "Hindi", R.color.subject_hindi, "6 May · 10 Q · 20 min", false,
            38, 60, 68.3f, 32, 165));

        // Sanskrit — offline
        allItems.add(upcoming("p11", "Sanskrit Shloka & Translation",
            "Sanskrit", R.color.subject_sanskrit, "11 May · 20 Q · 35 min", false));
        allItems.add(completed("p12", "Sanskrit Dhatu Roop Paper",
            "Sanskrit", R.color.subject_sanskrit, "4 May · 15 Q · 25 min", false,
            52, 60, 77.4f, 19, 155));
    }

    // Helper: upcoming test
    private TestItem upcoming(String id, String title, String subject, int colorRes,
                              String meta, boolean isOnline) {
        return new TestItem(id, title, subject, colorRes, meta, isOnline);
    }

    // Helper: completed test
    private TestItem completed(String id, String title, String subject, int colorRes,
                               String meta, boolean isOnline,
                               int score, int maxScore, float percentile, int rank, int total) {
        return new TestItem(id, title, subject, colorRes, meta, isOnline,
            score, maxScore, percentile, rank, total);
    }
}
