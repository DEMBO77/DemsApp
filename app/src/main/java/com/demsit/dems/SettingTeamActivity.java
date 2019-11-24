package com.demsit.dems;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.demsit.dems.Adapter.ContributorAdapter;
import com.demsit.dems.Adapter.TeamChatAdapter;
import com.demsit.dems.Model.Message;
import com.demsit.dems.Model.User;
import com.demsit.dems.ViewModel.TeamVM;
import com.demsit.dems.ViewModel.UserVM;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SettingTeamActivity extends AppCompatActivity {

    private String teamId;
    private EditText teamNameF, teamDescriptionF;
    private RecyclerView rv;
    private Button addContributor, updateTeam;
    private Toolbar toolbar;
    private TeamVM teamVM;
    private TextView teamNameB;
    private ContributorAdapter adapter;
    private ImageView teamDelete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_team);
        teamId = getIntent().getStringExtra("TEAM_UID");
        init();
        DatabaseReference contributorRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(teamId).child("Contributors");
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<User>().setQuery(contributorRef, User.class).build();
        adapter = new ContributorAdapter(options, teamId);
        rv = findViewById(R.id.rv_team_setting);
        rv.hasFixedSize();
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv.setAdapter(adapter);
        adapter.startListening();
    }

    private void init() {
        teamVM = new TeamVM(this.getApplication());
        teamNameF = findViewById(R.id.team_setting_name);
        teamDescriptionF = findViewById(R.id.team_setting_description);
        addContributor = findViewById(R.id.add_contributor);
        toolbar = findViewById(R.id.team_setting_bar);
        teamNameB = findViewById(R.id.team_setting_name_bar);
        updateTeam = findViewById(R.id.team_setting_update_button);
        teamDelete = findViewById(R.id.team_setting_delete);
        teamDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTeam();
            }
        });
        updateTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamVM.updateTeam(teamId, teamNameF.getText().toString(), teamDescriptionF.getText().toString());
            }
        });
        addContributor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toAddContributorIntent = new Intent(SettingTeamActivity.this, AddContributorsActivity.class);
                toAddContributorIntent.putExtra("TEAM_UID", teamId);
                startActivity(toAddContributorIntent);
            }
        });
        teamNameF.setInputType(InputType.TYPE_NULL);
        teamDescriptionF.setInputType(InputType.TYPE_NULL);
        Mydb.ref.child("Teams").child(teamId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("admin").getValue().toString().equals(Mydb.user.getUid())){
                        updateTeam.setVisibility(View.VISIBLE);
                        teamNameF.setInputType(InputType.TYPE_CLASS_TEXT);
                        teamDescriptionF.setInputType(InputType.TYPE_CLASS_TEXT);
                        addContributor.setVisibility(View.VISIBLE);
                        teamDelete.setVisibility(View.VISIBLE);
                    }
                    teamNameB.setText(dataSnapshot.child("project").getValue().toString());
                    teamNameF.setText(dataSnapshot.child("project").getValue().toString());
                    teamDescriptionF.setText(dataSnapshot.child("description").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        setSupportActionBar(toolbar);
    }

    private void deleteTeam() {
        LinearLayout layout = new LinearLayout(SettingTeamActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 8, 20, 8);

        AlertDialog.Builder ad = new AlertDialog.Builder(SettingTeamActivity.this, R.style.AlertDialog);
        ad.setTitle(getString(R.string.delete_team));

        final TextView textView = new TextView(this);
        textView.setPadding(20, 20, 20, 20);
        textView.setText(getString(R.string.alert_delete_team_text));

        layout.addView(textView);
        ad.setView(layout);

        ad.setPositiveButton(getString(R.string.alert_delete_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                teamVM.deleteTeam(teamId);
                Intent toMainIntent = new Intent(SettingTeamActivity.this, MainActivity.class);
                startActivity(toMainIntent);
            }
        });

        ad.setNegativeButton(getString(R.string.alert_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        ad.show();
    }
}
