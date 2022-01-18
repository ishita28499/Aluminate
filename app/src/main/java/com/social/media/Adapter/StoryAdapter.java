package com.social.media.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.social.media.Model.User;
import com.social.media.Model.UserStories;
import com.social.media.R;
import com.social.media.Model.Story;
import com.social.media.ReceiverProfile;
import com.social.media.databinding.StoryRvDesignBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder> {

    ArrayList<Story> list;
    Context context;

    public StoryAdapter(ArrayList<Story> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_rv_design, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Story story = list.get(position);

        //Check if any story available
        if (story.getStories().size() > 0) {
            //Get last story
            UserStories lastStory = story.getStories().get(story.getStories().size() - 1);
            //Set last story image
            Picasso.get()
                    .load(lastStory.getImage())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.binding.storyImg);
            //set story count
            holder.binding.statusCircle.setPortionsCount(story.getStories().size());

            //Get User data from database based on StoryBy
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(story.getStoryBy()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    //Set user profile
                    assert user !=null;
                    if (user.getProfile().equals("")){
                        holder.binding.profileImage.setImageResource(R.drawable.original);
                    }else
                    {
                        Picasso.get().load(user.getProfile())
                                .placeholder(R.drawable.placeholder)
                                .into(holder.binding.profileImage);
                    }
                    holder.binding.name.setText(user.getName());

                    //Open story in story view
                    holder.binding.storyImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Story array
                            ArrayList<MyStory> myStories = new ArrayList<>();
                            //Store all stories in array
                            for (UserStories stories : story.getStories()) {
                                myStories.add(new MyStory(
                                        stories.getImage()
                                ));
                            }

                            new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                                    .setStoriesList(myStories) // Required
                                    .setStoryDuration(10000)// Default is 2000 Millis (2 Seconds)
                                    .setTitleText(user.getName()) // Default is Hidden
                                    .setSubtitleText("") // Default is Hidden
                                    .setTitleLogoUrl(user.getProfile()) // Default is Hidden
                                    .setStoryClickListeners(new StoryClickListeners() {
                                        @Override
                                        public void onDescriptionClickListener(int position) {
                                            //your action
                                        }

                                        @Override
                                        public void onTitleIconClickListener(int position) {
                                            //your action
                                            Intent intent = new Intent(context, ReceiverProfile.class);
                                            intent.putExtra("userid",user.getUserID());
                                            context.startActivity(intent);
                                        }
                                    }) // Optional Listeners
                                    .build() // Must be called before calling show method
                                    .show();

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class viewHolder extends RecyclerView.ViewHolder {

        StoryRvDesignBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = StoryRvDesignBinding.bind(itemView);

        }
    }
}
