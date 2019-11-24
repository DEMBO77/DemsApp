package com.demsit.dems;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.demsit.dems.Adapter.AddContributorsAdapter;
import com.demsit.dems.Adapter.ContactAdapter;
import com.demsit.dems.Model.User;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddContributorsActivity extends AppCompatActivity {

    private Mydb db;
    private DatabaseReference contactsRef;
    private String teamId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contributor);
        init();
        teamId = getIntent().getStringExtra("TEAM_UID");
        db = Mydb.getInstance();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(db.user.getUid());
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setQuery(contactsRef, User.class).build();
        AddContributorsAdapter adapter = new AddContributorsAdapter(options, teamId);
        RecyclerView rv = findViewById(R.id.rv_add_contributors);
        rv.hasFixedSize();
        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv.setAdapter(adapter);
        adapter.startListening();
    }

    private void init() {
    }
}
