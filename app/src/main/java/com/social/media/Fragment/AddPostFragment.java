package com.social.media.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.social.media.Model.Post;
import com.social.media.Model.User;
import com.social.media.R;
import com.social.media.databinding.FragmentAddPostBinding;
import com.squareup.picasso.Picasso;

import java.util.Date;


public class AddPostFragment extends Fragment {

    FragmentAddPostBinding binding;
    Uri uri;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog dialog;

    public AddPostFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddPostBinding.inflate(inflater, container, false);

        //Loading dialog
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("Please Wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        //Get user data from database
        database.getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User user = snapshot.getValue(User.class);
                    assert user !=null;
                    //Set user profile using Picasso
                    if (user.getProfile().equals("")){
                        binding.profileImage.setImageResource(R.drawable.original);
                    }else
                    {
                        Picasso.get().load(user.getProfile())
                                .placeholder(R.drawable.placeholder)
                                .into(binding.profileImage);
                    }
                    //Set user name and profession
                    binding.name.setText(user.getName());
                    binding.profession.setText(user.getProfession());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Check if User add description or not
        binding.postDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String description = binding.postDescription.getText().toString();
                if (!description.isEmpty()){
                    //Enable post Button as user start typing
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn_bg));
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white));
                    binding.postBtn.setEnabled(true);
                }else {
                    //Disable post Button if User remove description
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_active_btn));
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.gray));
                    binding.postBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        binding.addImg.setOnClickListener(v -> {
            //Open Gallery using intent
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 10);
        });

        binding.postBtn.setOnClickListener(v -> {

            //Call method we created bellow for keyboard popping up automatically
            hideKeyboard();

            //Show loading dialog
            dialog.show();
            //Define storage reference for post image in Firebase Storage
            final StorageReference reference = storage.getReference().child("posts")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child(new Date().getTime()+"");
            //Store image in reference you define above
            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Get image URL from Storage and store a copy of image in Firebase database
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //Set post data to Post object
                            Post post = new Post();
                            post.setPostImage(uri.toString());
                            post.setPostedBy(FirebaseAuth.getInstance().getUid());
                            post.setPostDescription(binding.postDescription.getText().toString());
                            post.setPostedAt(new Date().getTime());

                            //Save post data in database
                            database.getReference().child("posts")
                                    .push()
                                    .setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Hide loading dialog as post uploaded
                                    dialog.dismiss();
                                    //Hide post image as post uploaded
                                    binding.postImage.setVisibility(View.GONE);
                                    binding.postDescription.setText("");
                                    Toast.makeText(getContext(), "Posted Successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            });
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check User select any image
        if (data.getData() != null){
            uri = data.getData();
            //set post image user select from gallery
            binding.postImage.setImageURI(uri);
            binding.postImage.setVisibility(View.VISIBLE);

            //Enable post button and change background color
            binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn_bg));
            binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.postBtn.setEnabled(true);
        }
    }

    //Stop keyboard popping up automatically
    public void hideKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}