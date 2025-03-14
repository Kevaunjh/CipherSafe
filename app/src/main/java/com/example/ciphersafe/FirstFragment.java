package com.example.ciphersafe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.ciphersafe.databinding.FragmentFirstBinding;
import com.example.ciphersafe.security.SecurityManager;
import com.example.ciphersafe.FirebaseAuthManager;
import com.google.firebase.auth.FirebaseAuth;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private SecurityManager securityManager;
    private FirebaseAuthManager authManager;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            securityManager = ((MainActivity) getActivity()).getSecurityManager();
            authManager = ((MainActivity) getActivity()).getFirebaseAuthManager();
        }

        binding.loginButton.setOnClickListener(v -> attemptLogin());

        binding.registerButton.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        binding.biometricButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).authenticateUser();
            }
        });
    }

    private void attemptLogin() {
        FirebaseAuth.getInstance().signOut();
        authManager = ((MainActivity) getActivity()).getFirebaseAuthManager();

        String email = binding.usernameInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        authManager.activateUser(email, password, new FirebaseAuthManager.FirebaseAuthListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_to_userProfileFragment);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
