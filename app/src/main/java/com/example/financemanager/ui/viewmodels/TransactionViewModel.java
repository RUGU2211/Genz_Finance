package com.example.financemanager.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.financemanager.data.dao.TransactionDao;
import com.example.financemanager.data.entities.Transaction;
import com.example.financemanager.data.repositories.TransactionRepository;

import java.util.Date;
import java.util.List;

public class TransactionViewModel extends AndroidViewModel {

    private final TransactionRepository repository;
    private final LiveData<List<Transaction>> allTransactions;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final LiveData<List<TransactionDao.CategoryTotal>> expensesByCategory;
    private final LiveData<List<TransactionDao.CategoryTotal>> incomeByCategory;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionRepository(application);
        allTransactions = repository.getAllTransactions();
        totalIncome = repository.getTotalIncome();
        totalExpense = repository.getTotalExpense();
        expensesByCategory = repository.getExpensesByCategory();
        incomeByCategory = repository.getIncomeByCategory();
    }

    // Methods exposed to the UI
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getTransactionsByType(String type) {
        return repository.getTransactionsByType(type);
    }

    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return repository.getTransactionsBetweenDates(startDate, endDate);
    }

    public LiveData<List<Transaction>> getTransactionsByCategory(String category) {
        return repository.getTransactionsByCategory(category);
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

    public LiveData<List<TransactionDao.CategoryTotal>> getIncomeByCategory() {
        return incomeByCategory;
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
}