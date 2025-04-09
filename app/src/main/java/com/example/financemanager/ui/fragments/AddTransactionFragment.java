package com.example.financemanager.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.financemanager.R;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.ui.viewmodels.TransactionViewModel;
import com.example.financemanager.ui.viewmodels.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class AddTransactionFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private UserViewModel userViewModel;
    private TextInputLayout amountLayout;
    private TextInputLayout titleLayout;
    private TextInputLayout descriptionLayout;
    private TextInputLayout categoryLayout;
    private TextInputLayout paymentMethodLayout;
    private MaterialButtonToggleGroup typeToggleGroup;
    private MaterialButton dateButton;
    private Date selectedDate;
    private Calendar calendar;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

            // Initialize Firebase Auth
            auth = FirebaseAuth.getInstance();

            // Initialize views
            initializeViews(view);

            // Setup category dropdown
            setupCategoryDropdown();

            // Setup date picker
            setupDatePicker();

            // Setup save button
            setupSaveButton(view.findViewById(R.id.btn_save));

            // Initialize ViewModels
            transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
            userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

            return view;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error initializing view: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void initializeViews(View view) {
        try {
            amountLayout = view.findViewById(R.id.layout_amount);
            titleLayout = view.findViewById(R.id.layout_title);
            descriptionLayout = view.findViewById(R.id.layout_description);
            categoryLayout = view.findViewById(R.id.layout_category);
            paymentMethodLayout = view.findViewById(R.id.layout_payment_method);
            typeToggleGroup = view.findViewById(R.id.toggle_type);
            dateButton = view.findViewById(R.id.btn_date);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupCategoryDropdown() {
        try {
            String[] categories = {"Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Education", "Other"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
                    android.R.layout.simple_dropdown_item_1line, categories);
            ((AutoCompleteTextView) categoryLayout.getEditText()).setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error setting up categories: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupDatePicker() {
        try {
            calendar = Calendar.getInstance();
            selectedDate = calendar.getTime();
            updateDateButtonText();

            dateButton.setOnClickListener(v -> {
                try {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                            requireContext(),
                            (view, year, month, dayOfMonth) -> {
                                calendar.set(Calendar.YEAR, year);
                                calendar.set(Calendar.MONTH, month);
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                                selectedDate = calendar.getTime();
                                updateDateButtonText();
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                    );
                    datePickerDialog.show();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error setting date: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error setting up date picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateDateButtonText() {
        try {
            dateButton.setText(String.format("%tF", selectedDate));
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error updating date: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupSaveButton(MaterialButton saveButton) {
        saveButton.setOnClickListener(v -> {
            try {
                if (validateInputs()) {
                    saveTransaction();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Error saving transaction: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs() {
        try {
            boolean isValid = true;

            // Validate amount
            String amountStr = amountLayout.getEditText().getText().toString().trim();
            if (amountStr.isEmpty()) {
                amountLayout.setError("Amount is required");
                isValid = false;
            } else {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        amountLayout.setError("Amount must be greater than 0");
                        isValid = false;
                    } else {
                        amountLayout.setError(null);
                    }
                } catch (NumberFormatException e) {
                    amountLayout.setError("Invalid amount");
                    isValid = false;
                }
            }

            // Validate title
            String title = titleLayout.getEditText().getText().toString().trim();
            if (title.isEmpty()) {
                titleLayout.setError("Title is required");
                isValid = false;
            } else {
                titleLayout.setError(null);
            }

            // Validate category
            String category = categoryLayout.getEditText().getText().toString().trim();
            if (category.isEmpty()) {
                categoryLayout.setError("Category is required");
                isValid = false;
            } else {
                categoryLayout.setError(null);
            }

            // Validate payment method
            String paymentMethod = paymentMethodLayout.getEditText().getText().toString().trim();
            if (paymentMethod.isEmpty()) {
                paymentMethodLayout.setError("Payment method is required");
                isValid = false;
            } else {
                paymentMethodLayout.setError(null);
            }

            return isValid;
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error validating inputs: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void saveTransaction() {
        try {
            // Check if user is authenticated
            if (auth.getCurrentUser() == null) {
                Toast.makeText(requireContext(), "Please sign in to add transactions", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.loginFragment);
                return;
            }

            // Disable save button to prevent double submission
            MaterialButton saveButton = requireView().findViewById(R.id.btn_save);
            saveButton.setEnabled(false);

            // Get values from inputs with null checks
            String title = getInputText(titleLayout);
            String category = getInputText(categoryLayout);
            String amountStr = getInputText(amountLayout);
            String paymentMethod = getInputText(paymentMethodLayout);
            String notes = descriptionLayout.getEditText() != null ? 
                descriptionLayout.getEditText().getText().toString().trim() : "";

            // Validate amount format
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    throw new IllegalArgumentException("Amount must be greater than 0");
                }
            } catch (NumberFormatException e) {
                amountLayout.setError("Invalid amount format");
                saveButton.setEnabled(true);
                return;
            }

            String type = typeToggleGroup.getCheckedButtonId() == R.id.btn_income ? "income" : "expense";

            // Create transaction object with validation
            Transaction transaction = new Transaction();
            transaction.setId(UUID.randomUUID().toString());
            transaction.setTitle(title);
            transaction.setCategory(category);
            transaction.setAmount(amount);
            transaction.setDate(selectedDate);
            transaction.setType(type);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setNotes(notes);
            transaction.setUserId(auth.getCurrentUser().getUid());

            // Observe loading and error states
            transactionViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
                if (!isLoading) {
                    saveButton.setEnabled(true);
                }
            });

            transactionViewModel.getError().observe(getViewLifecycleOwner(), error -> {
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                } else {
                    // No error means success
                    Toast.makeText(requireContext(), "Transaction added successfully", Toast.LENGTH_SHORT).show();
                    try {
                        Navigation.findNavController(requireView()).navigateUp();
                    } catch (Exception e) {
                        // If navigation fails, try to navigate to home
                        Navigation.findNavController(requireView()).navigate(R.id.homeFragment);
                    }
                }
            });

            // Save to database
            transactionViewModel.addTransaction(transaction);

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error saving transaction: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Re-enable save button in case of error
            MaterialButton saveButton = requireView().findViewById(R.id.btn_save);
            if (saveButton != null) {
                saveButton.setEnabled(true);
            }
        }
    }

    private String getInputText(TextInputLayout layout) {
        if (layout == null || layout.getEditText() == null) {
            throw new IllegalArgumentException("Input layout or edit text is null");
        }
        return layout.getEditText().getText().toString().trim();
    }

    private void updateUserBalance(String type, double amount) {
        try {
            userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    double newBalance = user.getTotalBalance();
                    double newIncome = user.getTotalIncome();
                    double newExpense = user.getTotalExpense();

                    if (type.equals("income")) {
                        newBalance += amount;
                        newIncome += amount;
                    } else {
                        newBalance -= amount;
                        newExpense += amount;
                    }

                    // Update values individually
                    userViewModel.updateBalance(newBalance);
                    userViewModel.updateIncome(newIncome);
                    userViewModel.updateExpense(newExpense);
                } else {
                    Toast.makeText(requireContext(), 
                        "Transaction saved but user data not found", 
                        Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), 
                "Error updating balance: " + e.getMessage(), 
                Toast.LENGTH_LONG).show();
        }
    }
}