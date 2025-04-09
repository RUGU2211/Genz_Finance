package com.example.financemanager.ui.viewmodels;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.financemanager.data.entities.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AuthViewModel extends ViewModel {
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private static final String TAG = "AuthViewModel";

    public AuthViewModel() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        checkAuthState();
    }

    private void checkAuthState() {
        isAuthenticated.setValue(auth.getCurrentUser() != null);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }

    public void signInWithEmail(String email, String password) {
        isLoading.setValue(true);
        error.setValue(null);

        // First, check if we need to verify reCAPTCHA
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isAuthenticated.setValue(true);
                        isLoading.setValue(false);
                    } else {
                        Exception exception = task.getException();
                        if (exception != null) {
                            String errorMessage = exception.getMessage();
                            if (errorMessage != null && errorMessage.contains("RecaptchaAction")) {
                                // Handle reCAPTCHA verification
                                error.setValue("Please verify that you are not a robot");
                            } else {
                                error.setValue(errorMessage);
                            }
                        } else {
                            error.setValue("Authentication failed");
                        }
                        isLoading.setValue(false);
                    }
                });
    }

    public void signUpWithEmail(String email, String password, String name) {
        isLoading.setValue(true);
        error.setValue(null);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User newUser = new User();
                        newUser.setId(auth.getCurrentUser().getUid());
                        newUser.setEmail(email);
                        newUser.setName(name);
                        
                        db.collection("users").document(newUser.getId())
                                .set(newUser)
                                .addOnSuccessListener(aVoid -> {
                                    isLoading.setValue(false);
                                    isAuthenticated.setValue(true);
                                })
                                .addOnFailureListener(e -> {
                                    isLoading.setValue(false);
                                    error.setValue(e.getMessage());
                                });
                    } else {
                        isLoading.setValue(false);
                        error.setValue(task.getException() != null ? 
                            task.getException().getMessage() : "Registration failed");
                    }
                });
    }

    public void signInWithGoogle(String idToken) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        Log.d(TAG, "Starting Google Sign In process");

        if (idToken == null) {
            isLoading.setValue(false);
            errorMessage.setValue("Invalid ID token");
            Log.e(TAG, "Google Sign In failed: ID token is null");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) {
                        // Check if user exists in Firestore
                        db.collection("users").document(user.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    // Create new user profile
                                    Map<String, Object> userProfile = new HashMap<>();
                                    userProfile.put("id", user.getUid());
                                    userProfile.put("email", user.getEmail());
                                    userProfile.put("name", user.getDisplayName() != null ? 
                                        user.getDisplayName() : "User");
                                    userProfile.put("profileImagePath", user.getPhotoUrl() != null ? 
                                        user.getPhotoUrl().toString() : null);
                                    userProfile.put("totalBalance", 0.0);
                                    userProfile.put("totalIncome", 0.0);
                                    userProfile.put("totalExpense", 0.0);
                                    userProfile.put("createdAt", new Date());

                                    db.collection("users").document(user.getUid())
                                        .set(userProfile)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "New user profile created successfully");
                                            isAuthenticated.setValue(true);
                                            isLoading.setValue(false);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error creating user profile", e);
                                            errorMessage.setValue("Failed to create user profile: " + e.getMessage());
                                            isLoading.setValue(false);
                                        });
                                } else {
                                    Log.d(TAG, "Existing user profile found");
                                    isAuthenticated.setValue(true);
                                    isLoading.setValue(false);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error checking user profile", e);
                                errorMessage.setValue("Failed to verify user profile: " + e.getMessage());
                                isLoading.setValue(false);
                            });
                    }
                } else {
                    Log.e(TAG, "Google sign in failed", task.getException());
                    errorMessage.setValue("Google sign in failed: " + 
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    isLoading.setValue(false);
                }
            });
    }

    public void signOut() {
        auth.signOut();
        isAuthenticated.setValue(false);
    }

    public void resetPassword(String email) {
        isLoading.setValue(true);
        error.setValue(null);

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    isLoading.setValue(false);
                    if (!task.isSuccessful()) {
                        error.setValue(task.getException() != null ? 
                            task.getException().getMessage() : "Password reset failed");
                    }
                });
    }
} 