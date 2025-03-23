package com.example.financemanager.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.financemanager.data.entities.Budget;
import com.example.financemanager.data.repositories.BudgetRepository;

import java.util.List;

public class BudgetViewModel extends AndroidViewModel {

    private final BudgetRepository repository;
    private final LiveData<List<Budget>> allBudgets;
    private final LiveData<Double> totalBudget;

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        repository = new BudgetRepository(application);
        allBudgets = repository.getAllBudgets();
        totalBudget = repository.getTotalBudget();
    }

    // Methods exposed to the UI
    public LiveData<List<Budget>> getAllBudgets() {
        return allBudgets;
    }

    public LiveData<Budget> getBudgetByCategory(String category) {
        return repository.getBudgetByCategory(category);
    }

    public LiveData<Double> getTotalBudget() {
        return totalBudget;
    }

    public void insert(Budget budget) {
        repository.insert(budget);
    }

    public void update(Budget budget) {
        repository.update(budget);
    }

    public void delete(Budget budget) {
        repository.delete(budget);
    }
}