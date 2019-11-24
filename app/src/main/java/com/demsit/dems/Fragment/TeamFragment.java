package com.demsit.dems.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demsit.dems.Adapter.ContactAdapter;
import com.demsit.dems.Adapter.TeamAdapter;
import com.demsit.dems.Model.Team;
import com.demsit.dems.Model.User;
import com.demsit.dems.R;
import com.demsit.dems.TeamChatActivity;
import com.demsit.dems.ViewModel.TeamVM;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class TeamFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference TeamRef;
    private RecyclerView.Adapter teamAdapter;
    private TeamVM teamVM;
    private View view;
    private DatabaseReference teamURef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_team, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        teamURef = FirebaseDatabase.getInstance().getReference().child("TeamUsers").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Team>().setQuery(teamURef.orderByChild("lastMessage"), Team.class).build();
        TeamAdapter adapter = new TeamAdapter(options);
        RecyclerView rv = view.findViewById(R.id.rv_teams);
        rv.hasFixedSize();
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setAdapter(adapter);
        adapter.startListening();
    }
}
