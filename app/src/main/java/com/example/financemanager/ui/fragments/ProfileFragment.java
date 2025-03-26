package com.example.financemanager.ui.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.financemanager.R;
import com.example.financemanager.data.entities.User;
import com.example.financemanager.ui.viewmodels.TransactionViewModel;
import com.example.financemanager.ui.viewmodels.UserViewModel;
import com.example.financemanager.utils.CurrencyFormatter;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private UserViewModel userViewModel;
    private TransactionViewModel transactionViewModel;

    private CircleImageView ivProfile;
    private TextView tvChangePhoto, tvTotalTransactions, tvTotalIncome, tvTotalExpense, tvBalance;
    private TextInputEditText etName, etEmail;
    private Button btnSaveChanges, btnExportData;

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

        // Setup ViewModels
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Observe user data
        observeUserData();

        // Observe transactions for statistics
        observeTransactions();

        // Setup click listeners
        setupListeners();

        return view;
    }

    private void observeUserData() {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                etName.setText(user.getName());
                etEmail.setText(user.getEmail());
                tvBalance.setText(CurrencyFormatter.format(user.getTotalBalance()));

                // Set profile image if available
                selectedImagePath = user.getProfileImagePath();
                if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                    try {
                        Uri imageUri = Uri.parse(selectedImagePath);
                        ivProfile.setImageURI(imageUri);
                    } catch (Exception e) {
                        // If image loading fails, keep default image
                    }
                }
            } else {
                // Create default user if none exists
                User defaultUser = new User("User", "user@example.com", "", 0, 0, 0);
                userViewModel.insert(defaultUser);
            }
        });
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
        btnExportData.setOnClickListener(v -> {
            exportUserData();
        });
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

    private void exportUserData() {
        // This is a simplified implementation of data export
        // In a real app, you would:
        // 1. Generate a proper CSV or other format file with transaction data
        // 2. Use proper file handling with permissions
        // 3. Handle various edge cases

        try {
            // Get download directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            // Generate filename with timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File exportFile = new File(downloadsDir, "finance_export_" + timestamp + ".csv");

            // Create file
            FileOutputStream fos = new FileOutputStream(exportFile);

            // Write header
            String header = "Date,Type,Category,Title,Amount,Payment Method,Notes\n";
            fos.write(header.getBytes());

            // Get transactions
            transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
                if (transactions != null && !transactions.isEmpty()) {
                    try {
                        // Write transaction data
                        for (com.example.financemanager.data.entities.Transaction transaction : transactions) {
                            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(transaction.getDate());
                            String line = String.format("%s,%s,%s,\"%s\",%s,%s,\"%s\"\n",
                                    date,
                                    transaction.getType(),
                                    transaction.getCategory(),
                                    transaction.getTitle().replace("\"", "\"\""), // Escape quotes
                                    transaction.getAmount(),
                                    transaction.getPaymentMethod(),
                                    transaction.getNotes().replace("\"", "\"\"") // Escape quotes
                            );
                            fos.write(line.getBytes());
                        }

                        // Close file
                        fos.close();

                        // Show success message
                        Toast.makeText(requireContext(), "Data exported to Downloads folder", Toast.LENGTH_LONG).show();

                        // Open the file
                        Uri contentUri = FileProvider.getUriForFile(
                                requireContext(),
                                requireContext().getPackageName() + ".fileprovider",
                                exportFile);

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(contentUri, "text/csv");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "Open with"));

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "No transactions to export", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}