package com.example.financemanager.ui.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.financemanager.data.entities.User;
import com.example.financemanager.data.repositories.UserRepository;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final LiveData<User> user;

    public UserViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        user = repository.getUser();
    }

    // Methods exposed to the UI
    public LiveData<User> getUser() {
        return user;
    }

    public void insert(User user) {
        repository.insert(user);
    }

    public void update(User user) {
        repository.update(user);
    }

    public void updateBalance(double balance) {
        repository.updateBalance(balance);
    }

    public void updateIncome(double income) {
        repository.updateIncome(income);
    }

    public void updateExpense(double expense) {
        repository.updateExpense(expense);
    }
}