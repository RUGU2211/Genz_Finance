package com.example.financemanager.ui.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financemanager.R;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.ui.adapters.TransactionAdapter;
import com.example.financemanager.ui.viewmodels.TransactionViewModel;
import com.example.financemanager.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionsFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private TransactionViewModel transactionViewModel;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private TabLayout tabLayout;
    private TextView tvDateRange, tvSort, tvNoTransactions;
    private FloatingActionButton fabAddTransaction;

    private Date startDate, endDate;
    private String currentType = "ALL"; // ALL, INCOME, EXPENSE
    private String currentSort = "DATE_DESC"; // DATE_DESC, DATE_ASC, AMOUNT_DESC, AMOUNT_ASC
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);

        // Initialize views
        rvTransactions = view.findViewById(R.id.rv_transactions);
        tabLayout = view.findViewById(R.id.tab_layout);
        tvDateRange = view.findViewById(R.id.tv_date_range);
        tvSort = view.findViewById(R.id.tv_sort);
        tvNoTransactions = view.findViewById(R.id.tv_no_transactions);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);

        // Setup RecyclerView
        rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TransactionAdapter(this);
        rvTransactions.setAdapter(adapter);

        // Set default date range to current month
        setCurrentMonthDateRange();

        // Setup click listeners
        setupListeners(view);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fabAddTransaction.setOnClickListener(v -> 
            Navigation.findNavController(view)
                    .navigate(R.id.action_transactionsFragment_to_addTransactionFragment)
        );

        setupObservers();
    }

    private void setupListeners(View view) {
        // Tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentType = "ALL";
                        break;
                    case 1:
                        currentType = Constants.TRANSACTION_TYPE_INCOME;
                        break;
                    case 2:
                        currentType = Constants.TRANSACTION_TYPE_EXPENSE;
                        break;
                }
                observeTransactions();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Date range selection
        tvDateRange.setOnClickListener(v -> {
            showDateRangePicker();
        });

        // Sort options
        tvSort.setOnClickListener(v -> {
            showSortMenu(v);
        });
    }

    private void setCurrentMonthDateRange() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        startDate = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        endDate = calendar.getTime();

        tvDateRange.setText("This Month");
    }

    private void showDateRangePicker() {
        // This is a simplified implementation. In a real app, you would use a proper date range picker
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    startDate = calendar.getTime();
                    showEndDatePicker(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setTitle("Select Start Date");
        datePickerDialog.show();
    }

    private void showEndDatePicker(Calendar startCalendar) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    endDate = calendar.getTime();
                    tvDateRange.setText(dateFormat.format(startDate) + " - " + dateFormat.format(endDate));
                    observeTransactions();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.setTitle("Select End Date");
        datePickerDialog.show();
    }

    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.sort_date_newest) {
                currentSort = "DATE_DESC";
                tvSort.setText("Date (Newest)");
            } else if (id == R.id.sort_date_oldest) {
                currentSort = "DATE_ASC";
                tvSort.setText("Date (Oldest)");
            } else if (id == R.id.sort_amount_highest) {
                currentSort = "AMOUNT_DESC";
                tvSort.setText("Amount (Highest)");
            } else if (id == R.id.sort_amount_lowest) {
                currentSort = "AMOUNT_ASC";
                tvSort.setText("Amount (Lowest)");
            }
            observeTransactions();
            return true;
        });

        popup.show();
    }

    private void setupObservers() {
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                updateEmptyState(transactions);
                adapter.submitList(transactions);
            } else {
                adapter.submitList(new ArrayList<>());
            }
        });
    }

    private void updateEmptyState(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void observeTransactions() {
        // Get transactions based on selected filters
        transactionViewModel.getTransactionsBetweenDates(startDate, endDate).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                List<Transaction> filteredTransactions = filterAndSortTransactions(transactions);
                adapter.submitList(filteredTransactions);

                rvTransactions.setVisibility(filteredTransactions.isEmpty() ? View.GONE : View.VISIBLE);
                tvNoTransactions.setVisibility(filteredTransactions.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                adapter.submitList(null);
                rvTransactions.setVisibility(View.GONE);
                tvNoTransactions.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<Transaction> filterAndSortTransactions(List<Transaction> transactions) {
        // Create a mutable copy of the list
        List<Transaction> result = new ArrayList<>();

        // Filter by type
        if (currentType.equals("ALL")) {
            // Add all transactions if not filtering by type
            result.addAll(transactions);
        } else {
            // Add only transactions of the selected type
            for (Transaction transaction : transactions) {
                if (transaction.getType().equals(currentType)) {
                    result.add(transaction);
                }
            }
        }

        // Sort based on selected option
        if (currentSort.equals("DATE_DESC")) {
            Collections.sort(result, new Comparator<Transaction>() {
                @Override
                public int compare(Transaction t1, Transaction t2) {
                    return t2.getDate().compareTo(t1.getDate());
                }
            });
        } else if (currentSort.equals("DATE_ASC")) {
            Collections.sort(result, new Comparator<Transaction>() {
                @Override
                public int compare(Transaction t1, Transaction t2) {
                    return t1.getDate().compareTo(t2.getDate());
                }
            });
        } else if (currentSort.equals("AMOUNT_DESC")) {
            Collections.sort(result, new Comparator<Transaction>() {
                @Override
                public int compare(Transaction t1, Transaction t2) {
                    return Double.compare(t2.getAmount(), t1.getAmount());
                }
            });
        } else if (currentSort.equals("AMOUNT_ASC")) {
            Collections.sort(result, new Comparator<Transaction>() {
                @Override
                public int compare(Transaction t1, Transaction t2) {
                    return Double.compare(t1.getAmount(), t2.getAmount());
                }
            });
        }

        return result;
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Bundle bundle = new Bundle();
        bundle.putString("transactionId", transaction.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_transactionsFragment_to_transactionDetailFragment, bundle);
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {

    }
}