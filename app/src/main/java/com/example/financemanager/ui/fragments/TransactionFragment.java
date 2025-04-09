package com.example.financemanager.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class TransactionFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private TransactionViewModel transactionViewModel;
    private TransactionAdapter adapter;
    private TextView emptyStateText;
    private RecyclerView recyclerView;
    private CircularProgressIndicator progressIndicator;
    private FloatingActionButton fabAddTransaction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionViewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);

        // Initialize views
        emptyStateText = view.findViewById(R.id.text_empty_state);
        recyclerView = view.findViewById(R.id.recycler_transactions);
        progressIndicator = view.findViewById(R.id.progress_indicator);
        fabAddTransaction = view.findViewById(R.id.fab_add_transaction);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup ViewModel
        setupViewModel();

        // Setup click listeners
        setupClickListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup ViewModel
        setupViewModel();
    }

    private void setupRecyclerView() {
        adapter = new TransactionAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        // Observe transactions
        transactionViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                updateEmptyState(transactions);
                adapter.submitList(transactions);
            } else {
                adapter.submitList(new ArrayList<>());
            }
        });

        // Observe loading state
        transactionViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            fabAddTransaction.setEnabled(!isLoading);
        });

        // Observe errors
        transactionViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Load transactions
        transactionViewModel.loadTransactions();
    }

    private void setupClickListeners() {
        fabAddTransaction.setOnClickListener(v -> 
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_transactionFragment_to_addTransactionFragment)
        );
    }

    private void updateEmptyState(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        Bundle bundle = new Bundle();
        bundle.putString("transactionId", transaction.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_transactionFragment_to_transactionDetailFragment, bundle);
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        // Show delete confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> 
                    transactionViewModel.deleteTransaction(transaction.getId()))
                .setNegativeButton("Cancel", null)
                .show();
    }
} 