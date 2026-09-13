package com.saraswati.institute;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Doubts tab — two modes:
 *   Q&A        : post written doubts; teacher auto-answers after a delay
 *   Live Query : real-time chat with Saraswati AI assistant
 */
public class DoubtsFragment extends Fragment {

    // ── Subject config ─────────────────────────────────────────────────────────
    private static final String[] DIALOG_SUBJECTS = {
        "Select Subject", "Mathematics", "Science", "English",
        "Social Studies", "Hindi", "Sanskrit"
    };
    private static final int[] SUBJECT_COLORS = {
        0,
        R.color.subject_math,    R.color.subject_science, R.color.subject_english,
        R.color.subject_social,  R.color.subject_hindi,   R.color.subject_sanskrit
    };

    // ── State ──────────────────────────────────────────────────────────────────
    private final List<DoubtItem>   doubts   = new ArrayList<>();
    private final List<ChatMessage> messages = new ArrayList<>();
    private final Handler           handler  = new Handler(Looper.getMainLooper());
    private int doubtIdCounter = 100;

    // ── Views ──────────────────────────────────────────────────────────────────
    private View          qaSection, liveSection, emptyState;
    private RecyclerView  doubtsRv, chatRv;
    private DoubtAdapter  doubtAdapter;
    private ChatAdapter   chatAdapter;

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup container,
                             @Nullable Bundle state) {
        return inf.inflate(R.layout.fragment_doubts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle state) {
        qaSection   = root.findViewById(R.id.qa_section);
        liveSection = root.findViewById(R.id.live_section);
        emptyState  = root.findViewById(R.id.empty_state);

        setupToggle(root);
        setupQA(root);
        setupLiveQuery(root);

        // Default: Q&A tab
        MaterialButtonToggleGroup toggle = root.findViewById(R.id.toggle_mode);
        toggle.check(R.id.btn_qa);
    }

    // ── Toggle ────────────────────────────────────────────────────────────────

    private void setupToggle(View root) {
        MaterialButtonToggleGroup toggle = root.findViewById(R.id.toggle_mode);
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_qa) {
                qaSection.setVisibility(View.VISIBLE);
                liveSection.setVisibility(View.GONE);
            } else {
                qaSection.setVisibility(View.GONE);
                liveSection.setVisibility(View.VISIBLE);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Q&A section
    // ══════════════════════════════════════════════════════════════════════════

    private void setupQA(View root) {
        doubtsRv = root.findViewById(R.id.doubts_rv);
        doubtsRv.setLayoutManager(new LinearLayoutManager(requireContext()));

        buildInitialDoubts();
        doubtAdapter = new DoubtAdapter(doubts);
        doubtsRv.setAdapter(doubtAdapter);
        updateEmptyState();

        FloatingActionButton fab = root.findViewById(R.id.fab_post);
        fab.setOnClickListener(v -> showPostDoubtDialog());
    }

    private void updateEmptyState() {
        if (doubts.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            doubtsRv.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            doubtsRv.setVisibility(View.VISIBLE);
        }
    }

    private void showPostDoubtDialog() {
        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_post_doubt, null);

        Spinner spinner = dialogView.findViewById(R.id.subject_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, DIALOG_SUBJECTS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        TextInputEditText chapterInput  = dialogView.findViewById(R.id.chapter_input);
        TextInputEditText questionInput = dialogView.findViewById(R.id.question_input);

        new MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.doubt_post_btn, (dlg, which) -> {
                int subjectIdx = spinner.getSelectedItemPosition();
                if (subjectIdx == 0) {
                    Toast.makeText(requireContext(),
                        R.string.doubt_select_subject_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                String question = questionInput.getText() != null
                    ? questionInput.getText().toString().trim() : "";
                if (question.isEmpty()) {
                    Toast.makeText(requireContext(),
                        R.string.doubt_empty_question_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                String chapter = chapterInput.getText() != null
                    ? chapterInput.getText().toString().trim() : "";
                postDoubt(DIALOG_SUBJECTS[subjectIdx], SUBJECT_COLORS[subjectIdx],
                    question, chapter);
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void postDoubt(String subject, int colorRes, String question, String chapter) {
        String id = "d" + (doubtIdCounter++);
        DoubtItem doubt = new DoubtItem(id, subject, colorRes, question, chapter, "Just now");
        doubts.add(0, doubt);
        doubtAdapter.notifyItemInserted(0);
        doubtsRv.scrollToPosition(0);
        updateEmptyState();

        Toast.makeText(requireContext(), R.string.doubt_posted_toast, Toast.LENGTH_SHORT).show();

        // Simulate teacher answering after 5 seconds
        int position = 0;
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            doubt.isAnswered = true;
            doubt.answerText = generateTeacherAnswer(subject, question);
            doubt.answeredBy = pickTeacher(subject);
            doubt.postedAt   = "Just now";
            int pos = doubts.indexOf(doubt);
            if (pos >= 0) doubtAdapter.notifyItemChanged(pos);
        }, 5_000);
    }

    private String generateTeacherAnswer(String subject, String question) {
        String q = question.toLowerCase();
        if (subject.equals("Mathematics")) {
            if (q.contains("quadratic") || q.contains("equation"))
                return "To solve a quadratic equation ax² + bx + c = 0, use the quadratic formula: x = (−b ± √(b²−4ac)) / 2a. First compute the discriminant D = b²−4ac. If D > 0 → two real roots; D = 0 → one root; D < 0 → no real roots. Try substituting the values and simplify step by step.";
            if (q.contains("triangle") || q.contains("geometry") || q.contains("angle"))
                return "Remember the key theorems: (1) Sum of angles in a triangle = 180°. (2) Pythagoras theorem: a² + b² = c² for right triangles. (3) For similarity, check AA, SAS, or SSS criteria. Draw a diagram first — it always helps!";
            return "Great question! Start by identifying what type of problem it is. Write down the given information and what you need to find. Apply the relevant formula from the chapter and solve step by step. Check your answer by substituting back.";
        }
        if (subject.equals("Science")) {
            if (q.contains("cell") || q.contains("biology") || q.contains("tissue"))
                return "Cells are the basic unit of life. Plant cells have a cell wall, chloroplasts, and a large vacuole, while animal cells don't. For tissues: (1) Epithelial — protective covering, (2) Connective — support/binding, (3) Muscular — movement, (4) Nervous — signal transmission. Revise Chapter 5 diagrams carefully!";
            if (q.contains("force") || q.contains("motion") || q.contains("newton"))
                return "Newton's 3 Laws: (1) An object stays at rest/motion unless acted upon by a net force. (2) F = ma (Force = mass × acceleration). (3) Every action has an equal and opposite reaction. For numerical problems, always write the known values, unknown, and the formula before solving.";
            return "Refer to the relevant chapter in NCERT. Draw diagrams wherever possible — especially for Biology and Physics. Note down all definitions and formulas on a separate page for quick revision.";
        }
        if (subject.equals("English")) {
            if (q.contains("grammar") || q.contains("tense") || q.contains("voice"))
                return "For Active/Passive voice: Active → Subject + Verb + Object (e.g., 'Ram ate the apple'). Passive → Object + be + past participle + by + Subject (e.g., 'The apple was eaten by Ram'). For tense conversion, ensure subject-verb agreement is maintained. Practice with 10 sentences daily for mastery.";
            return "For literature questions, always mention: (1) The title and author, (2) The central theme, (3) Specific lines/quotes from the text, (4) Your interpretation. For writing tasks, plan your content in 3 parts — introduction, body, conclusion — before you start writing.";
        }
        return "This is a good question! Please refer to your NCERT textbook — the answer is explained clearly in the relevant chapter. Also check your class notes and previous board question papers for similar questions. Feel free to ask if you need more clarification!";
    }

    private String pickTeacher(String subject) {
        switch (subject) {
            case "Mathematics":   return "Mr. Kumar";
            case "Science":       return "Ms. Sharma";
            case "English":       return "Ms. Verma";
            case "Social Studies":return "Mr. Singh";
            case "Hindi":         return "Mrs. Gupta";
            case "Sanskrit":      return "Mr. Mishra";
            default:              return "Your Teacher";
        }
    }

    // ── Initial demo doubts ───────────────────────────────────────────────────

    private void buildInitialDoubts() {
        doubts.add(new DoubtItem("d1",
            "Mathematics", R.color.subject_math,
            "How do I solve a quadratic equation using the discriminant method?",
            "Chapter 4", "2 hours ago",
            "Use the quadratic formula x = (−b ± √(b²−4ac)) / 2a. Compute D = b²−4ac first. D > 0 gives two real roots, D = 0 gives one root, and D < 0 means no real roots. Substitute your values carefully and simplify.",
            "Mr. Kumar"));

        doubts.add(new DoubtItem("d2",
            "Science", R.color.subject_science,
            "What is the difference between a cell and a tissue?",
            "Chapter 5", "Yesterday",
            "A cell is the smallest structural and functional unit of life. A tissue is a group of similar cells performing a specific function. Example: muscle cells form muscular tissue. In plants: meristematic, permanent (simple & complex), and secretory tissues. In animals: epithelial, connective, muscular, and nervous tissues.",
            "Ms. Sharma"));

        doubts.add(new DoubtItem("d3",
            "English", R.color.subject_english,
            "How should I structure an essay for the board exam?",
            "Writing Skills", "2 days ago",
            "A good essay has 3 parts: (1) Introduction — state your main idea/thesis clearly in 2-3 sentences. (2) Body — 2-3 paragraphs each covering one key point with supporting evidence or examples. (3) Conclusion — summarise your points and end with a strong closing statement. Always plan for 5 min before you write!",
            "Ms. Verma"));

        doubts.add(new DoubtItem("d4",
            "Social Studies", R.color.subject_social,
            "Why did the French Revolution happen and what were its effects?",
            "History Ch. 1", "3 days ago"));

        doubts.add(new DoubtItem("d5",
            "Hindi", R.color.subject_hindi,
            "Sandhi aur Samas mein kya antar hai? Udaharan ke saath samjhaiye.",
            "Vyakaran", "4 days ago",
            "Sandhi: Do shabdon ke milne par dhwaniyon mein parivartan hota hai. Jaise: Dev + Aalaya = Devaalaya (swar sandhi). Samas: Do ya adhik shabdon ka apna vibhakti chhodkar milna. Jaise: Neela + Aakash = Neelaakash (Karmadharaya Samas). Sandhi todna = vibhakti + alaga shabd; Samas todna = vigrah karna.",
            "Mrs. Gupta"));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Live Query section
    // ══════════════════════════════════════════════════════════════════════════

    private void setupLiveQuery(View root) {
        chatRv = root.findViewById(R.id.chat_rv);
        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setStackFromEnd(true);
        chatRv.setLayoutManager(llm);
        chatAdapter = new ChatAdapter(messages);
        chatRv.setAdapter(chatAdapter);

        initWelcomeMessages();

        EditText input = root.findViewById(R.id.chat_input);
        View sendBtn   = root.findViewById(R.id.send_btn);

        sendBtn.setOnClickListener(v -> sendUserMessage(input));
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendUserMessage(input);
                return true;
            }
            return false;
        });
    }

    private void initWelcomeMessages() {
        addSystemMsg("Hello! I'm Saraswati AI, your personal learning assistant. 👋");
        addSystemMsg("I can help you with doubts in Mathematics, Science, English, Social Studies, Hindi, and Sanskrit.\n\nAsk me anything!");
    }

    private void sendUserMessage(EditText input) {
        String text = input.getText() != null ? input.getText().toString().trim() : "";
        if (text.isEmpty()) return;

        input.setText("");
        addUserMsg(text);
        showTyping();

        // Delay response to simulate AI processing
        long delay = 1200 + (text.length() * 10L);
        if (delay > 2500) delay = 2500;
        final String response = generateResponse(text);
        handler.postDelayed(() -> {
            if (!isAdded()) return;
            hideTyping();
            addSystemMsg(response);
        }, delay);
    }

    private void addUserMsg(String text) {
        messages.add(new ChatMessage(ChatMessage.TYPE_USER, text, now()));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollChat();
    }

    private void addSystemMsg(String text) {
        messages.add(new ChatMessage(ChatMessage.TYPE_SYSTEM, text, now()));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollChat();
    }

    private void showTyping() {
        messages.add(new ChatMessage(ChatMessage.TYPE_TYPING, "", ""));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollChat();
    }

    private void hideTyping() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).type == ChatMessage.TYPE_TYPING) {
                messages.remove(i);
                chatAdapter.notifyItemRemoved(i);
                return;
            }
        }
    }

    private void scrollChat() {
        chatRv.post(() -> chatRv.scrollToPosition(messages.size() - 1));
    }

    private String now() {
        Calendar c = Calendar.getInstance();
        return String.format(java.util.Locale.getDefault(),
            "%d:%02d %s",
            c.get(Calendar.HOUR) == 0 ? 12 : c.get(Calendar.HOUR),
            c.get(Calendar.MINUTE),
            c.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
    }

    // ── AI response generator ─────────────────────────────────────────────────

    private String generateResponse(String input) {
        String q = input.toLowerCase();

        // ── Greetings ──
        if (matches(q, "hello", "hi", "hey", "namaste", "good morning", "good evening"))
            return "Hello! Great to see you here 😊\n\nI'm ready to help you with any subject. What are you studying today?";

        if (matches(q, "thank", "thanks", "dhanyawad", "shukriya"))
            return "You're most welcome! 😊 Keep studying hard. Feel free to ask anything anytime. All the best for your exams!";

        if (matches(q, "help", "what can you do", "how to use"))
            return "I can help you with:\n\n📐 Mathematics — equations, geometry, trigonometry\n🔬 Science — physics, chemistry, biology\n📖 English — grammar, writing, literature\n🗺 Social Studies — history, geography, civics\n🖊 Hindi & Sanskrit — vyakaran, literature\n\nJust type your doubt and I'll explain it clearly!";

        // ── Mathematics ──
        if (matches(q, "quadratic", "discriminant", "b²-4ac", "factori", "factor the"))
            return "To solve a quadratic equation ax² + bx + c = 0:\n\n1️⃣ Compute discriminant D = b² − 4ac\n   • D > 0 → Two distinct real roots\n   • D = 0 → One repeated root\n   • D < 0 → No real roots\n\n2️⃣ Apply formula:\n   x = (−b ± √D) / 2a\n\nExample: x² − 5x + 6 = 0\nD = 25 − 24 = 1 → x = (5±1)/2 → x=3 or x=2 ✓";

        if (matches(q, "triangle", "congruent", "similar", "pythagoras", "hypotenuse"))
            return "Key triangle theorems:\n\n📐 Pythagoras: a² + b² = c² (right triangles)\n🔄 Congruence: SSS, SAS, ASA, RHS\n🔎 Similarity: AA, SAS, SSS\n\nAngle sum = 180° always.\nFor area: ½ × base × height\nHeron's formula: √(s(s-a)(s-b)(s-c)) where s = (a+b+c)/2";

        if (matches(q, "trigonometry", "sin", "cos", "tan", "trig ratio"))
            return "Trigonometric ratios (in a right triangle):\n\nSin θ = Opposite / Hypotenuse\nCos θ = Adjacent / Hypotenuse\nTan θ = Opposite / Adjacent\n\nMemory trick: SOH-CAH-TOA 🧠\n\nKey values:\n0°: sin=0, cos=1, tan=0\n30°: sin=½, cos=√3/2\n45°: sin=cos=1/√2\n60°: sin=√3/2, cos=½\n90°: sin=1, cos=0";

        if (matches(q, "algebra", "linear equation", "solve for x", "simultaneous"))
            return "For linear equations:\n\n1️⃣ Isolate the variable on one side\n2️⃣ Perform same operation on both sides\n3️⃣ Simplify and verify\n\nFor simultaneous equations:\n• Substitution: solve one equation for x, substitute in the other\n• Elimination: add/subtract equations to eliminate one variable\n\nAlways verify your answer by substituting back! ✅";

        if (matches(q, "statistic", "mean", "median", "mode", "probability"))
            return "Statistics basics:\n\n📊 Mean = Sum of all values / Number of values\n📍 Median = Middle value when arranged in order\n🏆 Mode = Most frequently occurring value\n\nFor Probability:\nP(event) = Favourable outcomes / Total outcomes\nP lies between 0 and 1.\nP(certain event) = 1, P(impossible event) = 0";

        // ── Science ──
        if (matches(q, "cell", "tissue", "organelle", "mitochondria", "nucleus"))
            return "Cell Biology essentials:\n\n🔬 Cell = Basic unit of life\n• Prokaryotic (no nucleus, e.g. bacteria)\n• Eukaryotic (with nucleus, e.g. plant/animal cells)\n\nKey organelles:\n• Nucleus → controls all activities\n• Mitochondria → powerhouse (ATP production)\n• Chloroplast → photosynthesis (plants only)\n• Cell wall → rigidity (plants only)\n• Vacuole → storage (large in plants)";

        if (matches(q, "photosynthesis", "chlorophyll", "light reaction", "carbon dioxide"))
            return "Photosynthesis:\n6CO₂ + 6H₂O + light → C₆H₁₂O₆ + 6O₂\n\n⚡ Occurs in chloroplasts\n🌿 Chlorophyll absorbs sunlight\n\nTwo stages:\n1. Light reaction — splits water, produces ATP & NADPH\n2. Dark reaction (Calvin cycle) — uses CO₂ to make glucose\n\nConditions needed: Sunlight, CO₂, water, chlorophyll";

        if (matches(q, "newton", "force", "motion", "velocity", "acceleration", "momentum"))
            return "Newton's Laws of Motion:\n\n1️⃣ First Law (Inertia): An object remains at rest or in uniform motion unless acted upon by a net force.\n\n2️⃣ Second Law: F = ma\n   Force (N) = Mass (kg) × Acceleration (m/s²)\n\n3️⃣ Third Law: Every action has an equal and opposite reaction.\n\nMomentum p = mv\nImpulse = F × t = change in momentum";

        if (matches(q, "chemical", "element", "compound", "reaction", "atom", "molecule"))
            return "Chemistry basics:\n\n⚛ Atom → smallest particle of an element\n🔗 Molecule → combination of atoms\n🧪 Compound → different elements in fixed ratio\n\nTypes of chemical reactions:\n• Combination: A + B → AB\n• Decomposition: AB → A + B\n• Displacement: A + BC → AC + B\n• Double displacement: AB + CD → AD + CB\n\nBalancing equations: ensure equal atoms on both sides ⚖";

        if (matches(q, "electric", "circuit", "current", "resistance", "ohm"))
            return "Electricity basics:\n\n⚡ Ohm's Law: V = IR\n   Voltage (V) = Current (A) × Resistance (Ω)\n\n• Series circuit: I is same, V divides, R_total = R₁+R₂+...\n• Parallel circuit: V is same, I divides, 1/R_total = 1/R₁+1/R₂+...\n\nPower: P = VI = I²R = V²/R\nElectric energy: E = P × t (in kWh or Joules)";

        // ── English ──
        if (matches(q, "grammar", "tense", "active", "passive", "voice"))
            return "Active vs Passive Voice:\n\n✅ Active: Subject + Verb + Object\n   'The teacher taught the lesson.'\n\n✅ Passive: Object + be(was/were/is/are) + V3 + by + Subject\n   'The lesson was taught by the teacher.'\n\nFor tense conversion in passive:\n• Simple present → am/is/are + V3\n• Simple past → was/were + V3\n• Future → will be + V3";

        if (matches(q, "essay", "writing", "paragraph", "letter", "notice"))
            return "Essay writing structure:\n\n📝 Introduction (1 paragraph)\n   → Hook + background + thesis statement\n\n📝 Body (2-3 paragraphs)\n   → Each paragraph = 1 main idea + examples/evidence\n\n📝 Conclusion (1 paragraph)\n   → Summarise + closing thought\n\nTip: Plan 5 min before writing. Aim for 200-250 words for short essays, 400-500 for long. Check for grammar and spellings! ✅";

        // ── Social Studies ──
        if (matches(q, "french revolution", "revolution", "independence", "freedom", "history"))
            return "For History questions:\n\n📚 Causes → What led to the event?\n📅 Events → Key dates, battles, treaties, leaders\n🌍 Effects → Short-term and long-term impact\n\nFor French Revolution specifically:\nCauses: Financial crisis, inequality (3 estates), Enlightenment ideas\nKey events: Storming of Bastille (1789), Declaration of Rights\nEffects: Spread of democratic ideas, Napoleon's rise\n\nAlways link causes → events → consequences in your answers!";

        if (matches(q, "geography", "climate", "map", "resources", "soil", "river"))
            return "Geography study tips:\n\n🗺 Always study with the atlas open — locate every feature on the map!\n\n☁ Climate factors: Latitude, altitude, distance from sea, wind patterns, ocean currents\n\n🌱 Soil types (India):\n• Alluvial → Ganga plains, most fertile\n• Black → Deccan, good for cotton\n• Red & Yellow → Orissa, Madhya Pradesh\n• Laterite → Hills of Tamil Nadu, Kerala\n\nMake flash cards for river systems and their tributaries!";

        if (matches(q, "constitution", "democracy", "fundamental", "rights", "civics"))
            return "Constitution & Democracy:\n\n📜 Indian Constitution:\n• Adopted: 26 Nov 1949, Enforced: 26 Jan 1950\n• Drafted by: Dr. B.R. Ambedkar (Chairman)\n\nFundamental Rights (Part III):\n1. Right to Equality (Art 14-18)\n2. Right to Freedom (Art 19-22)\n3. Right against Exploitation (Art 23-24)\n4. Right to Freedom of Religion (Art 25-28)\n5. Cultural & Educational Rights (Art 29-30)\n6. Right to Constitutional Remedies (Art 32)";

        // ── Hindi ──
        if (matches(q, "sandhi", "samas", "vyakaran", "hindi grammar", "karak", "vibhakti"))
            return "Hindi Vyakaran:\n\n📌 Sandhi — do shabd milne par dhwaniyon mein badlav\n• Swar Sandhi: do swar mile — A+A=AA (Ram+Ayana=Ramayana)\n• Vyanjan Sandhi: vyanjan + swar/vyanjan milna\n• Visarg Sandhi: Visarg ke baad kuch aaye\n\n📌 Samas — do shabd apni vibhakti chhodkar milna\n• Avyayibhav, Tatpurush, Karmadharaya, Dvandva, Bahuvrihi, Dvigu\n\nYaad rakhe: Sandhi toda jaata hai (+ dhwani change), Samas ka vigrah hota hai!";

        // ── Study tips ──
        if (matches(q, "exam", "prepare", "study", "revision", "score", "marks", "tip"))
            return "Top study tips for board exams:\n\n📅 Plan: Make a realistic timetable — cover all subjects weekly\n📖 NCERT first: Master NCERT completely before any other book\n✍ Write: Don't just read — write down key points, formulas, dates\n🔄 Revise: Revise every chapter at least 3 times\n📝 Practice: Solve last 5 years' board papers under timed conditions\n😴 Sleep: 7-8 hours is non-negotiable for memory consolidation\n\nRemember: Consistency beats cramming every time! 💪";

        if (matches(q, "time management", "schedule", "timetable", "distract", "focus", "concentrate"))
            return "Time management tips:\n\n⏱ Use the Pomodoro technique:\n   25 min focused study → 5 min break (repeat 4×) → 30 min long break\n\n📵 Keep your phone away while studying\n🌅 Study difficult subjects in the morning when your mind is fresh\n📝 End each session by writing 3 things you learned\n🎯 Set specific goals: 'Finish Chapter 4 today' (not just 'study Math')\n\nStart with the hardest subject first! 🧠";

        // ── Default ──
        return "That's a thoughtful question! 🤔\n\nHere's how I'd suggest approaching it:\n\n1️⃣ Re-read the relevant chapter in your NCERT textbook\n2️⃣ Check your class notes for teacher explanations\n3️⃣ Look at solved examples in the chapter\n4️⃣ Try solving a similar simpler problem first\n\nIf you're still stuck, post it in the Q&A section — your teacher will answer with a detailed explanation within the day.\n\nCan you tell me which chapter or subject this is from? I can give you a more specific answer! 😊";
    }

    private boolean matches(String input, String... keywords) {
        for (String k : keywords) {
            if (input.contains(k)) return true;
        }
        return false;
    }
}
