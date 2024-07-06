package com.example.youtubeclone.fragment;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.youtubeclone.Adapter.ContentAdapter;
import com.example.youtubeclone.Models.ContentModel;
import com.example.youtubeclone.R;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<ContentModel> list;
    ContentAdapter adapter;

    DatabaseReference reference;



    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        reference = FirebaseDatabase.getInstance().getReference().child("Videos");

        getAllVideos();

        return view;
    }

    private void getAllVideos() {
        list = new ArrayList<>();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();

                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        ContentModel model = dataSnapshot.getValue(ContentModel.class);
                        list.add(model);
                    }

                    //shuffle the videos
                    Collections.shuffle(list);

                    adapter = new ContentAdapter(getActivity(), list);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }else {
                    Log.d("HomeFragment",list.toString());
                    Toast.makeText(getActivity(), "No Data Found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}