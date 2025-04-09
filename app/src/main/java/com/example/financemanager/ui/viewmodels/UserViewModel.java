package com.example.financemanager.ui.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.financemanager.data.AppDatabase;
import com.example.financemanager.data.dao.UserDao;
import com.example.financemanager.data.entities.User;
import com.example.financemanager.data.repositories.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository repository;
    private final LiveData<User> user;

    public UserViewModel(Application application) {
        super(application);
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        UserDao userDao = AppDatabase.getDatabase(application).userDao();
        repository = new UserRepository(userDao, userId);
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

    public void delete(User user) {
        repository.delete(user);
    }

    public LiveData<Boolean> checkUserExists() {
        return repository.checkUserExists();
    }
}