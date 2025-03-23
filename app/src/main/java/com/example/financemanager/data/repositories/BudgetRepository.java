package com.example.financemanager.data.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.financemanager.data.AppDatabase;
import com.example.financemanager.data.dao.BudgetDao;
import com.example.financemanager.data.entities.Budget;

import java.util.List;

public class BudgetRepository {

    private final BudgetDao budgetDao;
    private final LiveData<List<Budget>> allBudgets;
    private final LiveData<Double> totalBudget;

    public BudgetRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        budgetDao = database.budgetDao();
        allBudgets = budgetDao.getAllBudgets();
        totalBudget = budgetDao.getTotalBudget();
    }

    // Methods to access data from the database
    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<Budget> getBudgetByCategory(String category) {
        return budgetDao.getBudgetByCategory(category);
    }

    public LiveData<Double> getTotalBudget() {
        return totalBudget;
    }

    // Operations on the database
    public void insert(Budget budget) {
        new InsertBudgetAsyncTask(budgetDao).execute(budget);
    }

    public void update(Budget budget) {
        new UpdateBudgetAsyncTask(budgetDao).execute(budget);
    }

    public void delete(Budget budget) {
        new DeleteBudgetAsyncTask(budgetDao).execute(budget);
    }

    // AsyncTask classes for database operations
    private static class InsertBudgetAsyncTask extends AsyncTask<Budget, Void, Void> {
        private final BudgetDao budgetDao;

        private InsertBudgetAsyncTask(BudgetDao budgetDao) {
            this.budgetDao = budgetDao;
        }

        @Override
        protected Void doInBackground(Budget... budgets) {
            budgetDao.insert(budgets[0]);
            return null;
        }
    }

    private static class UpdateBudgetAsyncTask extends AsyncTask<Budget, Void, Void> {
        private final BudgetDao budgetDao;

        private UpdateBudgetAsyncTask(BudgetDao budgetDao) {
            this.budgetDao = budgetDao;
        }

        @Override
        protected Void doInBackground(Budget... budgets) {
            budgetDao.update(budgets[0]);
            return null;
        }
    }

    private static class DeleteBudgetAsyncTask extends AsyncTask<Budget, Void, Void> {
        private final BudgetDao budgetDao;

        private DeleteBudgetAsyncTask(BudgetDao budgetDao) {
            this.budgetDao = budgetDao;
        }

        @Override
        protected Void doInBackground(Budget... budgets) {
            budgetDao.delete(budgets[0]);
            return null;
        }
    }
}