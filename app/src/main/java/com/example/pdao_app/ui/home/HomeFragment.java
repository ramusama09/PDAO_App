package com.example.pdao_app.ui.home;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.pdao_app.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.pdao_app.databinding.DialogShowIdBinding;
import com.example.pdao_app.databinding.DialogUserInfoBinding;
import com.example.pdao_app.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();




        Button showDialogBtn = binding.showDialogButton;
        showDialogBtn.setOnClickListener(v -> showCustomDialog());

        Button showIdBtn = binding.showIdButton;
        showIdBtn.setOnClickListener(v -> showIDDialog());

        SwipeRefreshLayout swipeRefreshLayout = binding.swipeRefreshLayout;

        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchUserInfo();
            fetchLatestTransaction(root);

            // Simulate loading for 1 second (or stop when data is actually loaded)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
            }, 1000);
        });

        fetchUserInfo();
        fetchLatestTransaction(root);

        return root;
    }

    private void fetchUserInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        TextView nameTextView = binding.userName;
        TextView contactTextView = binding.userContact;
        TextView emailTextView = binding.userEmail;
        TextView disabilityTextView = binding.userDisability;
        TextView idStatusTextView = binding.userIdStatus;
        Button idButton = binding.showIdButton;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstName").getValue(String.class);
                String contactNum = snapshot.child("contactNumber").getValue(String.class);
                String disability = snapshot.child("disabilityType").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                nameTextView.setText("Welcome, " + (firstName != null ? firstName : "Unknown") + "!");
                contactTextView.setText(contactNum != null ? contactNum : "Unknown");
                disabilityTextView.setText(disability != null ? disability : "Unknown");
                emailTextView.setText(email != null ? email : "Unknown");

                // -------- ID CARD VALIDATION LOGIC --------
                DataSnapshot idCardSnapshot = snapshot.child("idCards");
                if (!idCardSnapshot.exists()) {
                    idStatusTextView.setText("No ID card found.");
                    idButton.setVisibility(View.GONE);
                    return;
                }

                String expirationDateStr = idCardSnapshot.child("expirationDate").getValue(String.class);
                String pwdIdNo = idCardSnapshot.child("pwdIdNo").getValue(String.class);

                if (expirationDateStr == null || expirationDateStr.isEmpty()) {
                    idStatusTextView.setText("No expiration date found.");
                    return;
                }

                if (pwdIdNo == null || pwdIdNo.isEmpty()) {
                    idStatusTextView.setText("PWD ID Number not found.");
                    return;
                }

                // Parse expiration date and calculate days remaining
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date expirationDate = inputFormat.parse(expirationDateStr);
                    Date currentDate = new Date();

                    if (expirationDate != null) {
                        long diffMillis = expirationDate.getTime() - currentDate.getTime();
                        long daysRemaining = diffMillis / (1000 * 60 * 60 * 24);

                        if (daysRemaining < 0) {
                            idStatusTextView.setText("Status: Expired\nExpired " + Math.abs(daysRemaining) + " day(s) ago");
                            idButton.setVisibility(View.GONE); // Optional: hide button if expired
                        } else {
                            idStatusTextView.setText("Status: Valid\n" + daysRemaining + " day(s) remaining");
                            idButton.setVisibility(View.VISIBLE);
                        }
                    } else {
                        idStatusTextView.setText("Invalid expiration date format.");
                    }
                } catch (Exception e) {
                    idStatusTextView.setText("Failed to parse expiration date.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                nameTextView.setText("Error loading user");
                contactTextView.setText("");
                idStatusTextView.setText("Error fetching ID card data");
            }
        });
    }





    private void showCustomDialog() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        // Inflate the dialog layout manually
        DialogUserInfoBinding dialogBinding = DialogUserInfoBinding.inflate(getLayoutInflater());

        // Create a Dialog instance
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove default title

        // Set the content view to the inflated binding root
        dialog.setContentView(dialogBinding.getRoot());

        // Set transparent background so rounded corners work (assuming your root layout drawable has rounded corners)
        if (dialog.getWindow() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);  // 90% of screen width
            int height = (int) (displayMetrics.heightPixels * 0.85); // 85% of screen height

            dialog.getWindow().setLayout(
                    width,
                    height
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }


        // Setup sex spinner
        Spinner sexSpinner = dialogBinding.sexSpinner;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.spinner_layout,
                Arrays.asList("Male", "Female")
        );
        adapter.setDropDownViewResource(R.layout.spinner_sex_item);
        sexSpinner.setAdapter(adapter);

        // Load user data and pre-fill fields
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dialogBinding.firstName.setText(snapshot.child("firstName").getValue(String.class));
                dialogBinding.middleName.setText(snapshot.child("middleName").getValue(String.class));
                dialogBinding.lastName.setText(snapshot.child("lastName").getValue(String.class));
                dialogBinding.suffix.setText(snapshot.child("suffix").getValue(String.class));
                dialogBinding.contactNum.setText(snapshot.child("contactNumber").getValue(String.class));
                dialogBinding.birthDate.setText(snapshot.child("birthDate").getValue(String.class));
                dialogBinding.address1.setText(snapshot.child("addressLine1").getValue(String.class));
                dialogBinding.address2.setText(snapshot.child("addressLine2").getValue(String.class));
                dialogBinding.emergencyName.setText(snapshot.child("emergencyContactName").getValue(String.class));
                dialogBinding.emergencyNumber.setText(snapshot.child("emergencyContactNumber").getValue(String.class));

                // Set spinner selection
                String sexValue = snapshot.child("sex").getValue(String.class);
                if (sexValue != null) {
                    int spinnerPosition = adapter.getPosition(sexValue);
                    sexSpinner.setSelection(spinnerPosition);
                }

                // Calculate and display age if birthdate present
                String birthDateStr = snapshot.child("birthDate").getValue(String.class);
                if (birthDateStr != null && !birthDateStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "PH"));
                        Date birthDate = sdf.parse(birthDateStr);
                        Calendar dob = Calendar.getInstance();
                        dob.setTime(birthDate);

                        Calendar today = Calendar.getInstance();
                        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                            age--;
                        }
                        dialogBinding.ageText.setText(String.valueOf(age));
                    } catch (Exception e) {
                        dialogBinding.ageText.setText("");
                    }
                }

                // DatePicker for birthDate field
                dialogBinding.birthDate.setOnClickListener(v -> {
                    final Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePicker = new DatePickerDialog(
                            requireContext(),
                            (view, selectedYear, selectedMonth, selectedDay) -> {
                                String dateStr = String.format(new Locale("en", "PH"), "%04d-%02d-%02d",
                                        selectedYear, selectedMonth + 1, selectedDay);
                                dialogBinding.birthDate.setText(dateStr);

                                Calendar dob = Calendar.getInstance();
                                dob.set(selectedYear, selectedMonth, selectedDay);
                                Calendar today = Calendar.getInstance();
                                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                                    age--;
                                }
                                dialogBinding.ageText.setText(String.valueOf(age));
                            },
                            year, month, day
                    );
                    datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
                    datePicker.show();
                });

                // Save button click listener with validation
                dialogBinding.saveBtn.setOnClickListener(v -> {
                    // Validation (same as before, required fields only)
                    if (dialogBinding.firstName.getText().toString().trim().isEmpty()) {
                        dialogBinding.firstName.setError("First name is required");
                        dialogBinding.firstName.requestFocus();
                        return;
                    }
                    if (dialogBinding.lastName.getText().toString().trim().isEmpty()) {
                        dialogBinding.lastName.setError("Last name is required");
                        dialogBinding.lastName.requestFocus();
                        return;
                    }
                    if (dialogBinding.contactNum.getText().toString().trim().isEmpty()) {
                        dialogBinding.contactNum.setError("Contact Number is required");
                        dialogBinding.contactNum.requestFocus();
                        return;
                    }
                    if (dialogBinding.birthDate.getText().toString().trim().isEmpty()) {
                        dialogBinding.birthDate.setError("Birth date is required");
                        dialogBinding.birthDate.requestFocus();
                        return;
                    }
                    if (dialogBinding.sexSpinner.getSelectedItem() == null ||
                            dialogBinding.sexSpinner.getSelectedItem().toString().trim().isEmpty()) {
                        Toast.makeText(getContext(), "Please select sex", Toast.LENGTH_SHORT).show();
                        dialogBinding.sexSpinner.requestFocus();
                        return;
                    }
                    if (dialogBinding.address1.getText().toString().trim().isEmpty()) {
                        dialogBinding.address1.setError("Address is required");
                        dialogBinding.address1.requestFocus();
                        return;
                    }
                    if (dialogBinding.address2.getText().toString().trim().isEmpty()) {
                        dialogBinding.address2.setError("Address is required");
                        dialogBinding.address2.requestFocus();
                        return;
                    }
                    if (dialogBinding.emergencyName.getText().toString().trim().isEmpty()) {
                        dialogBinding.emergencyName.setError("Emergency contact name is required");
                        dialogBinding.emergencyName.requestFocus();
                        return;
                    }
                    if (dialogBinding.emergencyNumber.getText().toString().trim().isEmpty()) {
                        dialogBinding.emergencyNumber.setError("Emergency contact number is required");
                        dialogBinding.emergencyNumber.requestFocus();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("firstName", dialogBinding.firstName.getText().toString().trim());
                    updates.put("middleName", dialogBinding.middleName.getText().toString().trim()); // optional
                    updates.put("lastName", dialogBinding.lastName.getText().toString().trim());
                    updates.put("suffix", dialogBinding.suffix.getText().toString().trim()); // optional
                    updates.put("contactNumber", dialogBinding.contactNum.getText().toString().trim());
                    updates.put("birthDate", dialogBinding.birthDate.getText().toString().trim());
                    updates.put("age", dialogBinding.ageText.getText().toString().trim());
                    updates.put("sex", dialogBinding.sexSpinner.getSelectedItem().toString());
                    updates.put("addressLine1", dialogBinding.address1.getText().toString().trim());
                    updates.put("addressLine2", dialogBinding.address2.getText().toString().trim()); // optional
                    updates.put("emergencyContactName", dialogBinding.emergencyName.getText().toString().trim());
                    updates.put("emergencyContactNumber", dialogBinding.emergencyNumber.getText().toString().trim());

                    userRef.updateChildren(updates)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    dialog.dismiss();
                });

                dialogBinding.dialogCloseBtn.setOnClickListener(v -> dialog.dismiss());

                // Show the dialog after data is loaded and listeners set
                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showIDDialog() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference idCardsRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("idCards");

        DialogShowIdBinding dialogBinding = DialogShowIdBinding.inflate(getLayoutInflater());

        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogBinding.getRoot());

        if (dialog.getWindow() != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = (int) (displayMetrics.widthPixels * 0.9);
            int height = (int) (displayMetrics.heightPixels * 0.7);

            dialog.getWindow().setLayout(width, height);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Load frontId and backId images
        idCardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String frontIdUrl = snapshot.child("frontID").getValue(String.class);
                String backIdUrl = snapshot.child("backID").getValue(String.class);

                if (frontIdUrl != null && !frontIdUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(frontIdUrl)
                            .placeholder(R.drawable.placeholder_id)
                            .error(R.drawable.error_image)
                            .into(dialogBinding.frontIdImage);

                    dialogBinding.frontIdImage.setOnClickListener(v -> showImagePopup(frontIdUrl));
                }

                if (backIdUrl != null && !backIdUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(backIdUrl)
                            .placeholder(R.drawable.placeholder_id)
                            .error(R.drawable.error_image)
                            .into(dialogBinding.backIdImage);

                    dialogBinding.backIdImage.setOnClickListener(v -> showImagePopup(backIdUrl));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to load ID images", Toast.LENGTH_SHORT).show();
            }
        });

        dialogBinding.dialogIDCloseBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showImagePopup(String imageUrl) {
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_image);

        ImageView popupImageView = dialog.findViewById(R.id.popup_image_view);

        // Apply rotation
        popupImageView.setRotation(90f);

        // Get screen dimensions and manually swap width and height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Swap dimensions to fit landscape orientation
        ViewGroup.LayoutParams layoutParams = popupImageView.getLayoutParams();
        layoutParams.width = screenHeight;
        layoutParams.height = screenWidth;
        popupImageView.setLayoutParams(layoutParams);

        Glide.with(requireContext())
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_id)
                .error(R.drawable.error_image)
                .into(popupImageView);

        popupImageView.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }





    public void fetchLatestTransaction(View root) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference transRef = FirebaseDatabase.getInstance()
                .getReference("transactions").child(uid);

        // Find views from the transaction card
        TextView transactionDate = root.findViewById(R.id.transaction_date);
        TextView transactionStore = root.findViewById(R.id.transaction_store);
        TextView transactionAddress = root.findViewById(R.id.text_transaction_address);
        TextView transactionDesc = root.findViewById(R.id.transaction_desc);
        View transactionCard = root.findViewById(R.id.transaction_card);

        // Hide card initially
        transactionCard.setVisibility(GONE);

        transRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot latestSnapshot = null;
                    String latestTimestamp = "";

                    for (DataSnapshot transactionSnapshot : snapshot.getChildren()) {
                        String key = transactionSnapshot.getKey(); // e.g., "20250528110403"
                        if (key != null && key.compareTo(latestTimestamp) > 0) {
                            latestTimestamp = key;
                            latestSnapshot = transactionSnapshot;
                        }
                    }

                    if (latestSnapshot != null) {
                        String address = latestSnapshot.child("address").getValue(String.class);
                        String description = latestSnapshot.child("description").getValue(String.class);
                        String storeName = latestSnapshot.child("storeName").getValue(String.class);

                        // Format and set values
                        transactionDate.setText(formatTimestamp(latestTimestamp));
                        transactionStore.setText(storeName != null ? storeName : "N/A");
                        transactionAddress.setText(address != null ? address : "N/A");
                        transactionDesc.setText(description != null ? description : "N/A");

                        transactionCard.setVisibility(VISIBLE); // show card now that data is loaded
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Transaction", "Error fetching transactions", error.toException());
            }
        });
    }

    private String formatTimestamp(String raw) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(raw);
            return outputFormat.format(date);
        } catch (java.text.ParseException e) {
            return raw; // fallback to raw if parsing fails
        }
    }







    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
