package com.example.pdao_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText email, password, confirmPassword;
    private EditText firstName, middleName, lastName, suffix;
    private EditText contactNum, address1, address2;
    private EditText birthDate, emergencyName, emergencyNumber;
    private Spinner sexSpinner;
    private TextView ageText;
    private Button signUpButton;
    private ImageView togglePasswordIcon, toggleConfirmPasswordIcon;
    private LinearLayout backButton;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    private int userAge = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);

        togglePasswordIcon = findViewById(R.id.toggle_password_icon);
        toggleConfirmPasswordIcon = findViewById(R.id.toggle_confirm_password_icon);

        firstName = findViewById(R.id.first_name);
        middleName = findViewById(R.id.middle_name);
        lastName = findViewById(R.id.last_name);
        suffix = findViewById(R.id.suffix);
        contactNum = findViewById(R.id.contactNum);
        address1 = findViewById(R.id.address1);
        address2 = findViewById(R.id.address2);
        birthDate = findViewById(R.id.birth_date);
        ageText = findViewById(R.id.age_text);
        sexSpinner = findViewById(R.id.sex_spinner);
        emergencyName = findViewById(R.id.emergency_name);
        emergencyNumber = findViewById(R.id.emergency_number);
        signUpButton = findViewById(R.id.signup_button);
        backButton = findViewById(R.id.backBtn);

        // Setup spinner options for sex
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_layout,
                new String[]{"Male", "Female"}
        );
        adapter.setDropDownViewResource(R.layout.spinner_sex_item);
        sexSpinner.setAdapter(adapter);

        // Setup password visibility toggle
        setupPasswordToggle(password, togglePasswordIcon);
        setupPasswordToggle(confirmPassword, toggleConfirmPasswordIcon);

        // Setup birth date picker
        birthDate.setOnClickListener(v -> showDatePicker());

        // Sign-up button click listener
        signUpButton.setOnClickListener(v -> registerUser());

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        int yearNow = today.get(Calendar.YEAR);
        int monthNow = today.get(Calendar.MONTH);
        int dayNow = today.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);

                    String formattedDate = (month + 1) + "/" + dayOfMonth + "/" + year;
                    birthDate.setText(formattedDate);
                    birthDate.setError(null);

                    int age = yearNow - year;
                    if (month > monthNow || (month == monthNow && dayOfMonth > dayNow)) {
                        age--;
                    }

                    userAge = age;
                    ageText.setText(String.valueOf(userAge));
                },
                yearNow, monthNow, dayNow
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }



    private void registerUser() {
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = confirmPassword.getText().toString().trim();
        String firstNameInput = firstName.getText().toString().trim();
        String lastNameInput = lastName.getText().toString().trim();
        String contactNumberInput = contactNum.getText().toString().trim();
        String address1Input = address1.getText().toString().trim();
        String address2Input = address2.getText().toString().trim();
        String birthDateInput = birthDate.getText().toString().trim();
        String emergencyNameInput = emergencyName.getText().toString().trim();
        String emergencyNumberInput = emergencyNumber.getText().toString().trim();


        if (TextUtils.isEmpty(firstNameInput)) {
            firstName.setError("First name is required");
            firstName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(lastNameInput)) {
            lastName.setError("Last name is required");
            lastName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contactNumberInput)) {
            contactNum.setError("Contact number is required");
            contactNum.requestFocus();
            return;
        }

        if (!isValidPhoneNumber(contactNumberInput)) {
            contactNum.setError("Enter a valid contact number (e.g. 09XXXXXXXXX)");
            contactNum.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address1Input)) {
            address1.setError("Address Line 1 is required");
            address1.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address2Input)) {
            address2.setError("Address Line 2 is required");
            address2.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(birthDateInput)) {
            birthDate.setError("Birth date is required");
            birthDate.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(emergencyNameInput)) {
            emergencyName.setError("Emergency contact name is required");
            emergencyName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(emergencyNumberInput)) {
            emergencyNumber.setError("Emergency contact number is required");
            emergencyNumber.requestFocus();
            return;
        }

        if (!isValidPhoneNumber(emergencyNumberInput)) {
            emergencyNumber.setError("Enter a valid emergency number (e.g. 09XXXXXXXXX)");
            emergencyNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(emailInput)) {
            email.setError("Email is required");
            email.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(passwordInput)) {
            password.setError("Password is required");
            password.requestFocus();
            return;
        }

        if (passwordInput.length() < 6) {
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
            return;
        }

        if (!passwordInput.equals(confirmPasswordInput)) {
            confirmPassword.setError("Passwords do not match");
            confirmPassword.requestFocus();
            return;
        }

        if (!isPasswordStrong(passwordInput)) {
            password.setError("Must contain uppercase, lowercase, number, and special character");
            password.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserProfile(user);
                    } else {
                        email.setError("Sign up failed: " + task.getException().getMessage());
                        email.requestFocus();
                    }
                });
    }



    private boolean isPasswordStrong(String password) {
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordPattern);
    }

    private boolean isValidPhoneNumber(String number) {
        return number.matches("^09\\d{9}$");
    }

    private void setupPasswordToggle(EditText passwordField, ImageView toggleIcon) {
        toggleIcon.setOnClickListener(v -> {
            if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleIcon.setImageResource(R.drawable.ic_hide_dark_green);
            } else {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleIcon.setImageResource(R.drawable.ic_show_dark_green);
            }
            passwordField.setSelection(passwordField.getText().length());
        });
    }

    private void saveUserProfile(FirebaseUser user) {
        if (user == null) return;

        String uid = user.getUid();

        Map<String, Object> profile = new HashMap<>();
        profile.put("email", email.getText().toString().trim());
        profile.put("firstName", firstName.getText().toString().trim());
        profile.put("middleName", middleName.getText().toString().trim());
        profile.put("lastName", lastName.getText().toString().trim());
        profile.put("suffix", suffix.getText().toString().trim());
        profile.put("contactNumber", contactNum.getText().toString().trim());
        profile.put("addressLine1", address1.getText().toString().trim());
        profile.put("addressLine2", address2.getText().toString().trim());
        profile.put("birthDate", birthDate.getText().toString().trim());
        profile.put("age", userAge);
        profile.put("sex", sexSpinner.getSelectedItem().toString());
        profile.put("emergencyContactName", emergencyName.getText().toString().trim());
        profile.put("emergencyContactNumber", emergencyNumber.getText().toString().trim());

        dbRef.child("users").child(uid)
                .setValue(profile)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
