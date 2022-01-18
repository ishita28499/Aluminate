package com.social.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.social.media.Model.User;
import com.social.media.databinding.ActivitySignUpBinding;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");

        binding.signUpBtn.setOnClickListener(v -> {


            String email = binding.emailET.getText().toString();
            String password = binding.passwordET.getText().toString();

            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(this, "Enter avlid email address", Toast.LENGTH_SHORT).show();
            }else
            {
                ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
                progressDialog.setMessage("Creating New Account..");
                progressDialog.setCancelable(false);
                progressDialog.show();

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    assert user !=null;
                                    saveData(user.getUid(),progressDialog);

                                }else
                                {
                                    progressDialog.dismiss();
                                    Toast.makeText(SignUpActivity.this, "Error", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }

        });

        binding.goToLogin.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void saveData(String uid, ProgressDialog progressDialog) {

        User user = new User();

        user.setName(binding.nameET.getText().toString());
        user.setEmail(binding.emailET.getText().toString());
        user.setPassword(binding.passwordET.getText().toString());
        user.setProfession(binding.professionET.getText().toString());
        user.setUserID(uid);
        user.setProfile("");
        user.setStatus("offline");
        user.setBio("Hello user welcome to this app. Hope you enjoy well!!");
        reference.child(uid).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                progressDialog.dismiss();
                Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
            }else
            {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to create account: "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });










    }
}