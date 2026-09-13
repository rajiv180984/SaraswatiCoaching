package com.saraswati.institute;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Test attempt screen.
 *
 * State per question:
 *   0 = not visited, 1 = answered, 2 = not answered (visited+skipped), 3 = marked for review
 *
 * Real implementation: load test from API, persist answers per-question to backend on each tap.
 */
public class TestAttemptActivity extends AppCompatActivity {

    private static final long DURATION_MS = 30 * 60 * 1000L; // 30 min

    private List<Question> questions;
    private int currentIndex = 0;
    private int[] answeredOption;     // -1 = none
    private int[] questionStatus;     // see header

    private TextView timerText, questionNumber, marksText, questionBody;
    private RecyclerView optionsRv, paletteRv;
    private OptionAdapter optionAdapter;
    private QuestionPaletteAdapter paletteAdapter;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_attempt);

        questions = buildMockQuestions();
        answeredOption = new int[questions.size()];
        questionStatus = new int[questions.size()];
        Arrays.fill(answeredOption, -1);

        timerText      = findViewById(R.id.timer_text);
        questionNumber = findViewById(R.id.question_number);
        marksText      = findViewById(R.id.marks_text);
        questionBody   = findViewById(R.id.question_body);
        optionsRv      = findViewById(R.id.options_rv);
        paletteRv      = findViewById(R.id.palette_rv);

        optionsRv.setLayoutManager(new LinearLayoutManager(this));
        optionAdapter = new OptionAdapter(new ArrayList<>(), -1, this::onOptionTapped);
        optionsRv.setAdapter(optionAdapter);

        paletteRv.setLayoutManager(new GridLayoutManager(this, 6));
        paletteAdapter = new QuestionPaletteAdapter(questions.size(), questionStatus, currentIndex, idx -> {
            currentIndex = idx;
            markVisited();
            renderQuestion();
        });
        paletteRv.setAdapter(paletteAdapter);

        MaterialButton prev   = findViewById(R.id.prev_button);
        MaterialButton next   = findViewById(R.id.next_button);
        MaterialButton review = findViewById(R.id.mark_review_button);
        MaterialButton clear  = findViewById(R.id.clear_button);
        MaterialButton submit = findViewById(R.id.submit_button);

        prev.setOnClickListener(v -> { if (currentIndex > 0) { currentIndex--; markVisited(); renderQuestion(); } });
        next.setOnClickListener(v -> { if (currentIndex < questions.size() - 1) { currentIndex++; markVisited(); renderQuestion(); } });
        review.setOnClickListener(v -> { questionStatus[currentIndex] = 3; paletteAdapter.notifyDataSetChanged(); });
        clear.setOnClickListener(v -> {
            answeredOption[currentIndex] = -1;
            questionStatus[currentIndex] = 2;
            paletteAdapter.notifyDataSetChanged();
            renderQuestion();
        });
        submit.setOnClickListener(v -> showSubmitDialog());

        findViewById(R.id.close_button).setOnClickListener(v -> finish());

        startTimer();
        markVisited();
        renderQuestion();
    }

    private void onOptionTapped(int index) {
        answeredOption[currentIndex] = index;
        questionStatus[currentIndex] = 1;
        optionAdapter.setSelected(index);
        paletteAdapter.notifyItemChanged(currentIndex);
    }

    private void markVisited() {
        if (questionStatus[currentIndex] == 0) questionStatus[currentIndex] = 2;
        paletteAdapter.setCurrent(currentIndex);
    }

    private void renderQuestion() {
        Question q = questions.get(currentIndex);
        questionNumber.setText(getString(R.string.test_question_of, currentIndex + 1, questions.size()));
        marksText.setText(getString(R.string.test_marks, q.marks) + "  ·  " + getString(R.string.test_negative, q.negative));
        questionBody.setText(q.body);
        optionAdapter.setItems(q.options, answeredOption[currentIndex]);
    }

    private void startTimer() {
        timer = new CountDownTimer(DURATION_MS, 1000L) {
            @Override public void onTick(long ms) {
                long total = ms / 1000L;
                long m = total / 60, s = total % 60;
                timerText.setText(String.format(Locale.US, "%02d:%02d", m, s));
            }
            @Override public void onFinish() {
                timerText.setText("00:00");
                autoSubmit();
            }
        }.start();
    }

    private void showSubmitDialog() {
        int answered = 0;
        for (int a : answeredOption) if (a >= 0) answered++;
        new AlertDialog.Builder(this)
            .setTitle(R.string.test_submit_confirm_title)
            .setMessage(getString(R.string.test_submit_confirm_message, answered, questions.size()))
            .setPositiveButton(R.string.test_submit_confirm_yes, (d, w) -> autoSubmit())
            .setNegativeButton(R.string.test_submit_confirm_no, null)
            .show();
    }

    private void autoSubmit() {
        // TODO: POST answers to /tests/{id}/submit; then navigate to results screen.
        finish();
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }

    // ─────────── Data ───────────

    static class Question {
        final String body;
        final List<String> options;
        final int marks;
        final float negative;
        Question(String body, List<String> options, int marks, float negative) {
            this.body = body; this.options = options; this.marks = marks; this.negative = negative;
        }
    }

    private List<Question> buildMockQuestions() {
        List<Question> list = new ArrayList<>();
        list.add(new Question(getString(R.string.demo_q1),
            Arrays.asList(
                getString(R.string.demo_q1_opt_a),
                getString(R.string.demo_q1_opt_b),
                getString(R.string.demo_q1_opt_c),
                getString(R.string.demo_q1_opt_d)),
            4, 1.00f));
        // pad to 30 with placeholder questions
        for (int i = 1; i < 30; i++) {
            list.add(new Question("Question " + (i + 1) + " — placeholder body, replace with real content from API.",
                Arrays.asList("Option A", "Option B", "Option C", "Option D"), 4, 1.00f));
        }
        return list;
    }
}
