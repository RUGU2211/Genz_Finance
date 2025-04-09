package com.example.financemanager;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.financemanager.databinding.ActivityMainBinding;
import com.example.financemanager.ui.viewmodels.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the toolbar
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Initialize AuthViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Set up Navigation - Fixed to use NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        
        // Define top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.homeFragment,
                R.id.transactionFragment,
                R.id.budgetFragment,
                R.id.profileFragment
        ).build();

        // Set up the bottom navigation
        BottomNavigationView bottomNav = binding.bottomNavigation;
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Set up the ActionBar with NavController
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Handle navigation visibility
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            // Hide bottom navigation on auth screens
            if (destination.getId() == R.id.loginFragment || 
                destination.getId() == R.id.signupFragment) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });

        // Check authentication state and navigate accordingly
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // User is not logged in, navigate to login fragment
            navController.navigate(R.id.loginFragment);
        } else {
            // User is logged in, navigate to home fragment
            navController.navigate(R.id.homeFragment);
        }

        // Observe authentication state changes
        authViewModel.getIsAuthenticated().observe(this, isAuthenticated -> {
            if (!isAuthenticated) {
                navController.navigate(R.id.loginFragment);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }
}