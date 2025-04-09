package com.example.financemanager.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class LoginFragment extends Fragment {
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "LoginFragment";

    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGoogleSignIn;
    private View tvForgotPassword, tvSignUp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configure Google Sign In with additional scopes
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoogleSignIn = view.findViewById(R.id.btnGoogleSignIn);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        tvSignUp = view.findViewById(R.id.tvSignUp);

        // Set click listeners
        btnLogin.setOnClickListener(v -> login());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        tvForgotPassword.setOnClickListener(v -> resetPassword());
        tvSignUp.setOnClickListener(v -> navigateToSignUp());

        // Observe authentication state
        authViewModel.getIsAuthenticated().observe(getViewLifecycleOwner(), isAuthenticated -> {
            if (isAuthenticated) {
                navigateToHome();
            }
        });

        // Observe loading state
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnLogin.setEnabled(!isLoading);
            btnGoogleSignIn.setEnabled(!isLoading);
            // You might want to show a progress indicator here
        });

        // Observe errors
        authViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                if (error.contains("verify that you are not a robot")) {
                    // Show reCAPTCHA verification message
                    Toast.makeText(requireContext(), 
                        "Please complete the reCAPTCHA verification", 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void login() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Validate inputs
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            return;
        }

        // Show loading state
        btnLogin.setEnabled(false);
        btnGoogleSignIn.setEnabled(false);

        authViewModel.signInWithEmail(email, password);
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
                    Log.d(TAG, "Google sign in success: " + account.getEmail());
                    authViewModel.signInWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google sign in failed", e);
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

    private void resetPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            return;
        }
        authViewModel.resetPassword(email);
    }

    private void navigateToSignUp() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_loginFragment_to_signupFragment);
    }

    private void navigateToHome() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_loginFragment_to_homeFragment);
    }
} 