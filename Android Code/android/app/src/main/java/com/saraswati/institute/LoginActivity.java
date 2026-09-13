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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.saraswati.institute.network.ApiClient;
import com.saraswati.institute.network.ApiResponse;
import com.saraswati.institute.network.AuthResponse;
import com.saraswati.institute.network.ForgotPasswordRequest;
import com.saraswati.institute.network.LoginRequest;
import com.saraswati.institute.network.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Login screen — email + password against {@code POST /api/auth/login} on saraswati-auth.
 * On success the JWT access/refresh tokens are stored via {@link SessionManager} and the
 * user lands on {@link HomeActivity}.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputLayout   emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private MaterialButton    loginButton;
    private CharSequence      loginButtonText;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        session = SessionManager.getInstance(this);

        emailLayout    = findViewById(R.id.email_layout);
        passwordLayout = findViewById(R.id.password_layout);
        emailInput     = findViewById(R.id.email_input);
        passwordInput  = findViewById(R.id.password_input);
        loginButton    = findViewById(R.id.login_button);
        loginButtonText = loginButton.getText();

        clearErrorOnType(emailInput, emailLayout);
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
    }

    // ── Validation ───────────────────────────────────────

    private String email() {
        return emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
    }

    private String password() {
        return passwordInput.getText() != null ? passwordInput.getText().toString() : "";
    }

    private boolean validate() {
        boolean ok = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email()).matches()) {
            emailLayout.setError(getString(R.string.login_error_email));
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
        if (!validate()) return;

        Log.d(TAG, "Attempting login for " + email());
        setLoading(true);
        ApiClient.getAuthApi()
            .login(new LoginRequest(email(), password()))
            .enqueue(new Callback<ApiResponse<AuthResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<AuthResponse>> call,
                                       Response<ApiResponse<AuthResponse>> response) {
                    setLoading(false);
                    ApiResponse<AuthResponse> body = response.body();
                    if (response.isSuccessful() && body != null && body.getData() != null) {
                        Log.i(TAG, "Login succeeded for " + email());
                        session.saveSession(body.getData());
                        goHome();
                    } else {
                        Log.w(TAG, "Login rejected (HTTP " + response.code() + ")");
                        passwordLayout.setError(messageOr(body, "Invalid email or password"));
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                    setLoading(false);
                    Log.e(TAG, "Login call failed", t);
                    Toast.makeText(LoginActivity.this,
                        "Could not reach the server: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void requestPasswordReset() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email()).matches()) {
            emailLayout.setError(getString(R.string.login_error_email));
            return;
        }
        Log.d(TAG, "Requesting password reset for " + email());
        ApiClient.getAuthApi()
            .forgotPassword(new ForgotPasswordRequest(email()))
            .enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    Log.i(TAG, "Password reset request completed (HTTP " + response.code() + ")");
                    Toast.makeText(LoginActivity.this,
                        "If that email is registered, a reset link has been sent",
                        Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    Log.e(TAG, "Password reset call failed", t);
                    Toast.makeText(LoginActivity.this,
                        "Could not reach the server: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void goHome() {
        Log.d(TAG, "Navigating to HomeActivity");
        startActivity(new Intent(this, HomeActivity.class));
        finishAffinity();
    }

    // ── UI helpers ───────────────────────────────────────

    private void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        loginButton.setText(loading ? getString(R.string.login_signing_in) : loginButtonText);
    }

    private static String messageOr(ApiResponse<?> body, String fallback) {
        if (body != null && body.getMessage() != null && !body.getMessage().isEmpty()) {
            return body.getMessage();
        }
        return fallback;
    }

    private void clearErrorOnType(TextInputEditText input, TextInputLayout layout) {
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { layout.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void dismissKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
