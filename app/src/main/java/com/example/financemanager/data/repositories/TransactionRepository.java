package com.example.financemanager.data.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.financemanager.data.AppDatabase;
import com.example.financemanager.data.dao.TransactionDao;
import com.example.financemanager.data.entities.Transaction;

import java.util.Date;
import java.util.List;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final LiveData<List<Transaction>> allTransactions;
    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final LiveData<List<TransactionDao.CategoryTotal>> expensesByCategory;
    private final LiveData<List<TransactionDao.CategoryTotal>> incomeByCategory;

    public TransactionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        transactionDao = database.transactionDao();
        allTransactions = transactionDao.getAllTransactions();
        totalIncome = transactionDao.getTotalIncome();
        totalExpense = transactionDao.getTotalExpense();
        expensesByCategory = transactionDao.getExpensesByCategory();
        incomeByCategory = transactionDao.getIncomeByCategory();
    }

    // Methods to access data from the database
    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Transaction>> getTransactionsByType(String type) {
        return transactionDao.getTransactionsByType(type);
    }

    public LiveData<List<Transaction>> getTransactionsBetweenDates(Date startDate, Date endDate) {
        return transactionDao.getTransactionsBetweenDates(startDate, endDate);
    }

    public LiveData<List<Transaction>> getTransactionsByCategory(String category) {
        return transactionDao.getTransactionsByCategory(category);
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

    // Operations on the database
    public void insert(Transaction transaction) {
        new InsertTransactionAsyncTask(transactionDao).execute(transaction);
    }

    public void update(Transaction transaction) {
        new UpdateTransactionAsyncTask(transactionDao).execute(transaction);
    }

    public void delete(Transaction transaction) {
        new DeleteTransactionAsyncTask(transactionDao).execute(transaction);
    }

    // AsyncTask classes for database operations
    private static class InsertTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {
        private final TransactionDao transactionDao;

        private InsertTransactionAsyncTask(TransactionDao transactionDao) {
            this.transactionDao = transactionDao;
        }

        @Override
        protected Void doInBackground(Transaction... transactions) {
            transactionDao.insert(transactions[0]);
            return null;
        }
    }

    private static class UpdateTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {
        private final TransactionDao transactionDao;

        private UpdateTransactionAsyncTask(TransactionDao transactionDao) {
            this.transactionDao = transactionDao;
        }

        @Override
        protected Void doInBackground(Transaction... transactions) {
            transactionDao.update(transactions[0]);
            return null;
        }
    }

    private static class DeleteTransactionAsyncTask extends AsyncTask<Transaction, Void, Void> {
        private final TransactionDao transactionDao;

        private DeleteTransactionAsyncTask(TransactionDao transactionDao) {
            this.transactionDao = transactionDao;
        }

        @Override
        protected Void doInBackground(Transaction... transactions) {
            transactionDao.delete(transactions[0]);
            return null;
        }
    }
}