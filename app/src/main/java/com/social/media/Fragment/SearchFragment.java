package com.social.media.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.social.media.Adapter.UserAdapter;
import com.social.media.Model.User;
import com.social.media.databinding.FragmentSearchBinding;

import java.util.ArrayList;


public class SearchFragment extends Fragment {

    FragmentSearchBinding binding;
    ArrayList<User> list = new ArrayList<>();
    UserAdapter adapter;
    FirebaseAuth auth;
    FirebaseDatabase database;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        //Show Shimmer effect until data loads
        binding.usersRV.showShimmerAdapter();

        //Users Recycler View
        adapter = new UserAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.usersRV.setLayoutManager(layoutManager);

        readUsers();

        binding.inputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH){
                    searchUsers();
                }
                return true;
            }
        });

        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.inputSearch.getText().toString().isEmpty()){
                    searchUsers();
                }else
                {
                    readUsers();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Fetching Users data from database and set in user recyclerview

        return binding.getRoot();
    }

    private void searchUsers(){
        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("name")
                .startAt(binding.inputSearch.getText().toString())
                .endAt(binding.inputSearch.getText().toString()+"\uf8ff");
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    assert user !=null;
                    if (user.getName().contains(binding.inputSearch.getText().toString())){
                        list.add(user);
                    }
                }
                binding.usersRV.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers(){
        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    user.setUserID(dataSnapshot.getKey());
                    if (!dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                        list.add(user);
                    }
                }
                binding.usersRV.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                //Hide shimmer effect when data loaded
                binding.usersRV.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}