package com.social.media.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.social.media.Adapter.NotificationAdapter;
import com.social.media.Model.Notification;
import com.social.media.R;

import java.util.ArrayList;


public class RequestFrag extends Fragment {

    RecyclerView requestRv;
    ArrayList<Notification> list;

    public RequestFrag() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        requestRv = view.findViewById(R.id.requestRecyclerView);

        list = new ArrayList<>();
        /*list.add(new Notification(R.drawable.nature,"<b>Alicia Addams</b> Send you a friend request","just now"));
        list.add(new Notification(R.drawable.deaf,"<b>Janeeleona</b> Send you a friend request","40 minutes ago"));
        list.add(new Notification(R.drawable.cover,"<b>Katherinn</b> Suggested for you","2 hours"));
        list.add(new Notification(R.drawable.dennis,"<b>Alicia Addams</b> Send you a friend request","3 hours"));
        list.add(new Notification(R.drawable.profile,"<b>Janeeleona</b> Suggested for you","3 hours"));
        list.add(new Notification(R.drawable.nature1,"<b>Alicia Addams</b> Send you a friend request","3 hours"));
        list.add(new Notification(R.drawable.hipster,"<b>Alicia Addams</b> Send you a friend request","3 hours"));
        list.add(new Notification(R.drawable.art,"<b>Alicia Addams</b> Send you a friend request","3 hours"));
*/

        NotificationAdapter adapter = new NotificationAdapter(list, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        requestRv.setLayoutManager(layoutManager);
        requestRv.setNestedScrollingEnabled(false);
        requestRv.setAdapter(adapter);

        return view;
    }
}