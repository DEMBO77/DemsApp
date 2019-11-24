package com.demsit.dems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.demsit.dems.Adapter.ChatAdapter;
import com.demsit.dems.Adapter.TeamChatAdapter;
import com.demsit.dems.Model.Message;
import com.demsit.dems.Model.Team;
import com.demsit.dems.ViewModel.TeamVM;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TeamChatActivity extends AppCompatActivity {

    private Team team;
    private TeamChatAdapter adapter;
    private RecyclerView rv;
    private String teamUID;
    private String userUID;
    private TeamVM teamVM;
    private ImageView sendMessageButton;
    private DatabaseReference teamChatRef;
    private EditText messageF;
    private Toolbar toolbar;
    private TextView teamName;
    private ImageView teamSetting, uploadFile;
    private String selectedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_chat);
        teamUID = getIntent().getStringExtra("TEAM_UID");
        userUID = Mydb.user.getUid();
        teamChatRef = Mydb.ref.child("Teams").child(teamUID).child("Messages");
        teamVM = ViewModelProviders.of(this).get(TeamVM.class);
        init();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(teamChatRef, Message.class).build();
        adapter = new TeamChatAdapter(options, teamUID);
        rv = findViewById(R.id.rv_team_chat);
        rv.hasFixedSize();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        adapter.startListening();
        teamChatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                rv.smoothScrollToPosition(adapter.getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    public void init(){
        sendMessageButton = findViewById(R.id.team_chat_button);
        messageF = findViewById(R.id.team_chat_field);
        uploadFile = findViewById(R.id.upload_file_button);
        teamName = findViewById(R.id.team_chat_name);
        teamSetting =  findViewById(R.id.team_chat_setting);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teamVM.sendMessage(teamUID, messageF.getText().toString());
                messageF.setText("");
            }
        });
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });
        toolbar = (Toolbar)findViewById(R.id.team_chat_bar);
        teamName = findViewById(R.id.team_chat_name);
        teamSetting = findViewById(R.id.team_chat_setting);
        setSupportActionBar(toolbar);
        Mydb.ref.child("Teams").child(teamUID).child("project").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    teamName.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        teamSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        teamSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingTeamItem = new Intent(TeamChatActivity.this, SettingTeamActivity.class);
                settingTeamItem.putExtra("TEAM_UID", teamUID);
                startActivity(settingTeamItem);
            }
        });
    }

    private void selectFile() {
        CharSequence op[] = new CharSequence[]{"Images", "PDF", "WORD"};
        AlertDialog.Builder builder = new AlertDialog.Builder(TeamChatActivity.this, R.style.AlertDialog);
        TextView title = new TextView(this);
        title.setText(getString(R.string.alert_select_file));
        title.setPadding(0,0,5, 5);
        title.setGravity(Gravity.CENTER);
        builder.setCustomTitle(title);
        builder.setItems(op, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0){
                    selectedFile = "image";
                    Intent imageFolder = new Intent();
                    imageFolder.setAction(Intent.ACTION_GET_CONTENT);
                    imageFolder.setType("image/*");
                    startActivityForResult(imageFolder.createChooser(imageFolder, getString(R.string.select_file_image_title)), 200);
                }else if(which ==1){
                    selectedFile = "pdf";
                    Intent pdfFolder = new Intent();
                    pdfFolder.setAction(Intent.ACTION_GET_CONTENT);
                    pdfFolder.setType("application/pdf");
                    startActivityForResult(pdfFolder.createChooser(pdfFolder, getString(R.string.select_file_pdf_title)), 200);
                }else{
                    selectedFile = "docx";
                    Intent docFolder = new Intent();
                    docFolder.setAction(Intent.ACTION_GET_CONTENT);
                    docFolder.setType("application/msword");
                    startActivityForResult(docFolder.createChooser(docFolder, getString(R.string.select_file_word_title)), 200);
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 200 && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            Uri uri = data.getData();
            if(!selectedFile.equals("image")){
                teamVM.sendFile(uri, selectedFile, teamUID);
            }else if(selectedFile.equals("image")){
                teamVM.sendImage(uri, teamUID);
            }
        }
    }

    public String getDate(){
        Calendar calD =  Calendar.getInstance();
        SimpleDateFormat dateF = new SimpleDateFormat("MMM dd, yyyy");
        return dateF.format(calD.getTime());
    }


}
