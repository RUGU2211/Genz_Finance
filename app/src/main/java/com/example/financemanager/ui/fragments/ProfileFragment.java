package com.example.financemanager.ui.fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PackageManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.financemanager.R;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.data.entities.User;
import com.example.financemanager.ui.viewmodels.AuthViewModel;
import com.example.financemanager.ui.viewmodels.TransactionViewModel;
import com.example.financemanager.ui.viewmodels.UserViewModel;
import com.example.financemanager.utils.CurrencyFormatter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int EXPORT_REQUEST_CODE = 101;

    private UserViewModel userViewModel;
    private TransactionViewModel transactionViewModel;
    private AuthViewModel authViewModel;

    private CircleImageView ivProfile;
    private TextView tvChangePhoto, tvTotalTransactions, tvTotalIncome, tvTotalExpense, tvBalance;
    private TextInputEditText etName, etEmail;
    private Button btnSaveChanges, btnExportData, btnLogout;

    private String selectedImagePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        ivProfile = view.findViewById(R.id.iv_profile);
        tvChangePhoto = view.findViewById(R.id.tv_change_photo);
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        tvTotalTransactions = view.findViewById(R.id.tv_total_transactions);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvBalance = view.findViewById(R.id.tv_balance);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
        btnExportData = view.findViewById(R.id.btn_export_data);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Setup ViewModels
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Observe user data
        observeUserData();

        // Observe transactions for statistics
        observeTransactions();

        // Setup click listeners
        setupListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // ... existing code ...

        btnExportData.setOnClickListener(v -> checkPermissionsAndExport());
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void observeUserData() {
        // Get current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // First check if user exists in Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // User exists in Firestore, update UI
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        updateUIWithUserData(user);
                    }
                } else {
                    // User doesn't exist in Firestore, create new user from Firebase Auth
                    createNewUserInFirestore(userId);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error fetching user data: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void createNewUserInFirestore(String userId) {
        // Get current Firebase user
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;
        
        // Create new user object
        User newUser = new User();
        newUser.setId(userId);
        newUser.setName(firebaseUser.getDisplayName() != null ? 
            firebaseUser.getDisplayName() : "User");
        newUser.setEmail(firebaseUser.getEmail() != null ? 
            firebaseUser.getEmail() : "");
        
        // Get profile photo URL if available (for Google sign-in)
        if (firebaseUser.getPhotoUrl() != null) {
            newUser.setProfileImagePath(firebaseUser.getPhotoUrl().toString());
        }
        
        // Save to Firestore
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(newUser)
            .addOnSuccessListener(aVoid -> {
                // Update UI with new user data
                updateUIWithUserData(newUser);
                Toast.makeText(requireContext(), "Profile created successfully", 
                    Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Error creating profile: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateUIWithUserData(User user) {
        // Update text fields
        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        tvBalance.setText(CurrencyFormatter.format(user.getTotalBalance()));
        
        // Set profile image if available
        selectedImagePath = user.getProfileImagePath();
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            try {
                if (selectedImagePath.startsWith("http")) {
                    // Load image from URL using Glide
                    Glide.with(this)
                        .load(selectedImagePath)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(ivProfile);
                } else {
                    // Load local image
                    Uri imageUri = Uri.parse(selectedImagePath);
                    ivProfile.setImageURI(imageUri);
                }
            } catch (Exception e) {
                Log.e("ProfileFragment", "Error loading profile image: " + e.getMessage());
                ivProfile.setImageResource(R.drawable.default_profile);
            }
        } else {
            ivProfile.setImageResource(R.drawable.default_profile);
        }
    }

    private void observeTransactions() {
        // Count total transactions
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                tvTotalTransactions.setText(String.valueOf(transactions.size()));
            } else {
                tvTotalTransactions.setText("0");
            }
        });

        // Get income total
        transactionViewModel.getTotalIncome().observe(getViewLifecycleOwner(), totalIncome -> {
            if (totalIncome != null) {
                tvTotalIncome.setText(CurrencyFormatter.format(totalIncome));
            } else {
                tvTotalIncome.setText(CurrencyFormatter.format(0));
            }
        });

        // Get expense total
        transactionViewModel.getTotalExpense().observe(getViewLifecycleOwner(), totalExpense -> {
            if (totalExpense != null) {
                tvTotalExpense.setText(CurrencyFormatter.format(totalExpense));
            } else {
                tvTotalExpense.setText(CurrencyFormatter.format(0));
            }
        });
    }

    private void setupListeners() {
        // Change photo
        tvChangePhoto.setOnClickListener(v -> {
            openImagePicker();
        });

        // Save changes
        btnSaveChanges.setOnClickListener(v -> {
            saveUserProfile();
        });

        // Export data
        btnExportData.setOnClickListener(v -> checkPermissionsAndExport());
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            selectedImagePath = imageUri.toString();
            ivProfile.setImageURI(imageUri);
        }
    }

    private void saveUserProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        // Get the current user value directly instead of observing
        User user = userViewModel.getUser().getValue();
        if (user != null) {
            user.setName(name);
            user.setEmail(email);
            user.setProfileImagePath(selectedImagePath);

            userViewModel.update(user);
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                authViewModel.signOut();
                navigateToLogin();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.loginFragment);
    }

    private void checkPermissionsAndExport() {
        if (ContextCompat.checkSelfPermission(requireContext(), 
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                PERMISSION_REQUEST_CODE);
        } else {
            exportUserData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
        @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportUserData();
            } else {
                Toast.makeText(requireContext(), 
                    "Storage permission is required to export data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void exportUserData() {
        try {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadDir.exists()) {
                downloadDir.mkdirs();
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String filename = "finance_data_" + timestamp + ".csv";
            File file = new File(downloadDir, filename);

            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.append("Date,Type,Category,Amount,Description\n");

                // Get transactions from Firestore
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("transactions")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        try {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Transaction transaction = document.toObject(Transaction.class);
                                writer.append(String.format("%s,%s,%s,%.2f,%s\n",
                                    transaction.getDate(),
                                    transaction.getType(),
                                    transaction.getCategory(),
                                    transaction.getAmount(),
                                    transaction.getDescription()));
                            }
                            writer.flush();
                            openExportedFile(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), 
                                "Error writing data: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), 
                            "Error fetching data: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), 
                    "Error creating file: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), 
                "Error exporting data: " + e.getMessage(), 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void openExportedFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(requireContext(),
            requireContext().getPackageName() + ".provider",
            file);
        intent.setDataAndType(uri, "text/csv");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
            Toast.makeText(requireContext(), 
                "Data exported successfully to Downloads folder", 
                Toast.LENGTH_LONG).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(requireContext(), 
                "No app found to open CSV file", 
                Toast.LENGTH_SHORT).show();
        }
    }
}