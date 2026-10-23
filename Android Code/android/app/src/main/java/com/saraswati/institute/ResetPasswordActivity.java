package com.saraswati.institute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.saraswati.institute.network.ApiCallback;
import com.saraswati.institute.network.ApiClient;
import com.saraswati.institute.network.ApiConfig;
import com.saraswati.institute.network.ApiError;
import com.saraswati.institute.network.AuthRequests;
import com.saraswati.institute.network.MessageResponse;
import com.saraswati.institute.network.SessionManager;

import java.util.regex.Pattern;

/**
 * Completes the password reset started by {@code POST /api/v1/auth/forgot-password}.
 *
 * <p>The service emails a link of the form
 * {@code <app.frontend-url>/reset-password?token=<token>}; the manifest registers this
 * activity for that path so tapping the link opens it with the token already filled in.
 * The token can also be pasted by hand, which is how the flow works while
 * {@code app.security.expose-tokens-in-response=true} echoes it in the API response.
 *
 * <p>A successful reset revokes every session for that account, so any session held on this
 * device is cleared before returning to the login screen.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    /** Extra for opening the screen from inside the app with a token already in hand. */
    public static final String EXTRA_TOKEN = "com.saraswati.institute.extra.RESET_TOKEN";

    private static final Pattern PASSWORD_RULE = Pattern.compile(ApiConfig.PASSWORD_REGEX);

    private TextInputLayout tokenLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmLayout;
    private TextInputEditText tokenInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private MaterialButton submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        tokenLayout = findViewById(R.id.token_layout);
        passwordLayout = findViewById(R.id.new_password_layout);
        confirmLayout = findViewById(R.id.confirm_password_layout);
        tokenInput = findViewById(R.id.token_input);
        passwordInput = findViewById(R.id.new_password_input);
        confirmInput = findViewById(R.id.confirm_password_input);
        submitButton = findViewById(R.id.reset_button);

        String token = tokenFromIntent(getIntent());
        if (token != null) {
            tokenInput.setText(token);
            passwordInput.requestFocus();
        }

        confirmInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        submitButton.setOnClickListener(v -> submit());
    }

    @Nullable
    private static String tokenFromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        String extra = intent.getStringExtra(EXTRA_TOKEN);
        if (extra != null && !extra.isEmpty()) {
            return extra;
        }
        Uri data = intent.getData();
        return data != null ? data.getQueryParameter("token") : null;
    }

    // ── Submit ───────────────────────────────────────────

    private void submit() {
        String token = text(tokenInput);
        String password = raw(passwordInput);

        boolean valid = true;
        if (token.isEmpty()) {
            tokenLayout.setError(getString(R.string.reset_error_token));
            valid = false;
        } else {
            tokenLayout.setError(null);
        }
        if (!PASSWORD_RULE.matcher(password).matches()) {
            passwordLayout.setError(getString(R.string.registration_error_password));
            valid = false;
        } else {
            passwordLayout.setError(null);
        }
        if (!password.equals(raw(confirmInput))) {
            confirmLayout.setError(getString(R.string.reset_error_mismatch));
            valid = false;
        } else {
            confirmLayout.setError(null);
        }
        if (!valid) {
            return;
        }

        setSubmitting(true);
        ApiClient.getAuthApi()
                .resetPassword(new AuthRequests.ResetPassword(token, password))
                .enqueue(new ApiCallback<MessageResponse>() {
                    @Override
                    public void onSuccess(@Nullable MessageResponse body) {
                        // The reset revoked every session, including this device's.
                        SessionManager.getInstance(ResetPasswordActivity.this).clear();
                        new MaterialAlertDialogBuilder(ResetPasswordActivity.this)
                                .setTitle(R.string.reset_done_title)
                                .setMessage(body != null
                                        ? body.messageOr(getString(R.string.reset_done_message))
                                        : getString(R.string.reset_done_message))
                                .setPositiveButton(R.string.registration_go_to_login,
                                        (dialog, which) -> goToLogin())
                                .setCancelable(false)
                                .show();
                    }

                    @Override
                    public void onError(@NonNull ApiError error) {
                        if (error.isTransport()) {
                            Toast.makeText(ResetPasswordActivity.this, error.message(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        String newPasswordError = error.fieldError("newPassword");
                        if (newPasswordError != null) {
                            passwordLayout.setError(newPasswordError);
                        } else {
                            // An expired or already-used token is the usual cause of a 400.
                            tokenLayout.setError(error.message());
                        }
                    }

                    @Override
                    public void onComplete() {
                        setSubmitting(false);
                    }
                });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setSubmitting(boolean submitting) {
        submitButton.setEnabled(!submitting);
        submitButton.setText(submitting ? R.string.reset_submitting : R.string.reset_submit);
    }

    private static String text(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString().trim() : "";
    }

    private static String raw(TextInputEditText input) {
        return input.getText() != null ? input.getText().toString() : "";
    }
}
