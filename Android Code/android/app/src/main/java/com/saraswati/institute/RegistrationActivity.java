package com.saraswati.institute;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.saraswati.institute.network.ApiCallback;
import com.saraswati.institute.network.ApiClient;
import com.saraswati.institute.network.ApiConfig;
import com.saraswati.institute.network.ApiError;
import com.saraswati.institute.network.AuthRequests;
import com.saraswati.institute.network.RegistrationResponse;
import com.saraswati.institute.network.SessionManager;
import com.saraswati.institute.network.TokenResponse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    // ── Section 1 ──────────────────────────────────────────
    private TextInputLayout  tilName, tilDob, tilAddress1, tilParentName, tilParentMobile;
    private TextInputLayout  tilEmail, tilUsername, tilPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etPassword, etConfirmPassword;
    private TextInputEditText etName, etDob, etAge, etMobile, etEmail;
    private TextInputEditText etAddress1, etAddress2, etCity, etPincode;
    private MaterialAutoCompleteTextView actState;
    private RadioGroup rgGender, rgRelation;
    private TextInputEditText etParentName, etParentMobile, etParentEmail, etAlternateContact;

    // ── Section 2 ──────────────────────────────────────────
    private TextInputLayout  tilClass, tilStream, tilSchool, tilBoard;
    private MaterialAutoCompleteTextView actClass, actSection, actStream, actBoard;
    private ChipGroup cgSubjects;
    private TextInputEditText etSchool, etSchoolCity;

    // ── Section 3 ──────────────────────────────────────────
    private TextInputEditText etPrevClass, etMarksObtained, etMaxMarks, etPercentage, etRank;
    private TextInputEditText etAchievements, etExtracurricular;

    // ── Section 4 ──────────────────────────────────────────
    private MaterialAutoCompleteTextView actHearAbout;
    private TextInputEditText etTeacherName, etTeacherContact;

    private Button btnSubmit;

    /** The service's own password and username rules, checked before the round trip. */
    private static final Pattern PASSWORD_RULE = Pattern.compile(ApiConfig.PASSWORD_REGEX);
    private static final Pattern USERNAME_RULE = Pattern.compile(ApiConfig.USERNAME_REGEX);

    // ── Subject lists ──────────────────────────────────────
    private static final String[] SUBJECTS_6_10 = {
        "Mathematics", "Science", "English", "Hindi",
        "Social Studies", "Sanskrit", "Computer Science"
    };
    private static final String[] SUBJECTS_11_SCIENCE = {
        "Physics", "Chemistry", "Mathematics", "Biology",
        "English", "Computer Science", "Physical Education"
    };
    private static final String[] SUBJECTS_11_COMMERCE = {
        "Accountancy", "Business Studies", "Economics",
        "Mathematics", "English", "Informatics Practices"
    };
    private static final String[] SUBJECTS_11_ARTS = {
        "History", "Geography", "Political Science",
        "Economics", "English", "Sociology", "Psychology"
    };

    private static final String[] INDIAN_STATES = {
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
        "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
        "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
        "Delhi", "Jammu & Kashmir", "Ladakh"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setupToolbar();
        bindViews();
        setupDropdowns();
        setupDobPicker();
        setupClassListener();
        setupStreamListener();
        setupMarksListener();
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) submitRegistration();
        });
    }

    // ── Toolbar ────────────────────────────────────────────

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Registration");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── View binding ───────────────────────────────────────

    private void bindViews() {
        // Section 1 — TextInputLayouts (for validation)
        tilName          = findViewById(R.id.til_name);
        tilDob           = findViewById(R.id.til_dob);
        tilAddress1      = findViewById(R.id.til_address1);
        tilParentName    = findViewById(R.id.til_parent_name);
        tilParentMobile  = findViewById(R.id.til_parent_mobile);

        tilEmail           = findViewById(R.id.til_email);
        tilUsername        = findViewById(R.id.til_username);
        tilPassword        = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);

        // Section 1 — EditTexts
        etName             = findViewById(R.id.et_name);
        etDob              = findViewById(R.id.et_dob);
        etAge              = findViewById(R.id.et_age);
        etMobile           = findViewById(R.id.et_mobile);
        etEmail            = findViewById(R.id.et_email);
        etUsername         = findViewById(R.id.et_username);
        etPassword         = findViewById(R.id.et_password);
        etConfirmPassword  = findViewById(R.id.et_confirm_password);
        etAddress1         = findViewById(R.id.et_address1);
        etAddress2         = findViewById(R.id.et_address2);
        etCity             = findViewById(R.id.et_city);
        etPincode          = findViewById(R.id.et_pincode);
        actState           = findViewById(R.id.act_state);
        rgGender           = findViewById(R.id.rg_gender);
        rgRelation         = findViewById(R.id.rg_relation);
        etParentName       = findViewById(R.id.et_parent_name);
        etParentMobile     = findViewById(R.id.et_parent_mobile);
        etParentEmail      = findViewById(R.id.et_parent_email);
        etAlternateContact = findViewById(R.id.et_alternate_contact);

        // Section 2
        tilClass    = findViewById(R.id.til_class);
        tilStream   = findViewById(R.id.til_stream);
        tilSchool   = findViewById(R.id.til_school);
        tilBoard    = findViewById(R.id.til_board);
        actClass    = findViewById(R.id.act_class);
        actSection  = findViewById(R.id.act_section);
        actStream   = findViewById(R.id.act_stream);
        actBoard    = findViewById(R.id.act_board);
        cgSubjects  = findViewById(R.id.cg_subjects);
        etSchool    = findViewById(R.id.et_school);
        etSchoolCity= findViewById(R.id.et_school_city);

        // Section 3
        etPrevClass      = findViewById(R.id.et_prev_class);
        etMarksObtained  = findViewById(R.id.et_marks_obtained);
        etMaxMarks       = findViewById(R.id.et_max_marks);
        etPercentage     = findViewById(R.id.et_percentage);
        etRank           = findViewById(R.id.et_rank);
        etAchievements   = findViewById(R.id.et_achievements);
        etExtracurricular= findViewById(R.id.et_extracurricular);

        // Section 4
        actHearAbout     = findViewById(R.id.act_hear_about);
        etTeacherName    = findViewById(R.id.et_teacher_name);
        etTeacherContact = findViewById(R.id.et_teacher_contact);

        btnSubmit        = findViewById(R.id.btn_submit);
    }

    // ── Dropdowns ──────────────────────────────────────────

    private void setupDropdowns() {
        setDropdown(actState,    INDIAN_STATES);
        setDropdown(actClass,    new String[]{"6", "7", "8", "9", "10", "11", "12"});
        setDropdown(actSection,  new String[]{"A", "B", "C", "D", "E"});
        setDropdown(actStream,   new String[]{"Science", "Commerce", "Arts"});
        setDropdown(actBoard,    new String[]{"CBSE", "ICSE", "State Board", "IGCSE", "Other"});
        setDropdown(actHearAbout, new String[]{
            "Teacher Referral", "Friend / Relative", "Advertisement",
            "Social Media", "Newspaper / Pamphlet", "Walk-in", "Other"
        });

        // Default subject chips for classes 6–10
        buildSubjectChips(SUBJECTS_6_10);
    }

    private void setDropdown(MaterialAutoCompleteTextView view, String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, items);
        view.setAdapter(adapter);
    }

    // ── DOB picker ─────────────────────────────────────────

    private void setupDobPicker() {
        View.OnClickListener pickDate = v -> showDatePicker();
        etDob.setOnClickListener(pickDate);
        // Also allow tap on the end icon area via the layout
        findViewById(R.id.til_dob).setOnClickListener(pickDate);
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(
            this,
            (picker, year, month, day) -> {
                String date = String.format(Locale.getDefault(), "%02d / %02d / %04d", day, month + 1, year);
                etDob.setText(date);
                tilDob.setError(null);

                // Calculate age
                Calendar dob = Calendar.getInstance();
                dob.set(year, month, day);
                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) age--;
                etAge.setText(String.valueOf(age));
            },
            today.get(Calendar.YEAR) - 10,   // default year (10-year-old)
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        );
        // Restrict: must be between 5 and 25 years old
        Calendar max = Calendar.getInstance();
        max.add(Calendar.YEAR, -5);
        Calendar min = Calendar.getInstance();
        min.add(Calendar.YEAR, -25);
        dlg.getDatePicker().setMaxDate(max.getTimeInMillis());
        dlg.getDatePicker().setMinDate(min.getTimeInMillis());
        dlg.show();
    }

    // ── Class → stream & subjects ──────────────────────────

    private void setupClassListener() {
        actClass.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            tilClass.setError(null);
            int classNum;
            try { classNum = Integer.parseInt(selected); } catch (Exception e) { return; }

            // Auto-fill previous class
            etPrevClass.setText(classNum > 6 ? "Class " + (classNum - 1) : "—");

            // Stream: only for 11–12
            if (classNum >= 11) {
                tilStream.setVisibility(View.VISIBLE);
                actStream.setText("", false);
                cgSubjects.removeAllViews();  // wait for stream selection
            } else {
                tilStream.setVisibility(View.GONE);
                actStream.setText("", false);
                buildSubjectChips(SUBJECTS_6_10);
            }
        });
    }

    private void setupStreamListener() {
        actStream.setOnItemClickListener((parent, view, position, id) -> {
            String stream = (String) parent.getItemAtPosition(position);
            switch (stream) {
                case "Science":  buildSubjectChips(SUBJECTS_11_SCIENCE);  break;
                case "Commerce": buildSubjectChips(SUBJECTS_11_COMMERCE); break;
                case "Arts":     buildSubjectChips(SUBJECTS_11_ARTS);     break;
            }
        });
    }

    private void buildSubjectChips(String[] subjects) {
        cgSubjects.removeAllViews();
        for (String subject : subjects) {
            Chip chip = new Chip(this);
            chip.setText(subject);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.reliance_blue_light);
            chip.setCheckedIconTintResource(R.color.reliance_blue);
            chip.setTextColor(getResources().getColor(R.color.text_primary, getTheme()));
            chip.setRippleColorResource(R.color.ripple_light);
            cgSubjects.addView(chip);
        }
    }

    // ── Marks → percentage ─────────────────────────────────

    private void setupMarksListener() {
        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) { recalcPercentage(); }
        };
        etMarksObtained.addTextChangedListener(tw);
        etMaxMarks.addTextChangedListener(tw);
    }

    private void recalcPercentage() {
        String obtStr = etMarksObtained.getText() != null
            ? etMarksObtained.getText().toString().trim() : "";
        String maxStr = etMaxMarks.getText() != null
            ? etMaxMarks.getText().toString().trim() : "";
        if (obtStr.isEmpty() || maxStr.isEmpty()) {
            etPercentage.setText("");
            return;
        }
        try {
            double obt = Double.parseDouble(obtStr);
            double max = Double.parseDouble(maxStr);
            if (max > 0) {
                etPercentage.setText(String.format(Locale.getDefault(), "%.1f %%", (obt / max) * 100));
            }
        } catch (NumberFormatException ignored) { }
    }

    // ── Validation ─────────────────────────────────────────

    private boolean validateForm() {
        boolean valid = true;

        // Name
        if (isEmpty(etName)) {
            tilName.setError("Full name is required");
            valid = false;
        } else {
            tilName.setError(null);
        }

        // Email (required — it is the login identifier)
        String email = textOf(etEmail);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address");
            valid = false;
        } else {
            tilEmail.setError(null);
        }

        // Username
        String username = textOf(etUsername);
        if (!USERNAME_RULE.matcher(username).matches()) {
            tilUsername.setError(getString(R.string.registration_error_username));
            valid = false;
        } else {
            tilUsername.setError(null);
        }

        // Password
        String password = passwordOf(etPassword);
        if (!PASSWORD_RULE.matcher(password).matches()) {
            tilPassword.setError(getString(R.string.registration_error_password));
            valid = false;
        } else {
            tilPassword.setError(null);
        }

        // Confirm password
        if (!password.equals(passwordOf(etConfirmPassword))) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        } else {
            tilConfirmPassword.setError(null);
        }

        // DOB
        if (isEmpty(etDob)) {
            tilDob.setError("Date of birth is required");
            valid = false;
        } else {
            tilDob.setError(null);
        }

        // Gender
        if (rgGender.getCheckedRadioButtonId() == -1) {
            showToast("Please select gender");
            valid = false;
        }

        // Address Line 1
        TextInputLayout tilAddress1 = findViewById(R.id.til_address1);
        if (isEmpty(etAddress1)) {
            tilAddress1.setError("Address is required");
            valid = false;
        } else {
            tilAddress1.setError(null);
        }

        // Parent name
        if (isEmpty(etParentName)) {
            tilParentName.setError("Parent name is required");
            valid = false;
        } else {
            tilParentName.setError(null);
        }

        // Parent mobile
        String parentMobile = etParentMobile.getText() != null
            ? etParentMobile.getText().toString().trim() : "";
        if (parentMobile.length() < 10) {
            tilParentMobile.setError("Valid 10-digit number required");
            valid = false;
        } else {
            tilParentMobile.setError(null);
        }

        // Class
        if (actClass.getText() == null || actClass.getText().toString().trim().isEmpty()) {
            tilClass.setError("Please select a class");
            valid = false;
        } else {
            tilClass.setError(null);
        }

        // School
        if (isEmpty(etSchool)) {
            tilSchool.setError("School name is required");
            valid = false;
        } else {
            tilSchool.setError(null);
        }

        // Board
        if (actBoard.getText() == null || actBoard.getText().toString().trim().isEmpty()) {
            tilBoard.setError("Please select a board");
            valid = false;
        } else {
            tilBoard.setError(null);
        }

        // At least one subject selected
        if (cgSubjects.getCheckedChipIds().isEmpty()) {
            showToast("Please select at least one subject");
            valid = false;
        }

        return valid;
    }

    // ── Submission ─────────────────────────────────────────

    /**
     * Creates the account, then signs in.
     *
     * <p>{@code POST /api/v1/auth/register} answers 201 with the new profile and sends a
     * verification email — it does <b>not</b> issue tokens. So a successful registration is
     * followed by a normal {@code login} call with the same credentials. That login succeeds
     * while the service runs with {@code app.security.require-email-verification=false}; once
     * verification is enforced it answers 403 and the student is asked to confirm the address
     * first, which {@link #showVerifyEmailDialog} explains.
     */
    private void submitRegistration() {
        final String username = textOf(etUsername);
        final String password = passwordOf(etPassword);

        setSubmitting(true);
        ApiClient.getAuthApi()
            .register(new AuthRequests.Register(
                username,
                textOf(etEmail),
                password,
                firstNamePart(),
                lastNamePart()))
            .enqueue(new ApiCallback<RegistrationResponse>() {
                @Override
                public void onSuccess(RegistrationResponse body) {
                    signIn(username, password);
                }

                @Override
                public void onError(ApiError error) {
                    setSubmitting(false);
                    showRegistrationError(error);
                }
            });
    }

    /** Signs the new student in so they land on the home screen already authenticated. */
    private void signIn(String username, String password) {
        ApiClient.getAuthApi()
            .login(new AuthRequests.Login(username, password))
            .enqueue(new ApiCallback<TokenResponse>() {
                @Override
                public void onSuccess(TokenResponse tokens) {
                    setSubmitting(false);
                    if (tokens != null && tokens.hasTokens()) {
                        SessionManager.getInstance(RegistrationActivity.this).saveSession(tokens);
                        showSuccessDialog();
                    } else {
                        showVerifyEmailDialog();
                    }
                }

                @Override
                public void onError(ApiError error) {
                    setSubmitting(false);
                    // The account exists either way; only the sign-in did not go through.
                    if (error.isForbidden()) {
                        showVerifyEmailDialog();
                    } else {
                        showCreatedButSignInFailedDialog(error.message());
                    }
                }
            });
    }

    /** Reports a 409 on the field that caused it, and a 400 on each field the server named. */
    private void showRegistrationError(ApiError error) {
        if (error.isTransport()) {
            showToast(error.message());
            return;
        }
        if (error.isConflict()) {
            String message = error.message();
            TextInputLayout target =
                message.toLowerCase(Locale.ROOT).contains("username") ? tilUsername : tilEmail;
            target.setError(message);
            target.requestFocus();
            showToast(message);
            return;
        }
        Map<String, String> fieldErrors = error.fieldErrors();
        if (!fieldErrors.isEmpty()) {
            applyFieldError(fieldErrors, "username", tilUsername);
            applyFieldError(fieldErrors, "email", tilEmail);
            applyFieldError(fieldErrors, "password", tilPassword);
            applyFieldError(fieldErrors, "firstName", tilName);
            applyFieldError(fieldErrors, "lastName", tilName);
        }
        showToast(error.message());
    }

    private void applyFieldError(Map<String, String> fieldErrors, String field, TextInputLayout layout) {
        String message = fieldErrors.get(field);
        if (message != null) {
            layout.setError(message);
        }
    }

    private void setSubmitting(boolean submitting) {
        btnSubmit.setEnabled(!submitting);
        btnSubmit.setText(submitting ? R.string.registration_submitting : R.string.registration_submit);
    }

    /**
     * The service stores one first and one last name, so the single full-name field is split
     * on the first space. Both parts stay within the 100-character server limit.
     */
    private String firstNamePart() {
        String fullName = textOf(etName);
        int space = fullName.indexOf(' ');
        return space > 0 ? fullName.substring(0, space).trim() : fullName;
    }

    private String lastNamePart() {
        String fullName = textOf(etName);
        int space = fullName.indexOf(' ');
        // Optional on the server, so an empty remainder is simply left out.
        return space > 0 ? fullName.substring(space + 1).trim() : null;
    }

    private String textOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private String passwordOf(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString() : "";
    }

    private String selectedRadioText(RadioGroup group) {
        int id = group.getCheckedRadioButtonId();
        if (id == -1) return "";
        RadioButton rb = group.findViewById(id);
        return rb != null ? rb.getText().toString() : "";
    }

    private List<String> selectedSubjects() {
        List<String> subjects = new ArrayList<>();
        for (int chipId : cgSubjects.getCheckedChipIds()) {
            Chip chip = cgSubjects.findViewById(chipId);
            if (chip != null) subjects.add(chip.getText().toString());
        }
        return subjects;
    }

    // ── Success dialog ─────────────────────────────────────

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.registration_success_title)
            .setMessage(R.string.registration_success_message)
            .setPositiveButton(R.string.registration_continue, (dialog, which) -> goHome())
            .setCancelable(false)
            .show();
    }

    /** The account was created but the address has to be confirmed before the first sign-in. */
    private void showVerifyEmailDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.registration_verify_title)
            .setMessage(getString(R.string.registration_verify_message, textOf(etEmail)))
            .setPositiveButton(R.string.registration_go_to_login, (dialog, which) -> goToLogin())
            .setCancelable(false)
            .show();
    }

    /** Registration went through but the follow-up sign-in did not; the login screen can retry. */
    private void showCreatedButSignInFailedDialog(String reason) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.registration_created_title)
            .setMessage(getString(R.string.registration_created_message, reason))
            .setPositiveButton(R.string.registration_go_to_login, (dialog, which) -> goToLogin())
            .setCancelable(false)
            .show();
    }

    private void goHome() {
        android.content.Intent intent = new android.content.Intent(this, HomeActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        android.content.Intent intent = new android.content.Intent(this, LoginActivity.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ── Helpers ────────────────────────────────────────────

    private boolean isEmpty(TextInputEditText et) {
        return et.getText() == null || et.getText().toString().trim().isEmpty();
    }

    private void showToast(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }
}
