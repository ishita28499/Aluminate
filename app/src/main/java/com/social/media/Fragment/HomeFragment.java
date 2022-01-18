package com.social.media.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.social.media.Adapter.PostAdapter;
import com.social.media.ChatListActivity;
import com.social.media.Model.Post;
import com.social.media.Model.UserStories;
import com.social.media.R;
import com.social.media.Adapter.StoryAdapter;
import com.social.media.Model.Story;

import java.util.ArrayList;
import java.util.Date;


public class HomeFragment extends Fragment {
    //Define your views here
    //We are not using Binding here thats why we need to define our views
    ShimmerRecyclerView dashboardRV,storyRV;
    ArrayList<Story> storyList;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    RoundedImageView addStoryImage;
    ActivityResultLauncher<String> galleryLauncher;
    ProgressDialog dialog;
    ConstraintLayout group;
    NestedScrollView scrollView;

    ImageView imgChat;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //Initializing views
        scrollView = view.findViewById(R.id.scroll);
        storyRV = view.findViewById(R.id.storyRV);
        dashboardRV = view.findViewById(R.id.dashboardRV);
        dashboardRV.showShimmerAdapter();
        storyRV.showShimmerAdapter();
        group = view.findViewById(R.id.group);
        imgChat = view.findViewById(R.id.imgChat);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        //Loading dialog
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        imgChat = view.findViewById(R.id.imgChat);
        imgChat.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), ChatListActivity.class);
            startActivity(intent);
        });



        //Story Recycler View
        storyList = new ArrayList<>();
        StoryAdapter adapter = new StoryAdapter(storyList, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        storyRV.setLayoutManager(layoutManager);
        storyRV.setNestedScrollingEnabled(false);

        //Fetching Story data from database and set in story recyclerview
        database.getReference()
                .child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
                if (snapshot != null){
                    storyList.clear();
                    for (DataSnapshot storySnapshot :snapshot.getChildren()){
                        Story story = new Story();
                        story.setStoryBy(storySnapshot.getKey());
                        story.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));

                        ArrayList<UserStories> stories = new ArrayList<>();
                        for (DataSnapshot snapshot1 : storySnapshot.child("userStories").getChildren()){
                            UserStories userStories = snapshot1.getValue(UserStories.class);
                            stories.add(userStories);
                        }
                        story.setStories(stories);
                        storyList.add(story);
                    }
                    storyRV.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    //Hide shimmer adapter when data load
                    storyRV.hideShimmerAdapter();
                    group.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
            });//End of story Recycler View


        //Post Recycler View
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, getContext(),getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true);
        dashboardRV.setLayoutManager(linearLayoutManager);
        dashboardRV.addItemDecoration(new DividerItemDecoration(dashboardRV.getContext(), DividerItemDecoration.VERTICAL));
        dashboardRV.setNestedScrollingEnabled(false);

        //Fetching Post data from database and set in post recyclerview
        database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postList.add(post);
                }
                dashboardRV.setAdapter(postAdapter);
                dashboardRV.hideShimmerAdapter();
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });//End of post Recycler View


        //Upload Story
        addStoryImage = view.findViewById(R.id.storyImg);
        addStoryImage.setOnClickListener(v -> {
            //Call gallery launcher and set input
            galleryLauncher.launch("image/*");
        });

        //Open gallery for images
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent()
                , new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        addStoryImage.setImageURI(result);

                        //Show loading dialog as user select image
                        dialog.show();
                        //Define storage reference for story image in Firebase Storage
                        final StorageReference reference = storage.getReference()
                                .child("stories")
                                .child(FirebaseAuth.getInstance().getUid())
                                .child(new Date().getTime() + "");
                        //Store image in reference you define above
                        reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                //Get image URL from Storage and store a copy of image in Firebase database
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Story story = new Story();
                                        story.setStoryAt(new Date().getTime());

                                        //Create a stories child in database
                                        database.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("postedBy")
                                                .setValue(story.getStoryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Set story image and time
                                                UserStories stories = new UserStories(uri.toString(), story.getStoryAt());

                                                //Save story data in database
                                                database.getReference()
                                                        .child("stories")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .child("userStories")
                                                        .push()
                                                        .setValue(stories).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });

                    }
                });


        return view;
    }
}