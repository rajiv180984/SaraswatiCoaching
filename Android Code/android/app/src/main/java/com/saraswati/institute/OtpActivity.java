package com.saraswati.institute;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * OTP entry screen — 4 boxes, auto-advance, backspace navigation,
 * paste support, shake + error on wrong OTP, max-3-attempts lock, 30s resend timer.
 *
 * Demo OTP: 1234
 * Real implementation: POST {phone, otp} to /auth/verify-otp → store JWT → navigate Home.
 */
public class OtpActivity extends AppCompatActivity {

    public static final String EXTRA_PHONE = "phone";

    private static final long   RESEND_MS    = 30_000L;
    private static final String DEMO_OTP     = "1234";
    private static final int    MAX_ATTEMPTS = 3;

    private final TextInputEditText[] boxes = new TextInputEditText[4];

    private LinearLayout     boxesRow;
    private TextView         tvError;
    private ImageView        ivErrorIcon;
    private TextView         resendText;
    private MaterialButton   verifyButton;
    private CountDownTimer   resendTimer;

    private int wrongAttempts = 0;

    // ── Lifecycle ────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        String phone = getIntent().getStringExtra(EXTRA_PHONE);

        TextView subtitle = findViewById(R.id.otp_subtitle);
        subtitle.setText(getString(R.string.otp_subtitle, phone != null ? phone : "your number"));

        findViewById(R.id.change_number).setOnClickListener(v -> finish());

        // Bind the 4 boxes
        boxesRow    = findViewById(R.id.otp_boxes_row);
        tvError     = findViewById(R.id.tv_otp_error);
        ivErrorIcon = findViewById(R.id.iv_error_icon);

        int[] ids = {R.id.otp_1, R.id.otp_2, R.id.otp_3, R.id.otp_4};
        for (int i = 0; i < 4; i++) {
            boxes[i] = findViewById(ids[i]);
            attachWatcher(i);
            attachBackspaceKey(i);
        }

        verifyButton = findViewById(R.id.verify_button);
        verifyButton.setEnabled(false);
        verifyButton.setOnClickListener(v -> attemptVerify());

        resendText = findViewById(R.id.resend_text);
        resendText.setOnClickListener(v -> {
            if (Boolean.TRUE.equals(v.getTag())) {
                wrongAttempts = 0;
                resetBoxes();
                startResendTimer();
                // TODO: call backend /auth/request-otp with the phone number
            }
        });

        startResendTimer();
        boxes[0].requestFocus();
        showKeyboard(boxes[0]);
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) resendTimer.cancel();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish(); // allow back to login
    }

    // ── Box behaviour ────────────────────────────────────

    private void attachWatcher(final int index) {
        boxes[index].addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Typing clears the error UI
                clearErrorUi();

                String txt = s.toString();

                if (txt.length() > 1) {
                    // ── Paste: distribute up to 4 digits ──
                    String digits = txt.replaceAll("\\D", "");
                    int len = Math.min(4, digits.length());
                    for (int i = 0; i < len; i++) {
                        boxes[i].setText(String.valueOf(digits.charAt(i)));
                    }
                    // Move focus to last filled box or verify button
                    if (len == 4) {
                        hideKeyboard();
                        verifyButton.requestFocus();
                    } else {
                        boxes[len - 1].requestFocus();
                    }
                } else if (txt.length() == 1 && index < 3) {
                    // Advance to next box
                    boxes[index + 1].requestFocus();
                } else if (txt.isEmpty() && index > 0 && before > 0) {
                    // Backspace from within — go back
                    boxes[index - 1].requestFocus();
                }

                updateVerifyEnabled();
            }
        });
    }

    /** Handle backspace on an empty box to move focus back. */
    private void attachBackspaceKey(final int index) {
        boxes[index].setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL
                    && event.getAction() == KeyEvent.ACTION_DOWN) {
                String cur = boxes[index].getText() != null
                    ? boxes[index].getText().toString() : "";
                if (cur.isEmpty() && index > 0) {
                    boxes[index - 1].requestFocus();
                    boxes[index - 1].setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void updateVerifyEnabled() {
        verifyButton.setEnabled(otpComplete() && wrongAttempts < MAX_ATTEMPTS);
    }

    private boolean otpComplete() {
        for (TextInputEditText box : boxes) {
            if (box.getText() == null || box.getText().toString().isEmpty()) return false;
        }
        return true;
    }

    private String collectOtp() {
        StringBuilder sb = new StringBuilder();
        for (TextInputEditText box : boxes) {
            sb.append(box.getText() == null ? "" : box.getText().toString().trim());
        }
        return sb.toString();
    }

    // ── Verification ─────────────────────────────────────

    private void attemptVerify() {
        String entered = collectOtp();

        if (DEMO_OTP.equals(entered)) {
            onVerifySuccess();
        } else {
            wrongAttempts++;
            onVerifyFailure();
        }
    }

    private void onVerifySuccess() {
        // Navigate to Home and clear back-stack
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
    }

    private void onVerifyFailure() {
        // Switch boxes to error background
        for (TextInputEditText box : boxes) {
            box.setBackground(getDrawable(R.drawable.bg_otp_box_error));
        }

        // Show error message
        tvError.setVisibility(View.VISIBLE);
        ivErrorIcon.setVisibility(View.VISIBLE);

        if (wrongAttempts >= MAX_ATTEMPTS) {
            tvError.setText("Too many incorrect attempts. Please resend a new OTP.");
            verifyButton.setEnabled(false);
        } else {
            int left = MAX_ATTEMPTS - wrongAttempts;
            tvError.setText("Incorrect OTP. "
                + left + " attempt" + (left > 1 ? "s" : "") + " remaining.");
        }

        // Shake the boxes row
        shakeBoxes();

        // After a short pause, clear boxes and restore normal background
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            for (TextInputEditText box : boxes) {
                box.setText("");
                box.setBackground(getDrawable(R.drawable.bg_otp_box));
            }
            if (wrongAttempts < MAX_ATTEMPTS) {
                boxes[0].requestFocus();
                showKeyboard(boxes[0]);
            }
            updateVerifyEnabled();
        }, 700);
    }

    // ── Error UI ─────────────────────────────────────────

    private void clearErrorUi() {
        if (tvError.getVisibility() == View.VISIBLE) {
            tvError.setVisibility(View.GONE);
            ivErrorIcon.setVisibility(View.GONE);
            for (TextInputEditText box : boxes) {
                box.setBackground(getDrawable(R.drawable.bg_otp_box));
            }
        }
    }

    private void resetBoxes() {
        clearErrorUi();
        for (TextInputEditText box : boxes) box.setText("");
        boxes[0].requestFocus();
        updateVerifyEnabled();
    }

    // ── Shake animation ──────────────────────────────────

    private void shakeBoxes() {
        ObjectAnimator shake = ObjectAnimator.ofFloat(
            boxesRow, "translationX",
            0f, -18f, 18f, -14f, 14f, -8f, 8f, -4f, 4f, 0f
        );
        shake.setDuration(550);
        shake.setInterpolator(new DecelerateInterpolator());
        shake.start();
    }

    // ── Resend timer ─────────────────────────────────────

    private void startResendTimer() {
        resendText.setTag(false);
        resendText.setTextColor(getColor(R.color.text_tertiary));
        if (resendTimer != null) resendTimer.cancel();

        resendTimer = new CountDownTimer(RESEND_MS, 1000L) {
            @Override
            public void onTick(long ms) {
                resendText.setText(getString(R.string.otp_resend_in, (int) (ms / 1000)));
            }
            @Override
            public void onFinish() {
                resendText.setText(R.string.otp_resend);
                resendText.setTextColor(getColor(R.color.reliance_blue));
                resendText.setTag(true);
            }
        }.start();
    }

    // ── Keyboard helpers ─────────────────────────────────

    private void hideKeyboard() {
        View focused = getCurrentFocus();
        if (focused != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    private void showKeyboard(View target) {
        target.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT);
        }, 150);
    }
}
