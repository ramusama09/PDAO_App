package com.example.pdao_app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.pdao_app.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.pdao_app.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView nameTextView = binding.userName;
        TextView contactTextView = binding.userContact;
        TextView emailTextView = binding.userEmail;
        TextView disabilityTextView = binding.userDisability;

        // Fetch current user ID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        Button showDialogBtn = binding.showDialogButton;
        showDialogBtn.setOnClickListener(v -> showCustomDialog());


        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Safely extract each field manually
                String firstName = snapshot.child("firstName").getValue(String.class);
                firstName = (firstName != null) ? firstName : "Unknown";

                String contactNum = snapshot.child("contactNumber").getValue(String.class);
                contactNum = (contactNum != null) ? contactNum : "Unknown";

                String disability = snapshot.child("disabilityType").getValue(String.class);
                disability = (disability != null) ? disability : "Unknown";

                String email = snapshot.child("email").getValue(String.class);
                email = (email != null) ? email : "Unknown";



                nameTextView.setText("Welcome, " + firstName + "!");
                contactTextView.setText(contactNum);
                disabilityTextView.setText(disability);
                emailTextView.setText(email);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                nameTextView.setText("Error loading user");
                contactTextView.setText("");
            }
        });

        return root;
    }

    private void showCustomDialog() {
        // Inflate the dialog layout using the fragment's inflater
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_info, null);

        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button closeButton = dialogView.findViewById(R.id.dialog_close_btn);

        // Optional: dynamic content
        dialogMessage.setText("This is a custom dialog inside a Fragment using ViewBinding.");

        // Build and show the dialog
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
