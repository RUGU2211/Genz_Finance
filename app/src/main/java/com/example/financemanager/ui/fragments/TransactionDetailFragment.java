package com.example.financemanager.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.financemanager.utils.CurrencyFormatter;
import com.example.financemanager.utils.DateUtils;

public class TransactionDetailFragment extends Fragment {

    private TransactionViewModel transactionViewModel;
    private UserViewModel userViewModel;

    private TextView tvTitle, tvAmount, tvCategory, tvDate, tvPaymentMethod, tvNotes, tvType;
    private ImageView ivTypeIcon, ivBack;
    private Button btnEdit, btnDelete;

    private int transactionId;
    private Transaction currentTransaction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get transaction ID from arguments
        if (getArguments() != null) {
            transactionId = getArguments().getInt("transactionId", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_detail, container, false);

        // Initialize views
        tvTitle = view.findViewById(R.id.tv_title);
        tvAmount = view.findViewById(R.id.tv_amount);
        tvCategory = view.findViewById(R.id.tv_category);
        tvDate = view.findViewById(R.id.tv_date);
        tvPaymentMethod = view.findViewById(R.id.tv_payment_method);
        tvNotes = view.findViewById(R.id.tv_notes);
        tvType = view.findViewById(R.id.tv_type);
        ivTypeIcon = view.findViewById(R.id.iv_type_icon);
        ivBack = view.findViewById(R.id.iv_back);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnDelete = view.findViewById(R.id.btn_delete);

        // Setup ViewModels
        transactionViewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Load transaction data
        loadTransactionData();

        // Setup click listeners
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
                        displayTransactionDetails(transaction);
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

    private void displayTransactionDetails(Transaction transaction) {
        tvTitle.setText(transaction.getTitle());

        // Format amount based on type
        String formattedAmount = CurrencyFormatter.format(transaction.getAmount());
        if (transaction.getType().equals(Constants.TRANSACTION_TYPE_EXPENSE)) {
            tvAmount.setText("- " + formattedAmount);
            tvAmount.setTextColor(getResources().getColor(R.color.colorExpense));
            tvType.setText("Expense");
            ivTypeIcon.setImageResource(R.drawable.ic_expense); // Make sure you have this drawable
        } else {
            tvAmount.setText("+ " + formattedAmount);
            tvAmount.setTextColor(getResources().getColor(R.color.colorIncome));
            tvType.setText("Income");
            ivTypeIcon.setImageResource(R.drawable.ic_income); // Make sure you have this drawable
        }

        tvCategory.setText(transaction.getCategory());
        tvDate.setText(DateUtils.formatDate(transaction.getDate()));
        tvPaymentMethod.setText(transaction.getPaymentMethod());

        // Handle notes - hide if empty
        if (transaction.getNotes() != null && !transaction.getNotes().isEmpty()) {
            tvNotes.setText(transaction.getNotes());
            tvNotes.setVisibility(View.VISIBLE);
        } else {
            tvNotes.setVisibility(View.GONE);
        }
    }

    private void setupListeners(View view) {
        // Back button
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(view).navigateUp();
        });

        // Edit button
        btnEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putInt("transactionId", transactionId);
            Navigation.findNavController(view).navigate(R.id.action_transactionDetail_to_editTransaction, bundle);
        });

        // Delete button
        btnDelete.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete Transaction");
        builder.setMessage("Are you sure you want to delete this transaction? This action cannot be undone.");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteTransaction();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteTransaction() {
        if (currentTransaction != null) {
            // Update user balance
            userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    double newBalance = user.getTotalBalance();
                    double newIncome = user.getTotalIncome();
                    double newExpense = user.getTotalExpense();

                    // Adjust totals based on transaction type
                    if (currentTransaction.getType().equals(Constants.TRANSACTION_TYPE_INCOME)) {
                        newBalance -= currentTransaction.getAmount();
                        newIncome -= currentTransaction.getAmount();
                    } else {
                        newBalance += currentTransaction.getAmount();
                        newExpense -= currentTransaction.getAmount();
                    }

                    // Update user data
                    userViewModel.updateBalance(newBalance);
                    userViewModel.updateIncome(newIncome);
                    userViewModel.updateExpense(newExpense);

                    // Delete the transaction
                    transactionViewModel.delete(currentTransaction);

                    // Show success message and navigate back
                    Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                }
            });
        }
    }
}