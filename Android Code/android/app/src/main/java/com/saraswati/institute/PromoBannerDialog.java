package com.saraswati.institute;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Promotional banner popup shown once after login (or every session
 * unless the user checks "Don't show this again").
 *
 * Usage:
 *   PromoBannerDialog.showIfNeeded(activity, () -> openRegistration());
 */
public class PromoBannerDialog extends Dialog {

    static final String PREFS_NAME    = "saraswati_prefs";
    static final String KEY_DISMISSED = "promo_dismissed";

    private final Runnable onEnrollClick;

    public PromoBannerDialog(@NonNull Context context, @Nullable Runnable onEnrollClick) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.onEnrollClick = onEnrollClick;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_promo_banner);

        configureWindow();
        setCanceledOnTouchOutside(true);
        setCancelable(true);

        CheckBox cbDontShow = findViewById(R.id.cb_dont_show);

        // Close  ✕  button
        findViewById(R.id.btn_promo_close).setOnClickListener(v -> {
            savePrefsIfChecked(cbDontShow);
            dismiss();
        });

        // "Maybe Later" text
        findViewById(R.id.btn_maybe_later).setOnClickListener(v -> {
            savePrefsIfChecked(cbDontShow);
            dismiss();
        });

        // "Enroll Now" — also saves pref (user has acted on the offer)
        findViewById(R.id.btn_enroll_now).setOnClickListener(v -> {
            saveDismissed();
            dismiss();
            if (onEnrollClick != null) onEnrollClick.run();
        });

        // Tapping outside also respects the checkbox
        setOnDismissListener(dialog -> savePrefsIfChecked(cbDontShow));
    }

    // ── Window setup ──────────────────────────────────────

    private void configureWindow() {
        Window w = getWindow();
        if (w == null) return;

        w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        w.setDimAmount(0.55f);

        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        int dialogWidth = (int) (dm.widthPixels * 0.92f);

        w.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
        w.setGravity(Gravity.CENTER);
    }

    // ── SharedPreferences ─────────────────────────────────

    private void savePrefsIfChecked(CheckBox cb) {
        if (cb != null && cb.isChecked()) saveDismissed();
    }

    private void saveDismissed() {
        getContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DISMISSED, true)
            .apply();
    }

    // ── Static factory ────────────────────────────────────

    /**
     * Shows the promo dialog if the user hasn't dismissed it permanently.
     *
     * @param context       The host activity.
     * @param onEnrollClick Called when the user taps "Enroll Now". Can be null.
     */
    public static void showIfNeeded(@NonNull Context context, @Nullable Runnable onEnrollClick) {
        SharedPreferences prefs =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_DISMISSED, false)) {
            new PromoBannerDialog(context, onEnrollClick).show();
        }
    }
}
