package com.saraswati.institute;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.saraswati.institute.network.ApiError;
import com.saraswati.institute.network.AuthRequests;
import com.saraswati.institute.network.MessageResponse;
import com.saraswati.institute.network.SessionManager;
import com.saraswati.institute.network.TokenResponse;

/**
 * Login screen for {@code POST /api/v1/auth/login}.
 *
 * <p>The service authenticates on <b>username or email</b> ({@code usernameOrEmail}), so the
 * first field accepts either. On success the token pair and profile are stored in
 * {@link SessionManager} and the user lands on {@link HomeActivity}. The service distinguishes
 * bad credentials (401), a disabled account or unverified address (403) and a temporary lock
 * after repeated failures (423); each gets its own treatment below.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout identifierLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText identifierInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private CharSequence loginButtonText;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = SessionManager.getInstance(this);

        identifierLayout = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        identifierInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        loginButtonText = loginButton.getText();

        clearErrorOnType(identifierInput, identifierLayout);
        clearErrorOnType(passwordInput, passwordLayout);

        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        loginButton.setOnClickListener(v -> attemptLogin());
        findViewById(R.id.register_link).setOnClickListener(v ->
                startActivity(new Intent(this, RegistrationActivity.class)));
        findViewById(R.id.forgot_password).setOnClickListener(v -> requestPasswordReset());

        prefillIdentifier();
    }

    /** After a sign-out the username is still known, so only the password has to be typed. */
    private void prefillIdentifier() {
        String remembered = session.getUsername();
        if (remembered == null || remembered.isEmpty()) {
            remembered = session.getUserEmail();
        }
        if (remembered != null && !remembered.isEmpty()) {
            identifierInput.setText(remembered);
            passwordInput.requestFocus();
        }
    }

    // ── Input ────────────────────────────────────────────

    private String identifier() {
        return identifierInput.getText() != null ? identifierInput.getText().toString().trim() : "";
    }

    private String password() {
        return passwordInput.getText() != null ? passwordInput.getText().toString() : "";
    }

    private boolean validate() {
        boolean ok = true;
        if (identifier().isEmpty()) {
            identifierLayout.setError(getString(R.string.login_error_identifier));
            ok = false;
        }
        if (password().isEmpty()) {
            passwordLayout.setError(getString(R.string.login_error_password));
            ok = false;
        }
        return ok;
    }

    // ── Login ────────────────────────────────────────────

    private void attemptLogin() {
        dismissKeyboard();
        if (!validate()) {
            return;
        }

        setLoading(true);
        ApiClient.getAuthApi()
                .login(new AuthRequests.Login(identifier(), password()))
                .enqueue(new ApiCallback<TokenResponse>() {
                    @Override
                    public void onSuccess(@Nullable TokenResponse tokens) {
                        if (tokens == null || !tokens.hasTokens()) {
                            Log.w(TAG, "Login returned no tokens");
                            passwordLayout.setError(getString(R.string.login_error_unexpected));
                            return;
                        }
                        session.saveSession(tokens);
                        Log.i(TAG, "Signed in with role " + tokens.getRole());
                        goHome();
                    }

                    @Override
                    public void onError(@NonNull ApiError error) {
                        Log.w(TAG, "Login failed: " + error);
                        handleLoginError(error);
                    }

                    @Override
                    public void onComplete() {
                        setLoading(false);
                    }
                });
    }

    private void handleLoginError(ApiError error) {
        if (error.isTransport()) {
            Toast.makeText(this, error.message(), Toast.LENGTH_LONG).show();
            return;
        }
        if (error.isEmailNotVerified()) {
            promptResendVerification();
            return;
        }
        if (error.isAccountLocked() || error.isForbidden()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(error.isAccountLocked()
                            ? R.string.login_locked_title
                            : R.string.login_blocked_title)
                    .setMessage(error.message())
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
        // 401 and anything else: the message belongs next to the credential fields.
        passwordLayout.setError(error.message());
    }

    /**
     * A 403 about verification means the account exists and the password was right — the
     * address simply has not been confirmed, so offer to send the email again.
     */
    private void promptResendVerification() {
        final String email = identifier();
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.login_verify_title)
                .setMessage(R.string.login_verify_message)
                .setNegativeButton(android.R.string.cancel, null);

        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            dialog.setPositiveButton(R.string.login_verify_resend, (d, which) -> resendVerification(email));
        } else {
            // The service only resends by email address, not by username.
            dialog.setMessage(getString(R.string.login_verify_message_username));
            dialog.setPositiveButton(android.R.string.ok, null);
        }
        dialog.show();
    }

    private void resendVerification(String email) {
        ApiClient.getAuthApi()
                .resendVerification(new AuthRequests.Email(email))
                .enqueue(new ApiCallback<MessageResponse>() {
                    @Override
                    public void onSuccess(@Nullable MessageResponse body) {
                        Toast.makeText(LoginActivity.this,
                                body != null
                                        ? body.messageOr(getString(R.string.login_verify_sent))
                                        : getString(R.string.login_verify_sent),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(@NonNull ApiError error) {
                        Toast.makeText(LoginActivity.this, error.message(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Forgot password ──────────────────────────────────

    /**
     * {@code forgot-password} takes an email address and always answers 200, so the reply
     * never reveals whether the address is registered.
     */
    private void requestPasswordReset() {
        final String email = identifier();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            identifierLayout.setError(getString(R.string.login_error_email_for_reset));
            return;
        }
        ApiClient.getAuthApi()
                .forgotPassword(new AuthRequests.Email(email))
                .enqueue(new ApiCallback<MessageResponse>() {
                    @Override
                    public void onSuccess(@Nullable MessageResponse body) {
                        // In dev the service echoes the token as debugToken, so the reset
                        // screen can be opened with it already filled in.
                        final String debugToken = body != null ? body.getDebugToken() : null;
                        new MaterialAlertDialogBuilder(LoginActivity.this)
                                .setTitle(R.string.login_reset_title)
                                .setMessage(body != null
                                        ? body.messageOr(getString(R.string.login_reset_sent))
                                        : getString(R.string.login_reset_sent))
                                .setPositiveButton(R.string.login_reset_enter_code,
                                        (dialog, which) -> openResetScreen(debugToken))
                                .setNegativeButton(android.R.string.ok, null)
                                .show();
                    }

                    @Override
                    public void onError(@NonNull ApiError error) {
                        Toast.makeText(LoginActivity.this, error.message(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Opens the reset screen; {@code token} is pre-filled when the service handed one back. */
    private void openResetScreen(@Nullable String token) {
        Intent intent = new Intent(this, ResetPasswordActivity.class);
        if (token != null && !token.isEmpty()) {
            intent.putExtra(ResetPasswordActivity.EXTRA_TOKEN, token);
        }
        startActivity(intent);
    }

    private void goHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
    }

    // ── UI helpers ───────────────────────────────────────

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        identifierInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        loginButton.setText(loading ? getString(R.string.login_signing_in) : loginButtonText);
    }

    private void clearErrorOnType(TextInputEditText input, TextInputLayout layout) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void dismissKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
