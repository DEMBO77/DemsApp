package com.demsit.dems.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demsit.dems.Adapter.RequestAdapter;
import com.demsit.dems.Model.User;
import com.demsit.dems.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class UserRequestFragment extends Fragment {

    private View view;
    private DatabaseReference requestRef;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_request, container, false);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests").child(userId);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setQuery(requestRef, User.class).build();
        RequestAdapter adapter = new RequestAdapter(options);
        RecyclerView rvRequest = view.findViewById(R.id.rv_user_request);
        rvRequest.hasFixedSize();
        rvRequest.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvRequest.setAdapter(adapter);
        adapter.startListening();

    }
}
