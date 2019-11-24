package com.demsit.dems.Repository;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.demsit.dems.Model.Message;
import com.demsit.dems.Mydb;
import com.demsit.dems.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class TeamRepository {

    public static  Mydb db;
    public static  Application app;
    public static TeamRepository instance;



    public synchronized  static TeamRepository getInstance(Application application){
        if(instance == null){
            instance = new TeamRepository();
            db = Mydb.getInstance();
            app = application;
        }
        return instance;
    }

    public void createTeam(String p, String d){
        final String newTeamKey = db.ref.child("Teams").push().getKey();
        String userId = db.user.getUid();
        Map team= new HashMap<>();
        team.put("admin", db.mAuth.getCurrentUser().getUid());
        team.put("project", p);
        team.put("description", d);
        Map map = new HashMap(), map2 = new HashMap();
        map2.put("status", "active");
        map.put(userId, map2);
        team.put("Contributors", map);
        db.ref.child("Teams").child(newTeamKey).setValue(team).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Map contributorSaved = new HashMap<>();
                    contributorSaved.put("status", "active");
                    contributorSaved.put("lastMessage", db.d2099 - Calendar.getInstance().getTimeInMillis());
                    contributorSaved.put("seen", "not_seen");
                    db.ref.child("TeamUsers").child(db.user.getUid()).child(newTeamKey).setValue(contributorSaved);
                    showMessage(R.string.team_created_successfully);
                }else{
                    showMessage(task.getException().toString());

                }
            }
        });
    }


    private void showMessage(int id){
        Toast.makeText(app.getApplicationContext(), app.getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(String s){
        Toast.makeText(app.getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void sendMessage(final String teamUID, final String message){
        if(TextUtils.isEmpty(message)){
            showMessage(R.string.field_empty);
        }else{
            Calendar calD =  Calendar.getInstance();
            SimpleDateFormat dateF = new SimpleDateFormat("MMM dd, yyyy");
            final String cDate = dateF.format(calD.getTime());
            Calendar calT =  Calendar.getInstance();
            SimpleDateFormat dateT = new SimpleDateFormat("hh:mm a");
            final String cTime = dateT.format(calT.getTime());

            final String userId = db.user.getUid();
            final DatabaseReference teamRef = db.ref.child("Teams").child(teamUID);
            final DatabaseReference teamMessRef = teamRef.child("Messages");
            final String messageKey = teamMessRef.push().getKey();

            db.ref.child("Users").child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && !dataSnapshot.getValue().toString().equals("")){
                        final Map<String, Object> messageInfo = getMessage(message, db.user.getUid(), dataSnapshot.getValue().toString(), "text", "none");
                        teamMessRef.child(messageKey).setValue(messageInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    updateLastMessage(teamUID);
                                }
                            }
                        });

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }




    public HashMap<String, Object> getMap(Message obj){
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : Message.class.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object put = field.get(obj);
                map.put(field.getName(), put);
            } catch (IllegalAccessException e) {

            }
        }
        return map;
    }


    public void updateTeam(final String teamId,final String name, final String desc) {
        if(!TextUtils.isEmpty(name)){
            db.ref.child("Teams").child(teamId).child("project").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.getValue().toString().equals(name)){
                        db.ref.child("Teams").child(teamId).child("project").setValue(name);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        if(!TextUtils.isEmpty(desc)){
            db.ref.child("Teams").child(teamId).child("description").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.getValue().toString().equals(desc)){
                        db.ref.child("Teams").child(teamId).child("description").setValue(desc);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void deleteTeam(final String teamId) {
        DatabaseReference teamUsersRef = db.ref.child("TeamUsers");
        db.ref.child("Teams").child(teamId).child("Contributors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map toDel = new HashMap();
                List<String> contributors = new ArrayList<String>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    contributors.add(child.getKey());
                }
                for(String c: contributors){
                    toDel.put("TeamUsers/"  + c + "/" + teamId + "/", null);
                }
                db.ref.updateChildren(toDel).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            db.ref.child("Teams").child(teamId).removeValue();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void sendFile(final Uri uri, final String selectedFile, final String teamUID) {
        Cursor returnCursor =
                app.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        final String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        final String senderId = db.user.getUid(),
                messRef = "Teams/"+ teamUID + "/Messages";
        DatabaseReference messageKeyRef = db.ref.child("Teams").child(teamUID).child("Messages").push();
        final String messageId = messageKeyRef.getKey();

        final StorageReference filePath = db.storageRef.child("DocFiles").child(messageId+"."+selectedFile);
        db.ref.child("Users").child(senderId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !dataSnapshot.getValue().toString().equals("")){
                    final String name = dataSnapshot.getValue().toString();
                    filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Calendar calD =  Calendar.getInstance();
                                    SimpleDateFormat dateF = new SimpleDateFormat("MMM dd, yyyy");
                                    String cDate = dateF.format(calD.getTime());
                                    Calendar calT =  Calendar.getInstance();
                                    SimpleDateFormat dateT = new SimpleDateFormat("hh:mm a");
                                    String cTime = dateT.format(calT.getTime());
                                    Map messageInfo = getMessage(uri.toString(), senderId, name, selectedFile, fileName), messageToRef = new HashMap();
                                    messageToRef.put(messRef + "/" + messageId, messageInfo);
                                    db.ref.updateChildren(messageToRef).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(!task.isSuccessful()){
                                                showMessage(task.getException().getMessage().toString());
                                            }else{
                                                updateLastMessage(teamUID);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void sendImage(final Uri uri1, final String teamUID) {
        final String senderId = db.user.getUid(),
                messRef = "Teams/"+ teamUID + "/Messages";
        DatabaseReference messageKeyRef = db.ref.child("Teams").child(teamUID).child("Messages").push();
        final String messageId = messageKeyRef.getKey();

        final StorageReference filePath = db.storageRef.child("Images").child(messageId+".jpg");
        db.ref.child("Users").child(senderId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if(dataSnapshot.exists() && !dataSnapshot.getValue().toString().equals("")){
                     final String name =  dataSnapshot.getValue().toString();
                     filePath.putFile(uri1).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                             filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                 @Override
                                 public void onSuccess(Uri uri) {
                                     Calendar calD = Calendar.getInstance();
                                     SimpleDateFormat dateF = new SimpleDateFormat("MMM dd, yyyy");
                                     String cDate = dateF.format(calD.getTime());
                                     Calendar calT = Calendar.getInstance();
                                     SimpleDateFormat dateT = new SimpleDateFormat("hh:mm a");
                                     String cTime = dateT.format(calT.getTime());
                                     Map messageInfo =getMessage(uri.toString(), senderId, name, "image", "none"), messageToRef = new HashMap();
                                     messageToRef.put(messRef + "/" + messageId, messageInfo);
                                     db.ref.updateChildren(messageToRef).addOnCompleteListener(new OnCompleteListener() {
                                         @Override
                                         public void onComplete(@NonNull Task task) {
                                             if (!task.isSuccessful()) {
                                                 showMessage(task.getException().getMessage().toString());
                                             } else {
                                                 updateLastMessage(teamUID);
                                             }
                                         }
                                     });
                                 }
                             });
                         }
                     });
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });

    }

    private void updateLastMessage(final String teamUID){
        final String userId = db.user.getUid();
        final long lastMessage = db.d2099 - Calendar.getInstance().getTimeInMillis();


        db.ref.child("Teams").child(teamUID).child("Contributors").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map updateLastMess = new HashMap();
                List<String> contributors = new ArrayList<String>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    contributors.add(child.getKey());
                }
                for(String c: contributors){
                    updateLastMess.put("TeamUsers/"  + c + "/" + teamUID + "/lastMessage/", lastMessage);
                    if(!c.equals(userId)){
                        updateLastMess.put("TeamUsers/"  + c + "/" + teamUID + "/seen/", "not_seen");
                    }else{
                        updateLastMess.put("TeamUsers/"  + c + "/" + teamUID + "/seen/", "seen");
                    }

                }
                db.ref.updateChildren(updateLastMess);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Map getMessage(String content, String sender, String senderName, String type, String info){
        Calendar calD =  Calendar.getInstance();
        SimpleDateFormat dateF = new SimpleDateFormat("MMM dd, yyyy");
        String cDate = dateF.format(calD.getTime());
        Calendar calT =  Calendar.getInstance();
        SimpleDateFormat dateT = new SimpleDateFormat("hh:mm a");
        String cTime = dateT.format(calT.getTime());
        Map toR = new HashMap();
        toR.put("content", content);
        toR.put("date", cDate);
        toR.put("time", cTime);
        toR.put("sender", sender);
        toR.put("senderName", senderName);
        toR.put("type", type);
        toR.put("info", info);
        return toR;
    }
}
