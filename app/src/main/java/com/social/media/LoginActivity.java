package com.social.media;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.social.media.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth auth;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();


        binding.loginBtn.setOnClickListener(v -> {
            String email = binding.emailET.getText().toString();
            String password = binding.passwordET.getText().toString();
            //Sign in User with email and password

            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(LoginActivity.this, "Enter valid email address", Toast.LENGTH_SHORT).show();
            }else
            {
                ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Logging..");
                progressDialog.setCancelable(false);
                progressDialog.show();
                auth.signInWithEmailAndPassword(email , password).
                        addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                //Open Main Activity when user login successfully
                                Intent intent= new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        });
        binding.goToSignup.setOnClickListener(v -> {
            //Open sign up Activity
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }


}