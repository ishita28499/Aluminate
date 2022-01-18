package com.social.media.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.social.media.Model.Follow;
import com.social.media.Model.Notification;
import com.social.media.Model.User;
import com.social.media.R;
import com.social.media.ReceiverProfile;
import com.social.media.databinding.UserSampleBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    Context context;
    ArrayList<User> list;

    public UserAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User user = list.get(position);

        //Set user profile
        if (user.getProfile().equals("")){
           holder.binding.profileImage.setImageResource(R.drawable.original);
        }else
        {
            Picasso.get().load(user.getProfile())
                    .placeholder(R.drawable.placeholder)
                    .into( holder.binding.profileImage);
        }
        holder.binding.name.setText(user.getName());
        holder.binding.profession.setText(user.getEmail());

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, ReceiverProfile.class);
            intent.putExtra("userid",user.getUserID());
            context.startActivity(intent);
        });


        isFollowing(user.getUserID(),holder.binding.followBtn);

        holder.binding.followBtn.setOnClickListener(view -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (holder.binding.followBtn.getText().toString().equals("follow")) {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(user.getUserID()).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserID())
                        .child("followers").child(firebaseUser.getUid()).setValue(true);

            } else {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("following").child(user.getUserID()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserID())
                        .child("followers").child(firebaseUser.getUid()).removeValue();
            }
        });
    }



    @Override
    public int getItemCount() {
        return list.size();

    }

    private void isFollowing(final String userid, final Button button){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser !=null;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()){
                    button.setText("following");
                    button.setBackgroundResource(R.drawable.following_btn_bg);
                } else{
                    button.setText("follow");
                    button.setBackgroundResource(R.drawable.follow_btn_bg);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        UserSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserSampleBinding.bind(itemView);
        }
    }
}
