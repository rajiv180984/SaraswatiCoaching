package com.saraswati.institute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Study Material tab — shows PDFs, videos, question papers, notes and
 * mind maps organised by subject with dual chip filters (type + subject).
 */
public class StudyMaterialFragment extends Fragment {

    // ── Filter options ────────────────────────────────────────────────────────
    private static final String[] TYPE_LABELS = {
        "All", "PDF", "Video", "Q. Paper", "Notes", "Mind Map"
    };
    // maps TYPE_LABELS index → StudyMaterial.KIND_* (−1 = no filter)
    private static final int[] TYPE_KINDS = { -1,
        StudyMaterial.KIND_PDF,
        StudyMaterial.KIND_VIDEO,
        StudyMaterial.KIND_QPAPER,
        StudyMaterial.KIND_NOTES,
        StudyMaterial.KIND_MINDMAP
    };

    private static final String[] SUBJECTS = {
        "All", "Mathematics", "Science", "English", "Social", "Hindi", "Sanskrit"
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private int    activeKind    = -1;       // −1 = show all types
    private String activeSubject = "All";

    private StudyMaterialAdapter adapter;
    private final List<StudyMaterial> allItems = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_study_material, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        buildDemoData();

        // RecyclerView
        RecyclerView rv = root.findViewById(R.id.materials_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setHasFixedSize(false);
        adapter = new StudyMaterialAdapter(buildFilteredList(), new StudyMaterialAdapter.OnMaterialAction() {
            @Override public void onDownload(StudyMaterial item) { handleDownload(item); }
            @Override public void onWatch(StudyMaterial item)    { handleWatch(item); }
        });
        rv.setAdapter(adapter);

        // Type chips
        ChipGroup typeChips = root.findViewById(R.id.chip_type);
        for (int i = 0; i < TYPE_LABELS.length; i++) {
            final int kind = TYPE_KINDS[i];
            Chip chip = new Chip(requireContext());
            chip.setText(TYPE_LABELS[i]);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            chip.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) { activeKind = kind; refresh(); }
            });
            typeChips.addView(chip);
        }

        // Subject chips
        ChipGroup subjectChips = root.findViewById(R.id.chip_subject);
        for (int i = 0; i < SUBJECTS.length; i++) {
            final String subject = SUBJECTS[i];
            Chip chip = new Chip(requireContext());
            chip.setText(subject);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            chip.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) { activeSubject = subject; refresh(); }
            });
            subjectChips.addView(chip);
        }
    }

    // ── Filtering & grouping ──────────────────────────────────────────────────

    private void refresh() {
        adapter.setItems(buildFilteredList());
    }

    private List<StudyMaterial> buildFilteredList() {
        String[] subjects = {
            "Mathematics", "Science", "English", "Social", "Hindi", "Sanskrit"
        };
        List<StudyMaterial> result = new ArrayList<>();
        for (String subject : subjects) {
            if (!"All".equals(activeSubject) && !activeSubject.equals(subject)) continue;

            List<StudyMaterial> group = new ArrayList<>();
            for (StudyMaterial m : allItems) {
                if (m.type != StudyMaterial.TYPE_ITEM) continue;
                if (!m.subject.equals(subject)) continue;
                if (activeKind != -1 && m.kind != activeKind) continue;
                group.add(m);
            }
            if (!group.isEmpty()) {
                result.add(new StudyMaterial(subject));   // section header
                result.addAll(group);
            }
        }
        return result;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void handleDownload(StudyMaterial item) {
        // Mark as downloaded and refresh — in real app: trigger background download
        item.isDownloaded = true;
        Toast.makeText(requireContext(),
            "\"" + item.title + "\" saved for offline use.",
            Toast.LENGTH_SHORT).show();
        refresh();
    }

    private void handleWatch(StudyMaterial item) {
        Toast.makeText(requireContext(),
            "Opening: " + item.title,
            Toast.LENGTH_SHORT).show();
    }

    // ── Demo data ─────────────────────────────────────────────────────────────

    private void buildDemoData() {
        allItems.clear();

        // ───── Mathematics ─────
        allItems.add(pdf("m1", "Algebra & Linear Equations — Complete Notes",
            "Mathematics", R.color.subject_math, "Chapter 3", "2.4 MB"));
        allItems.add(video("m2", "Quadratic Equations — Video Lecture",
            "Mathematics", R.color.subject_math, "Chapter 4", "18:45"));
        allItems.add(notes("m3", "Number System — Quick Revision",
            "Mathematics", R.color.subject_math, "Chapter 1", "1.1 MB"));
        allItems.add(qpaper("m4", "Mathematics — Previous Year 2023",
            "Mathematics", R.color.subject_math, "Annual Exam", "3.8 MB"));
        allItems.add(mindmap("m5", "Coordinate Geometry — Mind Map",
            "Mathematics", R.color.subject_math, "Chapter 7", "0.8 MB"));
        allItems.add(pdf("m6", "Statistics & Probability Notes",
            "Mathematics", R.color.subject_math, "Chapter 14", "1.9 MB"));
        allItems.add(video("m7", "Triangles & Similarity — Video Lecture",
            "Mathematics", R.color.subject_math, "Chapter 6", "22:30"));

        // ───── Science ─────
        allItems.add(pdf("s1", "Cell Biology — Complete Chapter Notes",
            "Science", R.color.subject_science, "Chapter 5", "3.2 MB"));
        allItems.add(video("s2", "Photosynthesis — Process Explained",
            "Science", R.color.subject_science, "Chapter 6", "14:20"));
        allItems.add(pdf("s3", "Motion & Laws of Motion Notes",
            "Science", R.color.subject_science, "Chapter 8", "2.7 MB"));
        allItems.add(qpaper("s4", "Science — Previous Year 2023",
            "Science", R.color.subject_science, "Annual Exam", "4.1 MB"));
        allItems.add(notes("s5", "Chemical Reactions — Revision Notes",
            "Science", R.color.subject_science, "Chapter 1", "1.5 MB"));
        allItems.add(mindmap("s6", "Human Digestive System — Mind Map",
            "Science", R.color.subject_science, "Chapter 7", "1.0 MB"));
        allItems.add(video("s7", "Electricity & Circuits — Video Lecture",
            "Science", R.color.subject_science, "Chapter 12", "19:55"));

        // ───── English ─────
        allItems.add(pdf("e1", "The Road Not Taken — Detailed Analysis",
            "English", R.color.subject_english, "Beehive Ch. 1", "0.9 MB"));
        allItems.add(video("e2", "Grammar Masterclass — Tenses & Voice",
            "English", R.color.subject_english, "Grammar", "22:10"));
        allItems.add(pdf("e3", "Writing Skills — Letter & Essay Guide",
            "English", R.color.subject_english, "Writing", "2.1 MB"));
        allItems.add(qpaper("e4", "English — Previous Year 2023",
            "English", R.color.subject_english, "Annual Exam", "2.9 MB"));
        allItems.add(notes("e5", "Comprehension Passages — 50 Practice Sets",
            "English", R.color.subject_english, "Reading", "1.8 MB"));
        allItems.add(mindmap("e6", "Parts of Speech — Mind Map",
            "English", R.color.subject_english, "Grammar", "0.6 MB"));

        // ───── Social Studies ─────
        allItems.add(pdf("ss1", "Ancient Indian History — Chapter Notes",
            "Social", R.color.subject_social, "History Ch. 1–4", "4.3 MB"));
        allItems.add(video("ss2", "French Revolution — Video Lecture",
            "Social", R.color.subject_social, "History Ch. 1", "16:30"));
        allItems.add(pdf("ss3", "Geography — Climate & Natural Vegetation",
            "Social", R.color.subject_social, "Geography Ch. 4", "3.5 MB"));
        allItems.add(qpaper("ss4", "Social Science — Previous Year 2023",
            "Social", R.color.subject_social, "Annual Exam", "3.2 MB"));
        allItems.add(notes("ss5", "Civics — Constitution & Democracy Notes",
            "Social", R.color.subject_social, "Civics Ch. 1–3", "2.0 MB"));
        allItems.add(mindmap("ss6", "Sectors of Economy — Mind Map",
            "Social", R.color.subject_social, "Economics Ch. 2", "0.7 MB"));

        // ───── Hindi ─────
        allItems.add(pdf("h1", "Vyakaran — Sandhi, Samas & Alankar",
            "Hindi", R.color.subject_hindi, "Vyakaran", "2.0 MB"));
        allItems.add(video("h2", "Kshitij — Kabir & Meera Pad Analysis",
            "Hindi", R.color.subject_hindi, "Kshitij Ch. 1–2", "13:40"));
        allItems.add(notes("h3", "Nibandh Lekhan — Top 20 Topics",
            "Hindi", R.color.subject_hindi, "Writing", "1.4 MB"));
        allItems.add(qpaper("h4", "Hindi — Previous Year 2023",
            "Hindi", R.color.subject_hindi, "Annual Exam", "2.6 MB"));
        allItems.add(pdf("h5", "Sparsh — Prosody & Comprehension Notes",
            "Hindi", R.color.subject_hindi, "Sparsh Ch. 1–5", "1.7 MB"));

        // ───── Sanskrit ─────
        allItems.add(pdf("sk1", "Sandhi & Samas — Complete Notes",
            "Sanskrit", R.color.subject_sanskrit, "Vyakaran", "1.8 MB"));
        allItems.add(pdf("sk2", "Dhatu Roop Practice Sheet",
            "Sanskrit", R.color.subject_sanskrit, "Vyakaran", "0.7 MB"));
        allItems.add(video("sk3", "Sanskrit Vyakaran — Video Lecture",
            "Sanskrit", R.color.subject_sanskrit, "Vyakaran", "11:15"));
        allItems.add(qpaper("sk4", "Sanskrit — Previous Year 2023",
            "Sanskrit", R.color.subject_sanskrit, "Annual Exam", "2.2 MB"));
        allItems.add(notes("sk5", "Shloka Meanings — Revision Notes",
            "Sanskrit", R.color.subject_sanskrit, "Shemushi Ch. 1–4", "1.2 MB"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private StudyMaterial pdf(String id, String title, String subject,
                              int color, String chapter, String size) {
        return new StudyMaterial(id, title, subject, color,
            StudyMaterial.KIND_PDF, chapter, size);
    }

    private StudyMaterial video(String id, String title, String subject,
                                int color, String chapter, String duration) {
        return new StudyMaterial(id, title, subject, color,
            StudyMaterial.KIND_VIDEO, chapter, duration);
    }

    private StudyMaterial notes(String id, String title, String subject,
                                int color, String chapter, String size) {
        return new StudyMaterial(id, title, subject, color,
            StudyMaterial.KIND_NOTES, chapter, size);
    }

    private StudyMaterial qpaper(String id, String title, String subject,
                                 int color, String chapter, String size) {
        return new StudyMaterial(id, title, subject, color,
            StudyMaterial.KIND_QPAPER, chapter, size);
    }

    private StudyMaterial mindmap(String id, String title, String subject,
                                  int color, String chapter, String size) {
        return new StudyMaterial(id, title, subject, color,
            StudyMaterial.KIND_MINDMAP, chapter, size);
    }
}
