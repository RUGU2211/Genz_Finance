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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class AddTransactionFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private UserViewModel userViewModel;

    private RadioGroup rgTransactionType;
    private RadioButton rbIncome, rbExpense;
    private TextInputEditText etAmount, etTitle, etDate, etNotes;
    private AutoCompleteTextView dropdownCategory, dropdownPaymentMethod;
    private Button btnAddTransaction;
    private TextInputLayout tilAmount, tilTitle, tilCategory, tilPaymentMethod;

    private Date selectedDate = new Date(); // Default to current date
    private String currentType = Constants.TRANSACTION_TYPE_EXPENSE; // Default type

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        // Initialize views
        rgTransactionType = view.findViewById(R.id.rg_transaction_type);
        rbIncome = view.findViewById(R.id.rb_income);
        rbExpense = view.findViewById(R.id.rb_expense);
        etAmount = view.findViewById(R.id.et_amount);
        etTitle = view.findViewById(R.id.et_title);
        etDate = view.findViewById(R.id.et_date);
        etNotes = view.findViewById(R.id.et_notes);
        dropdownCategory = view.findViewById(R.id.dropdown_category);
        dropdownPaymentMethod = view.findViewById(R.id.dropdown_payment_method);
        btnAddTransaction = view.findViewById(R.id.btn_add_transaction);

        // Get TextInputLayouts for validation
        tilAmount = view.findViewById(R.id.til_amount);
        tilTitle = view.findViewById(R.id.til_title);
        tilCategory = view.findViewById(R.id.til_category);
        tilPaymentMethod = view.findViewById(R.id.til_payment_method);

        // Setup ViewModels
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Set default date
        etDate.setText(DateUtils.formatDate(selectedDate));

        // Setup category dropdown based on transaction type
        setupCategoryDropdown();

        // Setup payment method dropdown
        ArrayAdapter<String> paymentMethodAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Constants.DEFAULT_PAYMENT_METHODS
        );
        dropdownPaymentMethod.setAdapter(paymentMethodAdapter);

        // Setup listeners
        setupListeners();

        return view;
    }

    private void setupListeners() {
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

        // Add transaction button
        btnAddTransaction.setOnClickListener(v -> {
            saveTransaction();
        });
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

        // Clear current selection
        dropdownCategory.setText("", false);
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

    private void saveTransaction() {
        // Reset errors
        tilAmount.setError(null);
        tilTitle.setError(null);
        tilCategory.setError(null);
        tilPaymentMethod.setError(null);

        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values from inputs
        String title = etTitle.getText().toString().trim();
        String category = dropdownCategory.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        String paymentMethod = dropdownPaymentMethod.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Create transaction object
        Transaction transaction = new Transaction(
                title,
                category,
                amount,
                selectedDate,
                currentType,
                paymentMethod,
                notes
        );

        // Save to database
        transactionViewModel.insert(transaction);

        // Update user totals
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                double newBalance = user.getTotalBalance();
                double newIncome = user.getTotalIncome();
                double newExpense = user.getTotalExpense();

                if (currentType.equals(Constants.TRANSACTION_TYPE_INCOME)) {
                    newBalance += amount;
                    newIncome += amount;
                } else {
                    newBalance -= amount;
                    newExpense += amount;
                }

                userViewModel.updateBalance(newBalance);
                userViewModel.updateIncome(newIncome);
                userViewModel.updateExpense(newExpense);
            }
        });

        // Show success message
        Toast.makeText(requireContext(), "Transaction added successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to Home
        Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
    }

    private boolean validateInputs() {
        boolean isValid = true;

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

    private void clearInputs() {
        etAmount.setText("");
        etTitle.setText("");
        dropdownCategory.setText("", false);
        dropdownPaymentMethod.setText("", false);
        etNotes.setText("");

        // Reset to defaults
        rbExpense.setChecked(true);
        selectedDate = new Date();
        etDate.setText(DateUtils.formatDate(selectedDate));
    }
}