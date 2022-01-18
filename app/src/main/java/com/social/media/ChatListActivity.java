package com.social.media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.social.media.Adapter.MessageUserAdapter;
import com.social.media.Model.Chatlist;
import com.social.media.Model.User;
import com.social.media.databinding.ActivityChatListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatListActivity extends AppCompatActivity {


    ActivityChatListBinding binding;

    private MessageUserAdapter messageUserAdapter;
    private List<User> mUsers;

    FirebaseUser user;
    DatabaseReference reference;

    private List<Chatlist> usersList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();
        usersList = new ArrayList<>();

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(view -> {
            finish();
        });




        reference = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(user.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);
                }

                Collections.reverse(usersList);
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ChatListActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }


    private void chatList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    assert user !=null;
                    for (Chatlist chatlist : usersList){
                        if (user.getUserID().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }
                messageUserAdapter = new MessageUserAdapter(ChatListActivity.this, mUsers, true);
                binding.recyclerView.setAdapter(messageUserAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}