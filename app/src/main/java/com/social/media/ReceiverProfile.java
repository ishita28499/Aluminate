package com.social.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.social.media.Model.Post;
import com.social.media.Model.User;
import com.social.media.databinding.ActivityReceiverProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ReceiverProfile extends AppCompatActivity {

    ActivityReceiverProfileBinding binding;

    DatabaseReference reference;
    FirebaseUser user;

    String userid;
    ArrayList<Post> postList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReceiverProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        userid = getIntent().getStringExtra("userid");

        user = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        setReceiverData();

        binding.imgChat.setOnClickListener(view -> {
            Intent intent = new Intent(ReceiverProfile.this,ChatActivity.class);
            intent.putExtra("userid",userid);
            startActivity(intent);
        });


        getFollowers();


    }
    private void setReceiverData(){
        reference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    User model = snapshot.getValue(User.class);
                    assert model !=null;

                    Objects.requireNonNull(getSupportActionBar()).setTitle(model.getName());
                    if (model.getProfile().equals("")){
                        binding.profileImage.setImageResource(R.drawable.placeholder);
                    }else
                    {
                        Picasso.get().load(model.getProfile())
                                .placeholder(R.drawable.placeholder)
                                .into(binding.profileImage);
                    }
                    binding.userName.setText(model.getName());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReceiverProfile.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFollowers(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(userid).child("followers");
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.followersCount.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReceiverProfile.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance()
                .getReference("Follow").child(userid).child("following");
        reference1.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                binding.followingCount.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReceiverProfile.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
                        if (post.getPostedBy().equals(userid)){
                            postList.add(post);

                            binding.postsCount.setText(String.valueOf(postList.size()));
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReceiverProfile.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });















    }
}