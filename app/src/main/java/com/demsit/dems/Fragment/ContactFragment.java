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

import com.demsit.dems.Adapter.ContactAdapter;
import com.demsit.dems.Adapter.RequestAdapter;
import com.demsit.dems.Model.User;
import com.demsit.dems.Mydb;
import com.demsit.dems.R;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class ContactFragment extends Fragment {

    private View view;
    private DatabaseReference contactsRef;
    private FirebaseAuth mAuth;
    private String userId;
    private Mydb db;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_contact, container, false);
        db = Mydb.getInstance();
        userId = db.user.getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(userId);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setQuery(contactsRef.orderByChild("lastMessage"), User.class).build();
        ContactAdapter adapter = new ContactAdapter(options);
        RecyclerView rv = view .findViewById(R.id.rv_contacts);
        rv.hasFixedSize();
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);
        adapter.startListening();
    }
}
