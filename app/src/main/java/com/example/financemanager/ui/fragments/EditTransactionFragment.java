package com.example.financemanager.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import com.example.financemanager.utils.Constants;
import com.example.financemanager.utils.DateUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EditTransactionFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private UserViewModel userViewModel;

    private ImageView ivBack;
    private TextView tvScreenTitle;
    private RadioGroup rgTransactionType;
    private RadioButton rbIncome, rbExpense;
    private TextInputLayout tilAmount, tilTitle, tilCategory, tilPaymentMethod;
    private TextInputEditText etAmount, etTitle, etDate, etNotes;
    private AutoCompleteTextView dropdownCategory, dropdownPaymentMethod;
    private Button btnUpdateTransaction, btnCancel;

    private int transactionId = -1;
    private Transaction currentTransaction;
    private Date selectedDate = new Date();
    private String currentType = Constants.TRANSACTION_TYPE_EXPENSE;
    private double originalAmount = 0;
    private String originalType = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get transaction ID from arguments
        if (getArguments() != null) {
            transactionId = getArguments().getInt("transactionId", -1);
        }

        if (transactionId == -1) {
            // Invalid transaction ID, should not happen
            Toast.makeText(requireContext(), "Invalid transaction", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_transaction, container, false);

        // Initialize views
        ivBack = view.findViewById(R.id.iv_back);
        tvScreenTitle = view.findViewById(R.id.tv_screen_title);
        rgTransactionType = view.findViewById(R.id.rg_transaction_type);
        rbIncome = view.findViewById(R.id.rb_income);
        rbExpense = view.findViewById(R.id.rb_expense);
        etAmount = view.findViewById(R.id.et_amount);
        etTitle = view.findViewById(R.id.et_title);
        etDate = view.findViewById(R.id.et_date);
        etNotes = view.findViewById(R.id.et_notes);
        dropdownCategory = view.findViewById(R.id.dropdown_category);
        dropdownPaymentMethod = view.findViewById(R.id.dropdown_payment_method);
        btnUpdateTransaction = view.findViewById(R.id.btn_update_transaction);
        btnCancel = view.findViewById(R.id.btn_cancel);

        // Get TextInputLayouts for validation
        tilAmount = view.findViewById(R.id.til_amount);
        tilTitle = view.findViewById(R.id.til_title);
        tilCategory = view.findViewById(R.id.til_category);
        tilPaymentMethod = view.findViewById(R.id.til_payment_method);

        // Setup ViewModels
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Setup payment method dropdown
        ArrayAdapter<String> paymentMethodAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Constants.DEFAULT_PAYMENT_METHODS
        );
        dropdownPaymentMethod.setAdapter(paymentMethodAdapter);

        // Load transaction data
        loadTransactionData();

        // Setup listeners
        setupListeners(view);

        return view;
    }

    private void loadTransactionData() {
        if (transactionId == -1) {
            // Invalid ID, navigate back
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        // Query all transactions and find the one with matching ID
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                for (Transaction transaction : transactions) {
                    if (transaction.getId() == transactionId) {
                        currentTransaction = transaction;
                        populateFields(transaction);
                        break;
                    }
                }

                if (currentTransaction == null) {
                    // Transaction not found
                    Toast.makeText(requireContext(), "Transaction not found", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                }
            }
        });
    }

    private void populateFields(Transaction transaction) {
        // Save original values for balance adjustment later
        originalAmount = transaction.getAmount();
        originalType = transaction.getType();

        // Set transaction type
        if (transaction.getType().equals(Constants.TRANSACTION_TYPE_INCOME)) {
            rbIncome.setChecked(true);
            currentType = Constants.TRANSACTION_TYPE_INCOME;
        } else {
            rbExpense.setChecked(true);
            currentType = Constants.TRANSACTION_TYPE_EXPENSE;
        }

        // Set amount
        etAmount.setText(String.valueOf(transaction.getAmount()));

        // Set title
        etTitle.setText(transaction.getTitle());

        // Set date
        selectedDate = transaction.getDate();
        etDate.setText(DateUtils.formatDate(selectedDate));

        // Setup category dropdown based on type
        setupCategoryDropdown();

        // Set category
        dropdownCategory.setText(transaction.getCategory(), false);

        // Set payment method
        dropdownPaymentMethod.setText(transaction.getPaymentMethod(), false);

        // Set notes
        etNotes.setText(transaction.getNotes());
    }

    private void setupCategoryDropdown() {
        List<String> categories = currentType.equals(Constants.TRANSACTION_TYPE_INCOME) ?
                Constants.DEFAULT_INCOME_CATEGORIES : Constants.DEFAULT_EXPENSE_CATEGORIES;

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        dropdownCategory.setAdapter(categoryAdapter);
    }

    private void setupListeners(View view) {
        // Back button
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        // Transaction type change
        rgTransactionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_income) {
                currentType = Constants.TRANSACTION_TYPE_INCOME;
            } else {
                currentType = Constants.TRANSACTION_TYPE_EXPENSE;
            }
            setupCategoryDropdown();
        });

        // Date selection
        etDate.setOnClickListener(v -> {
            showDatePicker();
        });

        // Update button
        btnUpdateTransaction.setOnClickListener(v -> {
            updateTransaction();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDate = calendar.getTime();
                    etDate.setText(DateUtils.formatDate(selectedDate));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateTransaction() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values from inputs
        String title = Objects.requireNonNull(etTitle.getText()).toString().trim();
        String category = Objects.requireNonNull(dropdownCategory.getText()).toString().trim();
        double amount = Double.parseDouble(Objects.requireNonNull(etAmount.getText()).toString().trim());
        String paymentMethod = Objects.requireNonNull(dropdownPaymentMethod.getText()).toString().trim();
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        // Update transaction object
        currentTransaction.setTitle(title);
        currentTransaction.setCategory(category);
        currentTransaction.setAmount(amount);
        currentTransaction.setDate(selectedDate);
        currentTransaction.setType(currentType);
        currentTransaction.setPaymentMethod(paymentMethod);
        currentTransaction.setNotes(notes);

        // Save to database
        transactionViewModel.update(currentTransaction);

        // Update user balance based on changes
        updateUserBalance(originalType, originalAmount, currentType, amount);

        // Show success message
        Toast.makeText(requireContext(), "Transaction updated successfully", Toast.LENGTH_SHORT).show();

        // Navigate back
        Navigation.findNavController(requireView()).navigateUp();
    }

    private void updateUserBalance(String oldType, double oldAmount, String newType, double newAmount) {
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                double newBalance = user.getTotalBalance();
                double newIncome = user.getTotalIncome();
                double newExpense = user.getTotalExpense();

                // Revert the effects of the original transaction
                if (oldType.equals(Constants.TRANSACTION_TYPE_INCOME)) {
                    newBalance -= oldAmount;
                    newIncome -= oldAmount;
                } else {
                    newBalance += oldAmount;
                    newExpense -= oldAmount;
                }

                // Apply the effects of the updated transaction
                if (newType.equals(Constants.TRANSACTION_TYPE_INCOME)) {
                    newBalance += newAmount;
                    newIncome += newAmount;
                } else {
                    newBalance -= newAmount;
                    newExpense += newAmount;
                }

                // Update user data
                userViewModel.updateBalance(newBalance);
                userViewModel.updateIncome(newIncome);
                userViewModel.updateExpense(newExpense);
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Reset errors
        tilAmount.setError(null);
        tilTitle.setError(null);
        tilCategory.setError(null);
        tilPaymentMethod.setError(null);

        // Check amount
        if (TextUtils.isEmpty(etAmount.getText())) {
            tilAmount.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(etAmount.getText().toString().trim());
                if (amount <= 0) {
                    tilAmount.setError("Amount must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilAmount.setError("Invalid amount");
                isValid = false;
            }
        }

        // Check title
        if (TextUtils.isEmpty(etTitle.getText())) {
            tilTitle.setError("Title is required");
            isValid = false;
        }

        // Check category
        if (TextUtils.isEmpty(dropdownCategory.getText())) {
            tilCategory.setError("Category is required");
            isValid = false;
        }

        // Check payment method
        if (TextUtils.isEmpty(dropdownPaymentMethod.getText())) {
            tilPaymentMethod.setError("Payment method is required");
            isValid = false;
        }

        return isValid;
    }
}