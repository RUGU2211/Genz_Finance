package com.example.financemanager.ui.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.financemanager.data.dao.TransactionDao;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.data.repositories.TransactionRepository;
import com.example.financemanager.data.AppDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {
    private final TransactionRepository repository;
    private final LiveData<List<Transaction>> allTransactions;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final LiveData<List<TransactionDao.CategoryTotal>> expensesByCategory;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>(new ArrayList<>());

    public TransactionViewModel(Application application) {
        super(application);
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        TransactionDao transactionDao = AppDatabase.getDatabase(application).transactionDao();
        repository = new TransactionRepository(transactionDao, userId);
        allTransactions = repository.getAllTransactions();
        totalIncome = repository.getTotalByType("income");
        totalExpense = repository.getTotalByType("expense");
        expensesByCategory = repository.getExpensesByCategory();
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<List<TransactionDao.CategoryTotal>> getExpensesByCategory() {
        return expensesByCategory;
    }

    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return repository.getTransactionsBetweenDates(startDate, endDate);
    }

    public void insert(Transaction transaction) {
        repository.insert(transaction);
    }

    public void update(Transaction transaction) {
        repository.update(transaction);
    }

    public void delete(Transaction transaction) {
        repository.delete(transaction);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

    public void loadTransactions() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            error.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        db.collection("transactions")
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener((value, e) -> {
                    isLoading.setValue(false);
                    if (e != null) {
                        error.setValue("Error loading transactions: " + e.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Transaction> transactionList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Transaction transaction = doc.toObject(Transaction.class);
                            transaction.setId(doc.getId());
                            transactionList.add(transaction);
                        }
                        transactions.setValue(transactionList);
                    }
                });
    }

    public void addTransaction(Transaction transaction) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            error.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        transaction.setUserId(userId);
        
        // First verify the user is still authenticated
        auth.getCurrentUser().getIdToken(true)
            .addOnSuccessListener(result -> {
                // User is still authenticated, proceed with adding transaction
                db.collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener(documentReference -> {
                        isLoading.setValue(false);
                        transaction.setId(documentReference.getId());
                        // Refresh the transactions list
                        loadTransactions();
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        error.setValue("Error adding transaction: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                isLoading.setValue(false);
                error.setValue("Authentication error: " + e.getMessage());
            });
    }

    public void deleteTransaction(String transactionId) {
        isLoading.setValue(true);
        db.collection("transactions")
                .document(transactionId)
                .delete()
                .addOnSuccessListener(aVoid -> isLoading.setValue(false))
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    error.setValue("Error deleting transaction: " + e.getMessage());
                });
    }

    public void updateTransaction(Transaction transaction) {
        if (transaction.getId() == null || transaction.getId().isEmpty()) {
            error.setValue("Transaction ID is required for update");
            return;
        }

        isLoading.setValue(true);
        db.collection("transactions")
                .document(transaction.getId())
                .set(transaction)
                .addOnSuccessListener(aVoid -> isLoading.setValue(false))
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    error.setValue("Error updating transaction: " + e.getMessage());
                });
    }
}