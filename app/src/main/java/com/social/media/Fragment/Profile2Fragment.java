package com.social.media.Fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.social.media.Adapter.FollowersAdapter;
import com.social.media.Model.Follow;
import com.social.media.Model.Post;
import com.social.media.R;
import com.social.media.Model.User;
import com.social.media.databinding.FragmentProfile2Binding;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class Profile2Fragment extends Fragment {

    ArrayList<Follow> list;
    FragmentProfile2Binding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FirebaseUser user;

    private Uri imageUri;
    String bio;

    ArrayList<Post> postList = new ArrayList<>();

    public Profile2Fragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding  = FragmentProfile2Binding.inflate(inflater, container, false);

        //My followers Recycler View
        list = new ArrayList<>();
        FollowersAdapter adapter = new FollowersAdapter(list,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.followersRV.setLayoutManager(layoutManager);
        binding.followersRV.setAdapter(adapter);

        //Fetching followers data from database and set in recyclerview
//        database.getReference().child("Users")
//                .child(auth.getUid())
//                .child("followers").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                list.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
//                    Follow follow = dataSnapshot.getValue(Follow.class);
//                    list.add(follow);
//                }
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });//End of Followers Recycler View


        //Fetch User data from database

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();


        setUserData();

        //Upload Cover Image from gallery
        binding.profileImage.setOnClickListener(v -> {
            //Open gallery using Intent
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 11);
        });

        binding.textEditBio.setOnClickListener(view -> {
            showUpdateDialogue();
        });

//        //Upload Profile Image from gallery
//        binding.verifiedAccount.setOnClickListener(v -> {
//            //Open gallery using Intent
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            intent.setType("image/*");
//            startActivityForResult(intent, 22);
//        });
        getFollowers();

        return binding.getRoot();
    }

    private void setUserData(){
        database.getReference().child("Users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            User user = snapshot.getValue(User.class);
                            assert user !=null;
                            //Set User cover image
                            bio = user.getBio();
                            if (user.getProfile().equals("")){
                                binding.profileImage.setImageResource(R.drawable.placeholder);
                            }else
                            {
                                Picasso.get()
                                        .load(user.getCoverPhoto())
                                        .placeholder(R.drawable.placeholder)
                                        .into(binding.profileImage);
                            }

                            //Set user info
                            binding.userName.setText(user.getName());
                            binding.profession.setText(user.getProfession());
                            binding.bio.setText(user.getBio());
//                            binding.followersCount.setText(user.getFollowerCount()+"");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Differentiate user select profile or cover through request code
        if (requestCode == 11){
            if (resultCode == RESULT_OK && data !=null){
                imageUri = data.getData();
                binding.profileImage.setImageURI(imageUri);

                uploadImageToStorage();

            }
        }else
        {
            Toast.makeText(getContext(), "No image selected!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToStorage() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Profiles")
                .child(user.getUid())
                .child(System.currentTimeMillis()+".jpg");

        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

            String url = uri.toString();

            HashMap<String,Object> map = new HashMap<>();
            map.put("profile",url);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Users");

            reference.child(user.getUid()).updateChildren(map).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                }else
                {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to upload image: "+ Objects.requireNonNull(task.getException())
                            .getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }));










    }

    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(user.getUid()).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.followersCount.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance()
                .getReference("Follow").child(user.getUid()).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                binding.followingCount.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("posts");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Post post = dataSnapshot.getValue(Post.class);
                        assert post !=null;
                        if (post.getPostedBy().equals(user.getUid())){
                            postList.add(post);

                            binding.postsCount.setText(String.valueOf(postList.size()));
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void showUpdateDialogue(){
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.update_bio_dialogue);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        EditText input = dialog.findViewById(R.id.input_bio);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        input.setText(bio);

        btnUpdate.setOnClickListener(view -> {

            HashMap<String,Object> map = new HashMap<>();
            map.put("bio",input.getText().toString());

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(user.getUid())
                    .updateChildren(map)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Bio updated!", Toast.LENGTH_SHORT).show();
                        }else
                        {
                            Toast.makeText(getContext(), "Unable to update: "+
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });



        dialog.show();
    }
}