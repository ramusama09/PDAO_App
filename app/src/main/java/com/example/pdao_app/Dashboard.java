package com.example.pdao_app;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.pdao_app.databinding.DialogChangeEmailBinding;
import com.example.pdao_app.databinding.DialogChangePasswordBinding;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pdao_app.databinding.ActivityDashboardBinding;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Dashboard extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));


        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarDashboard.toolbar);

        // Hide default navigation (hamburger) icon on the left
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);


        //Uncomment this if you need bottom-right button across the dashboard
        /*binding.appBarDashboard.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });*/
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        NavigationView navView = findViewById(R.id.nav_view);

        // Get header view
        View headerView = navigationView.getHeaderView(0);

        // Reference TextViews in header
        TextView pwdNameTextView = headerView.findViewById(R.id.pwdNameTextView);
        TextView pwdIdTextView = headerView.findViewById(R.id.pwdIdTextView);
        ImageView profileImageView = headerView.findViewById(R.id.imageView);

        // Fetch and display user info from Firebase
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Build and show full name
                String firstName = snapshot.child("firstName").getValue(String.class);
                String middleName = snapshot.child("middleName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String suffix = snapshot.child("suffix").getValue(String.class);

                StringBuilder fullName = new StringBuilder();
                if (firstName != null) fullName.append(firstName).append(" ");
                if (middleName != null && !middleName.isEmpty()) fullName.append(middleName).append(" ");
                if (lastName != null) fullName.append(lastName).append(" ");
                if (suffix != null && !suffix.isEmpty()) fullName.append(suffix);

                pwdNameTextView.setText(fullName != null ? fullName : "Unknown");

                // Get pwdId from nested "idCards" node
                String pwdId = snapshot.child("idCards").child("pwdIdNo").getValue(String.class);
                pwdIdTextView.setText(pwdId != null ? "ID No. " + pwdId : "No record of ID No.");

                DatabaseReference idCardsRef = userRef.child("idCards");

                idCardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String photoIdUrl = snapshot.child("photoID").getValue(String.class);

                        if (photoIdUrl != null && !photoIdUrl.isEmpty()) {
                            Glide.with(Dashboard.this)  // Use the Activity context here
                                    .load(photoIdUrl)
                                    .placeholder(R.drawable.placeholder_id)
                                    .error(R.drawable.error_image)
                                    .into(profileImageView);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(Dashboard.this, "Failed to load ID images", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                pwdNameTextView.setText("Error");
                pwdIdTextView.setText("");
            }
        });




        // Inflate footer layout containing the button
        View footerView = getLayoutInflater().inflate(R.layout.nav_logout, navView, false);
        navView.addView(footerView);

        // Setup the button click listener
        Button footerBtn = footerView.findViewById(R.id.nav_footer_logout);
        footerBtn.setOnClickListener(v -> {

            // Your logout or other action here
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        //Change email
        Button changeEmailBtn = footerView.findViewById(R.id.nav_footer_change_email);
        changeEmailBtn.setOnClickListener(v -> showChangeEmailDialog());

        //Change password
        Button changePassBtn = footerView.findViewById(R.id.nav_footer_change_pass);
        changePassBtn.setOnClickListener(v -> showChangePasswordDialog());





        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery/*, R.id.nav_aboutyou*/) //Uncomment if you need more menu
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Set toolbar title to the label defined in navigation graph
            getSupportActionBar().setTitle(destination.getLabel());
        });
        //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_dashboard);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void showChangeEmailDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DialogChangeEmailBinding dialogBinding = DialogChangeEmailBinding.inflate(getLayoutInflater());
        Dialog dialog = new Dialog(Dashboard.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Prefill current email
        String currentEmail = user.getEmail();
        if (currentEmail != null) {
            dialogBinding.editEmail.setText(currentEmail);
        }

        // Password visibility toggles
        setupPasswordToggle(dialogBinding.editCurrentPassword, dialogBinding.toggleCurrentPassword); // New

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnUpdate.setOnClickListener(v -> {
            String newEmail = dialogBinding.editEmail.getText().toString().trim();
            String currentPassword = dialogBinding.editCurrentPassword.getText().toString().trim();

            if (currentPassword.isEmpty()) {
                dialogBinding.editCurrentPassword.setError("Current password required");
                dialogBinding.editCurrentPassword.requestFocus();
                return;
            }

            if (newEmail.isEmpty()) {
                dialogBinding.editEmail.setError("Email required");
                dialogBinding.editEmail.requestFocus();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);

            // Re-authenticate first
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        user.verifyBeforeUpdateEmail(newEmail)
                                .addOnSuccessListener(unused1 -> {
                                    // Verification email sent, update your own database email field
                                    userRef.child("email").setValue(newEmail)
                                            .addOnSuccessListener(aVoid -> {
                                                showEmailChangedDialog(newEmail);
                                                dialog.dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(Dashboard.this,
                                                        "Failed to update email in database: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });

                                    // Optionally update password here or somewhere else after this
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Dashboard.this,
                                            "Email update failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Dashboard.this,
                                "You have entered incorrect password!",
                                Toast.LENGTH_SHORT).show();
                    });

        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DialogChangePasswordBinding dialogBinding = DialogChangePasswordBinding.inflate(getLayoutInflater());
        Dialog dialog = new Dialog(Dashboard.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        setupPasswordToggle(dialogBinding.editCurrentPassword, dialogBinding.toggleCurrentPassword);
        setupPasswordToggle(dialogBinding.editNewPassword, dialogBinding.toggleNewPassword);
        setupPasswordToggle(dialogBinding.editConfirmPassword, dialogBinding.toggleConfirmPassword);

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnUpdate.setOnClickListener(v -> {
            String currentPassword = dialogBinding.editCurrentPassword.getText().toString().trim();
            String newPassword = dialogBinding.editNewPassword.getText().toString().trim();
            String confirmPassword = dialogBinding.editConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty()) {
                dialogBinding.editNewPassword.setError("Password required");
                dialogBinding.editNewPassword.requestFocus();
                return;
            }

            if (!isPasswordStrong(newPassword)) {
                dialogBinding.editNewPassword.setError("Password must be 8+ characters with uppercase, lowercase, number, and special character");
                dialogBinding.editNewPassword.requestFocus();
                return;
            }


            if (!newPassword.equals(confirmPassword)) {
                dialogBinding.editConfirmPassword.setError("Passwords do not match");
                dialogBinding.editConfirmPassword.requestFocus();
                return;
            }

            String currentEmail = user.getEmail();
            if (currentEmail == null) {
                Toast.makeText(Dashboard.this, "User email not found", Toast.LENGTH_SHORT).show();
                return;
            }

            AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);

            user.reauthenticate(credential)
                    .addOnSuccessListener(unused -> {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(unused1 -> {

                                    dialog.dismiss();
                                    dialog.dismiss();
                                    showPasswordChangedDialog();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Dashboard.this,
                                            "Password update failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Dashboard.this,
                                "You have entered incorrect password!",
                                Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private boolean isPasswordStrong(String password) {
        // Regex explanation:
        // (?=.*[0-9])       - at least one digit
        // (?=.*[a-z])       - at least one lowercase letter
        // (?=.*[A-Z])       - at least one uppercase letter
        // (?=.*[@#$%^&+=!]) - at least one special character
        // .{8,}             - minimum 8 characters
        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordPattern);
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

    private void showPasswordChangedDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_password_change, null);
        Dialog confirmDialog = new Dialog(this);
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmDialog.setContentView(view);

        if (confirmDialog.getWindow() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.85);
            confirmDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        Button btnOkay = view.findViewById(R.id.btn_okay);
        btnOkay.setOnClickListener(v -> confirmDialog.dismiss());

        confirmDialog.show();
    }

    private void showEmailChangedDialog(String newEmail) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_email_change, null);
        Dialog confirmDialog = new Dialog(this);
        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        confirmDialog.setContentView(view);

        if (confirmDialog.getWindow() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.85);
            confirmDialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMessage = view.findViewById(R.id.tv_verification_message);
        tvMessage.setText("A verification email has been sent to:\n" + newEmail);

        Button btnOk = view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> logoutAndRedirect());

        // Handle dialog dismiss by logging out too
        confirmDialog.setOnDismissListener(dialog -> logoutAndRedirect());

        confirmDialog.show();
    }

    private void logoutAndRedirect() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Dashboard.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
        startActivity(intent);
        finish(); // Close Dashboard
    }









    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_drawer_toggle) {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}