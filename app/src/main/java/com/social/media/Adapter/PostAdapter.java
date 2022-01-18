package com.social.media.Adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.social.media.CommentActivity;
import com.social.media.Model.Notification;
import com.social.media.Model.Post;
import com.social.media.Model.User;
import com.social.media.R;
import com.social.media.databinding.DashboardRvDesignBinding;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

    ArrayList<Post> list;
    Context context;
    Activity activity;

    Bitmap result = null;


    public PostAdapter(ArrayList<Post> list, Context context, Activity activity) {
        this.list = list;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_rv_design, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Post model = list.get(position);
        //Set post image
        Picasso.get()
                .load(model.getPostImage())
                .placeholder(R.drawable.placeholder)
                .into(holder.binding.postImage);
        //set post other info
        holder.binding.like.setText(model.getPostLike() + "");
        holder.binding.comment.setText(model.getCommentCount() + "");
        String description = model.getPostDescription();
        if (description != null)
        {
            if (description.equals("")) {
                //hide description if not available
                holder.binding.postDescription.setVisibility(View.GONE);
            } else {
                holder.binding.postDescription.setText(model.getPostDescription());
                holder.binding.postDescription.setVisibility(View.VISIBLE);
            }
        }


        new Thread(){
            @Override
            public void run() {
                try {
                    result = Picasso.get().load(model.getPostImage()).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();


        //Getting User data from database based on PostedBy
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(model.getPostedBy()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    //Set user profile
                    if (user.getProfile().equals("")){
                        holder.binding.profileImage.setImageResource(R.drawable.original);
                    }else
                    {
                        Picasso.get().load(user.getProfile())
                                .placeholder(R.drawable.placeholder)
                                .into(holder.binding.profileImage);
                    }
                    holder.binding.userName.setText(user.getName());
                    holder.binding.bio.setText(user.getProfession());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        //LIKE POST
        //Check if User already liked post
        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(model.getPostId())
                .child("likes")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            //If already liked simply changed drawable
                            //perform your action
                            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_2, 0, 0, 0);
                        } else {
                            //If Not liked yet
                            holder.binding.like.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    //Save user id who liked in database
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(model.getPostId())
                                            .child("likes")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //Increment by 1 in existing likes
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("posts")
                                                    .child(model.getPostId())
                                                    .child("postLike")
                                                    .setValue(model.getPostLike() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_heart_2, 0, 0, 0);

                                                    //Set Notification data to Notification object
                                                    Notification notification = new Notification();
                                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                    notification.setNotificationAt(new Date().getTime());
                                                    notification.setPostID(model.getPostId());
                                                    notification.setPostedBy(model.getPostedBy());
                                                    notification.setType("like");

                                                    //Save notification data in database
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("notification")
                                                            .child(model.getPostedBy())
                                                            .push()
                                                            .setValue(notification);
                                                }
                                            });
                                        }
                                    });

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open comment activity
                Intent intent = new Intent(context, CommentActivity.class);
                //Send data to Comment activity through Intent
                intent.putExtra("postId", model.getPostId());
                intent.putExtra("postedBy", model.getPostedBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

//        holder.binding.share.setOnClickListener(view -> {
//            if (checkExternalStorage()){
//                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),result,
//                        "img_"+System.currentTimeMillis(),null);
//                Uri bitmapUri = Uri.parse(path);
//
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("image/png");
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                intent.putExtra(Intent.EXTRA_STREAM,bitmapUri);
//                context.startActivity(Intent.createChooser(intent,"Choose app"));
//            }else
//            {
//                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
//                        ,100);
//            }
//        });
    }
//    private boolean checkExternalStorage(){
//        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
//        int res =context.checkCallingOrSelfPermission(permission);
//        return (res == PackageManager.PERMISSION_GRANTED);
//    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {

        DashboardRvDesignBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DashboardRvDesignBinding.bind(itemView);
        }
    }
}
