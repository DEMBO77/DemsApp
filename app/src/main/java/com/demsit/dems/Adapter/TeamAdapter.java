package com.demsit.dems.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.demsit.dems.Model.Team;
import com.demsit.dems.Mydb;
import com.demsit.dems.R;
import com.demsit.dems.TeamChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TeamAdapter extends FirebaseRecyclerAdapter<Team, TeamAdapter.TeamViewHolder> {

    private DatabaseReference teamRef, teamUsersRef;
    private Mydb db;
    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public TeamAdapter(@NonNull FirebaseRecyclerOptions<Team> options) {
        super(options);
        db = Mydb.getInstance();
        teamRef = db.ref.child("Teams");
        teamUsersRef = db.ref.child("TeamUsers");
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_team_item, parent, false);
        return new TeamViewHolder(view);
    }



    @Override
    protected void onBindViewHolder(@NonNull final TeamViewHolder viewHolder,final int i, @NonNull Team team) {
        final String teamId = getRef(i).getKey(), currentUserId = getRef(i).getParent().getKey();
        teamUsersRef.child(currentUserId).child(teamId).child("seen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getValue().toString().equals("not_seen")){
                    viewHolder.newMessage.setVisibility(View.VISIBLE);
                }else if(dataSnapshot.getValue().toString().equals("seen")){
                    viewHolder.newMessage.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        teamRef.child(teamId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    viewHolder.project.setText(dataSnapshot.child("project").getValue().toString());
                    viewHolder.description.setText(dataSnapshot.child("description").getValue().toString());
                    final Context c = viewHolder.itemView.getContext();
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            teamUsersRef.child(currentUserId).child(teamId).child("seen").setValue("seen");
                            Intent teamChatIntent = new Intent(c, TeamChatActivity.class);
                            teamChatIntent.putExtra("TEAM_UID", teamId);
                            c.startActivity(teamChatIntent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    class TeamViewHolder extends RecyclerView.ViewHolder{
        TextView project, description, newMessage;
        TeamViewHolder(View itemView){
            super(itemView);
            this.project = itemView.findViewById(R.id.team_project);
            this.description = itemView.findViewById(R.id.team_desc);
            newMessage = itemView.findViewById(R.id.new_message);
        }

    }

}
