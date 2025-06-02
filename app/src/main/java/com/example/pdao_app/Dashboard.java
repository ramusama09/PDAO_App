package com.example.pdao_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
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

import com.google.firebase.auth.FirebaseAuth;
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


        binding.appBarDashboard.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
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
        Button footerBtn = footerView.findViewById(R.id.nav_footer_button);
        footerBtn.setOnClickListener(v -> {

            // Your logout or other action here
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });




        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_aboutyou)
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