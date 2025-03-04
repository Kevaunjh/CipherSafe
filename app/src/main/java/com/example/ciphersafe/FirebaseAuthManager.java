package com.example.ciphersafe;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseAuthManager {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    public FirebaseAuthManager() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public interface FirebaseAuthListener {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public void registerUser(String email, String password, String username, FirebaseAuthListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d("FirebaseAuth", "User registered successfully: " + user.getEmail());

                            // Save user data to Realtime Database
                            saveUserToDatabase(user.getUid(), email, username, listener);
                        }
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            Log.e("FirebaseAuth", "Registration failed: " + e.getMessage(), e);
                            listener.onError(e.getMessage());
                        } else {
                            Log.e("FirebaseAuth", "Registration failed with unknown error.");
                            listener.onError("Unknown error.");
                        }
                    }
                });
    }

    private void saveUserToDatabase(String userId, String email, String username, FirebaseAuthListener listener) {
        // Create a user object
        User user = new User(email, username);

        // Save to Realtime Database
        databaseReference.child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseAuth", "User data saved to Realtime Database");
                    listener.onSuccess("Registration successful!");
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseAuth", "Failed to save user data to Realtime Database: " + e.getMessage());
                    listener.onError("Failed to save user data.");
                });
    }

    public void activateUser(String username, String password, FirebaseAuthListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Find the email associated with the username
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String email = document.getString("email");

                            if (email != null) {
                                // Now log in using email and password
                                mAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(signInTask -> {
                                            if (signInTask.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                    Log.d("FirebaseAuth", "User logged in: " + user.getEmail());
                                                    listener.onSuccess("Login successful!");
                                                }
                                            } else {
                                                String errorMessage = signInTask.getException() != null ?
                                                        signInTask.getException().getMessage() : "Login failed.";
                                                Log.e("FirebaseAuth", "Login failed: " + errorMessage);
                                                listener.onError(errorMessage);
                                            }
                                        });
                            } else {
                                listener.onError("Email not found for this username.");
                            }
                            break; // Exit loop after first match
                        }
                    } else {
                        listener.onError("Username not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseAuth", "Error fetching username: " + e.getMessage(), e);
                    listener.onError("Failed to fetch username.");
                });
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}
