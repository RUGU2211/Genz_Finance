package com.example.financemanager.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.ui.adapters.BudgetAdapter;
import com.example.financemanager.ui.viewmodels.BudgetViewModel;
import com.example.financemanager.ui.viewmodels.TransactionViewModel;
import com.example.financemanager.utils.CurrencyFormatter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetFragment extends Fragment {

    private BudgetViewModel budgetViewModel;
    private TransactionViewModel transactionViewModel;
    private RecyclerView rvBudgets;
    private BudgetAdapter budgetAdapter;
    private Spinner spinnerMonth;
    private TextView tvTotalBudget, tvNoBudgets;
    private MaterialButton btnAddBudget;

    private int selectedMonth;
    private int selectedYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        // Initialize views
        rvBudgets = view.findViewById(R.id.rv_budgets);
        spinnerMonth = view.findViewById(R.id.spinner_month);
        tvTotalBudget = view.findViewById(R.id.tv_total_budget);
        tvNoBudgets = view.findViewById(R.id.tv_no_budgets);
        btnAddBudget = view.findViewById(R.id.btn_add_budget);

        // Setup RecyclerView
        rvBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        budgetAdapter = new BudgetAdapter();
        rvBudgets.setAdapter(budgetAdapter);

        // Setup ViewModels
        budgetViewModel = new ViewModelProvider(this).get(BudgetViewModel.class);
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        // Setup month spinner
        setupMonthSpinner();

        // Observe data changes
        observeBudgets();

        // Setup click listeners
        btnAddBudget.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_to_addEditBudgetFragment);
        });

        budgetAdapter.setOnEditClickListener(budget -> {
            Bundle bundle = new Bundle();
            bundle.putInt("budgetId", budget.getId());
            Navigation.findNavController(view).navigate(R.id.action_to_addEditBudgetFragment, bundle);
        });

        return view;
    }

    private void setupMonthSpinner() {
        // Get current month and year
        Calendar calendar = Calendar.getInstance();
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedYear = calendar.get(Calendar.YEAR);

        // Create list of months with year (for previous months)
        List<String> monthYearLabels = new ArrayList<>();
        Calendar tempCalendar = Calendar.getInstance();

        // Add last 3 months, current month, and next month
        for (int i = -3; i <= 1; i++) {
            tempCalendar.set(Calendar.MONTH, selectedMonth + i);
            tempCalendar.set(Calendar.YEAR, selectedYear);

            // Adjust year if needed
            if (selectedMonth + i < 0) {
                tempCalendar.set(Calendar.YEAR, selectedYear - 1);
                tempCalendar.set(Calendar.MONTH, 12 + (selectedMonth + i));
            } else if (selectedMonth + i > 11) {
                tempCalendar.set(Calendar.YEAR, selectedYear + 1);
                tempCalendar.set(Calendar.MONTH, (selectedMonth + i) - 12);
            }

            String monthName = new java.text.SimpleDateFormat("MMMM").format(tempCalendar.getTime());
            String yearName = new java.text.SimpleDateFormat("yyyy").format(tempCalendar.getTime());
            monthYearLabels.add(monthName + " " + yearName);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                monthYearLabels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Set current month
        spinnerMonth.setSelection(3); // Current month is in the middle (index 3)

        // Listen for month changes
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Calculate selected month and year based on position
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, selectedMonth + (position - 3));
                cal.set(Calendar.YEAR, selectedYear);

                // Adjust year if needed
                if (selectedMonth + (position - 3) < 0) {
                    cal.set(Calendar.YEAR, selectedYear - 1);
                    cal.set(Calendar.MONTH, 12 + (selectedMonth + (position - 3)));
                } else if (selectedMonth + (position - 3) > 11) {
                    cal.set(Calendar.YEAR, selectedYear + 1);
                    cal.set(Calendar.MONTH, (selectedMonth + (position - 3)) - 12);
                }

                // Filter budgets and transactions by month
                updateBudgetData(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateBudgetData(int month, int year) {
        // In a real implementation, you would filter both budgets and transactions by the selected month
        // For simplicity in this example, we're just observing all budgets

        // You would need to:
        // 1. Filter transactions for this month/year to get actual spending
        // 2. Update budget progress based on these transactions
        // 3. Display updated budget data

        // For now, just log the selected month/year
        System.out.println("Selected month: " + month + ", year: " + year);
    }

    private void observeBudgets() {
        budgetViewModel.getAllBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null && !budgets.isEmpty()) {
                budgetAdapter.submitList(budgets);
                rvBudgets.setVisibility(View.VISIBLE);
                tvNoBudgets.setVisibility(View.GONE);
            } else {
                budgetAdapter.submitList(null);
                rvBudgets.setVisibility(View.GONE);
                tvNoBudgets.setVisibility(View.VISIBLE);
            }
        });

        budgetViewModel.getTotalBudget().observe(getViewLifecycleOwner(), totalBudget -> {
            if (totalBudget != null) {
                tvTotalBudget.setText(CurrencyFormatter.format(totalBudget));
            } else {
                tvTotalBudget.setText(CurrencyFormatter.format(0));
            }
        });

        // You would also want to observe transactions to update budget progress
        // This would look like:
        /*
        transactionViewModel.getTransactionsByMonthYear(selectedMonth, selectedYear).observe(
            getViewLifecycleOwner(), transactions -> {
                // Calculate category spending
                // Update budget progress
            });
        */
    }
}