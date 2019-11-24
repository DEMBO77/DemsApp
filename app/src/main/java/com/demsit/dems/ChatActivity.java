package com.demsit.dems;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.demsit.dems.Adapter.ChatAdapter;
import com.demsit.dems.Adapter.RequestAdapter;
import com.demsit.dems.Model.Message;
import com.demsit.dems.Model.User;
import com.demsit.dems.ViewModel.UserVM;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private String senderUID, receiverUID, receiverName, receiverImage;
    private DatabaseReference messageRef;
    private UserVM userVM;
    private EditText chatF;
    private ImageView sendButton, uploadFile;
    private TextView receiverNameT, barChatLastSeen, barChatContactState;
    private CircleImageView receiverImageView;
    private RecyclerView rv;
    private ChatAdapter adapter;
    private String selectedFile;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        senderUID = getIntent().getStringExtra("SENDER_UID");
        receiverUID = getIntent().getStringExtra("RECEIVER_UID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");
        receiverImage = getIntent().getStringExtra("RECEIVER_IMAGE");
        init();
        receiverNameT.setText(receiverName);
        Picasso.get().load(receiverImage).placeholder(R.drawable.ic_account).into(receiverImageView);
        messageRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(senderUID).child(receiverUID);
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(messageRef, Message.class).build();
        final List<Message> messages = new ArrayList<Message>();
        adapter = new ChatAdapter(receiverUID, messages);
        rv = findViewById(R.id.rv_chat);
        rv.hasFixedSize();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message mess = dataSnapshot.getValue(Message.class);
                mess.setUid(dataSnapshot.getKey());
                messages.add(mess);
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

    private void init() {
        userVM = ViewModelProviders.of(this).get(UserVM.class);
        chatF = findViewById(R.id.chat_field);
        sendButton = findViewById(R.id.send_chat_button);
        uploadFile = findViewById(R.id.upload_file_button);
        barChatContactState =  findViewById(R.id.bar_chat_contact_state);
        barChatLastSeen = findViewById(R.id.bar_chat_last_seen);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userVM.sendMessage(chatF.getText().toString(), receiverUID);
                chatF.setText("");
            }
        });
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFile();
            }
        });
        receiverNameT = findViewById(R.id.chat_receiver_name);
        receiverImageView = findViewById(R.id.chat_image);
        toolbar = findViewById(R.id.chat_bar);
        Mydb.ref.child("Users").child(receiverUID).child("State").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("status").getValue().toString().equals("online")){
                    barChatContactState.setBackgroundResource(R.drawable.online_bg);
                    barChatLastSeen.setText("");
                }else{
                    String date = dataSnapshot.child("date").getValue().toString();
                    String time = dataSnapshot.child("time").getValue().toString();
                    String ls = "";
                    if(!getDate().equals(date)){
                        ls+=date;
                    }
                    ls+=" "+time;
                    barChatContactState.setBackgroundResource(R.drawable.offline_bg);
                    barChatLastSeen.setText(ls);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        setSupportActionBar(toolbar);
    }

    private void selectFile() {
        CharSequence op[] = new CharSequence[]{"Images", "PDF", "WORD"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this, R.style.AlertDialog);
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
                userVM.sendFile(uri, selectedFile, receiverUID);
            }else if(selectedFile.equals("image")){
                userVM.sendImage(uri, receiverUID);
            }
        }
    }

    public String getDate(){
        Calendar calD =  Calendar.getInstance();
        SimpleDateFormat dateF = new SimpleDateFormat("MMM dd, yyyy");
        return dateF.format(calD.getTime());
    }
}
