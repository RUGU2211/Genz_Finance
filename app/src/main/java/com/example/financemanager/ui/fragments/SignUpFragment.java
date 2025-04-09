package com.example.financemanager.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.financemanager.R;
import com.example.financemanager.ui.viewmodels.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpFragment extends Fragment {
    private static final int RC_SIGN_IN = 9001;

    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp, btnGoogleSignIn;
    private View tvLogin;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tilName = view.findViewById(R.id.tilName);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        tilConfirmPassword = view.findViewById(R.id.tilConfirmPassword);
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        tvLogin = view.findViewById(R.id.tvLogin);

        // Set click listeners
        btnSignUp.setOnClickListener(v -> signUp());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvLogin.setOnClickListener(v -> navigateToLogin());

        // Observe authentication state
        authViewModel.getIsAuthenticated().observe(getViewLifecycleOwner(), isAuthenticated -> {
            if (isAuthenticated) {
                navigateToHome();
            }
        });

        // Observe loading state
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSignUp.setEnabled(!isLoading);
            btnGoogleSignIn.setEnabled(!isLoading);
            // You might want to show a progress indicator here
        });

        // Observe errors
        authViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void signUp() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Reset errors
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validate inputs
        if (name.isEmpty()) {
            tilName.setError("Name is required");
            return;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Confirm password is required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        authViewModel.signUpWithEmail(name, email, password);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    String idToken = account.getIdToken();
                    if (idToken != null) {
                        authViewModel.signInWithGoogle(idToken);
                    } else {
                        Toast.makeText(requireContext(), "Google sign in failed: Unable to get ID token", 
                            Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Google sign in failed: No account found", 
                        Toast.LENGTH_LONG).show();
                }
            } catch (ApiException e) {
                String errorMessage = "Google sign in failed: ";
                switch (e.getStatusCode()) {
                    case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                        errorMessage += "Sign in was cancelled";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_CURRENTLY_IN_PROGRESS:
                        errorMessage += "Sign in is already in progress";
                        break;
                    case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                        errorMessage += "Sign in failed";
                        break;
                    case GoogleSignInStatusCodes.DEVELOPER_ERROR:
                        errorMessage += "Developer error - Please check configuration";
                        break;
                    default:
                        errorMessage += e.getMessage();
                }
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void navigateToLogin() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_signupFragment_to_loginFragment);
    }

    private void navigateToHome() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_signupFragment_to_homeFragment);
    }
} 