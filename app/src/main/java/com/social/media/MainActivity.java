package com.social.media;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.social.media.Fragment.AddPostFragment;
import com.social.media.Fragment.HomeFragment;
import com.social.media.Fragment.NotificationFragment;
import com.social.media.Fragment.Profile2Fragment;
import com.social.media.Fragment.SearchFragment;
import com.social.media.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Setting Custom toolbar
        setSupportActionBar(binding.toolbar);
        //Set title in toolbar
        MainActivity.this.setTitle("My Profile");

        //Show Home fragment by default
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        binding.toolbar.setVisibility(View.GONE);
        transaction.replace(R.id.content, new HomeFragment());
        transaction.commit();


        //Bottom Navigation
        binding.readableBottomBar.setOnItemSelectListener(i -> {

            //Replace Fragment
            FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
            switch (i) {
                case 0:
                    binding.toolbar.setVisibility(View.GONE);
                    transaction1.replace(R.id.content, new HomeFragment());
                    break;
                case 1:
                    binding.toolbar.setVisibility(View.GONE);
                    transaction1.replace(R.id.content, new NotificationFragment());
                    break;
                case 2:
                    transaction1.replace(R.id.content, new AddPostFragment());
                    binding.toolbar.setVisibility(View.GONE);
                    break;
                case 3:
                    transaction1.replace(R.id.content, new SearchFragment());
                    binding.toolbar.setVisibility(View.GONE);
                    break;
                case 4:
                    transaction1.replace(R.id.content, new Profile2Fragment());
                    binding.toolbar.setVisibility(View.VISIBLE);
                    break;

            }
            transaction1.commit();
        });

    }


    //Set option menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return true;
    }

    //Option menu On click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure want to logout?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            auth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}