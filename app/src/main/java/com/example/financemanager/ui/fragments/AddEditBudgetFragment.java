package com.example.financemanager.ui.fragments;

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
import com.example.financemanager.data.entities.Budget;
import com.example.financemanager.ui.viewmodels.BudgetViewModel;
import com.example.financemanager.ui.viewmodels.TransactionViewModel;
import com.example.financemanager.utils.Constants;
import com.example.financemanager.utils.CurrencyFormatter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class AddEditBudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private TransactionViewModel transactionViewModel;

    private ImageView ivBack;
    private TextView tvScreenTitle, tvCurrentSpending;
    private TextInputLayout tilCategory, tilAmount;
    private TextInputEditText etAmount;
    private AutoCompleteTextView dropdownCategory;
    private RadioGroup rgPeriod;
    private RadioButton rbMonthly, rbWeekly, rbYearly;
    private Button btnSaveBudget, btnCancel;

    private int budgetId = -1;
    private Budget currentBudget;
    private boolean isEditMode = false;
    private String currentPeriod = Constants.BUDGET_PERIOD_MONTHLY;
    private double currentSpending = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get budget ID from arguments if present
        if (getArguments() != null) {
            budgetId = getArguments().getInt("budgetId", -1);
            isEditMode = budgetId != -1;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_edit_budget, container, false);

        // Initialize views
        ivBack = view.findViewById(R.id.iv_back);
        tvScreenTitle = view.findViewById(R.id.tv_screen_title);
        tilCategory = view.findViewById(R.id.til_category);
        tilAmount = view.findViewById(R.id.til_amount);
        etAmount = view.findViewById(R.id.et_amount);
        dropdownCategory = view.findViewById(R.id.dropdown_category);
        rgPeriod = view.findViewById(R.id.rg_period);
        rbMonthly = view.findViewById(R.id.rb_monthly);
        rbWeekly = view.findViewById(R.id.rb_weekly);
        rbYearly = view.findViewById(R.id.rb_yearly);
        btnSaveBudget = view.findViewById(R.id.btn_save_budget);
        btnCancel = view.findViewById(R.id.btn_cancel);
        tvCurrentSpending = view.findViewById(R.id.tv_current_spending);

        // Setup ViewModels
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Setup screen title based on mode
        tvScreenTitle.setText(isEditMode ? "Edit Budget" : "Add Budget");

        // Setup category dropdown
        setupCategoryDropdown();

        // Load budget data if in edit mode
        if (isEditMode) {
            loadBudgetData();
        } else {
            // Hide current spending section in add mode
            view.findViewById(R.id.layout_current_spending).setVisibility(View.GONE);
        }

        // Setup listeners
        setupListeners(view);

        return view;
    }

    private void setupCategoryDropdown() {
        // Use expense categories for budget
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                Constants.DEFAULT_EXPENSE_CATEGORIES
        );
        dropdownCategory.setAdapter(categoryAdapter);
    }

    private void loadBudgetData() {
        budgetViewModel.getAllBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null) {
                for (Budget budget : budgets) {
                    if (budget.getId() == budgetId) {
                        currentBudget = budget;
                        populateFields(budget);
                        break;
                    }
                }

                if (currentBudget == null && isEditMode) {
                    // Budget not found but in edit mode
                    Toast.makeText(requireContext(), "Budget not found", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                }
            }
        });
    }

    private void populateFields(Budget budget) {
        // Set category
        dropdownCategory.setText(budget.getCategory(), false);

        // Set amount
        etAmount.setText(String.valueOf(budget.getBudgetAmount()));

        // Set period
        switch (budget.getPeriod()) {
            case Constants.BUDGET_PERIOD_WEEKLY:
                rbWeekly.setChecked(true);
                break;
            case Constants.BUDGET_PERIOD_YEARLY:
                rbYearly.setChecked(true);
                break;
            default:
                rbMonthly.setChecked(true);
                break;
        }

        // Set current spending
        currentSpending = budget.getSpentAmount();
        tvCurrentSpending.setText(CurrencyFormatter.format(currentSpending));
    }

    private void setupListeners(View view) {
        // Back button
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        // Period selection
        rgPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_weekly) {
                currentPeriod = Constants.BUDGET_PERIOD_WEEKLY;
            } else if (checkedId == R.id.rb_yearly) {
                currentPeriod = Constants.BUDGET_PERIOD_YEARLY;
            } else {
                currentPeriod = Constants.BUDGET_PERIOD_MONTHLY;
            }
        });

        // Save button
        btnSaveBudget.setOnClickListener(v -> {
            saveBudget();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });
    }

    private void saveBudget() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        String category = Objects.requireNonNull(dropdownCategory.getText()).toString().trim();
        double amount = Double.parseDouble(Objects.requireNonNull(etAmount.getText()).toString().trim());

        if (isEditMode && currentBudget != null) {
            // Update existing budget
            currentBudget.setCategory(category);
            currentBudget.setBudgetAmount(amount);
            currentBudget.setPeriod(currentPeriod);

            budgetViewModel.update(currentBudget);
            Toast.makeText(requireContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Create new budget
            Budget newBudget = new Budget(
                    category,
                    amount,
                    0, // No spending yet for new budget
                    currentPeriod
            );

            budgetViewModel.insert(newBudget);
            Toast.makeText(requireContext(), "Budget added successfully", Toast.LENGTH_SHORT).show();
        }

        // Navigate back
        Navigation.findNavController(requireView()).navigateUp();
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Reset errors
        tilCategory.setError(null);
        tilAmount.setError(null);

        // Validate category
        if (TextUtils.isEmpty(dropdownCategory.getText())) {
            tilCategory.setError("Category is required");
            isValid = false;
        }

        // Validate amount
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        if (TextUtils.isEmpty(amountStr)) {
            tilAmount.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    tilAmount.setError("Amount must be greater than zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilAmount.setError("Invalid amount");
                isValid = false;
            }
        }

        return isValid;
    }
}