package com.example.financemanager.data.repositories;

import androidx.lifecycle.LiveData;
import com.example.financemanager.data.dao.TransactionDao;
import com.example.financemanager.data.entities.Transaction;
import java.util.Date;
import java.util.List;

public class TransactionRepository {
    private final TransactionDao transactionDao;
    private final String userId;

    public TransactionRepository(TransactionDao transactionDao, String userId) {
        this.transactionDao = transactionDao;
        this.userId = userId;
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return transactionDao.getAllTransactions(userId);
    }

    public LiveData<Double> getTotalIncome() {
        return transactionDao.getTotalByType(userId, "income");
    }

    public LiveData<Double> getTotalExpense() {
        return transactionDao.getTotalByType(userId, "expense");
    }

    public LiveData<Double> getTotalByType(String type) {
        return transactionDao.getTotalByType(userId, type);
    }

    public LiveData<List<TransactionDao.CategoryTotal>> getExpensesByCategory() {
        return transactionDao.getExpensesByCategory(userId);
    }

    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return transactionDao.getTransactionsBetweenDates(userId, startDate, endDate);
    }

    public void insert(Transaction transaction) {
        transaction.setUserId(userId);
        transactionDao.insert(transaction);
    }

    public void update(Transaction transaction) {
        transaction.setUserId(userId);
        transactionDao.update(transaction);
    }

    public void delete(Transaction transaction) {
        transactionDao.delete(transaction);
    }
}