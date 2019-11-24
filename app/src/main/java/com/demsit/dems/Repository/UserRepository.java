package com.demsit.dems.Repository;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.demsit.dems.LoginNumberActivity;
import com.demsit.dems.MainActivity;
import com.demsit.dems.Model.User;
import com.demsit.dems.Mydb;
import com.demsit.dems.R;
import com.demsit.dems.RegistrationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UserRepository {

    public static Mydb db;
    public static Application app;
    public static UserRepository instance;
    public static MutableLiveData<User> user;


    public static synchronized UserRepository getInstance(Application application){
        if(instance == null){
            instance = new UserRepository();
            db = Mydb.getInstance();
            app = application;
            user = new MutableLiveData<User>();
        }return instance;
    }

    public void createUser(final String email, final String password, final AlertDialog pd){

        db.mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String userId = db.mAuth.getCurrentUser().getUid();
                    db.ref.child("Users").child(userId).setValue("");
                    db.ref.child("UserID").child(getProperEmail(email)).setValue(userId);
                    pd.dismiss();
                    showMessage(R.string.registration_success);
                    loginUser(email, password);

                }else{
                    pd.dismiss();
                    showMessage(task.getException().toString());
                }
            }
        });

    }

    public void loginUser(final String email, String password){
        db.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    final String userId = db.mAuth.getCurrentUser().getUid();
                    db.user = task.getResult().getUser();
                    toMainActivity();
                    showMessage(R.string.login_successfull);
                }else{
                    showMessage(task.getException().toString());
                }
            }
        });
    }

    private String getProperEmail(String email){
        String[] emailT = email.split("[@]");
        emailT[0]+="@";
        emailT[1] = emailT[1].replace('.', '%');
        return emailT[0]+emailT[1];
    }

    public void loginUser(final String email, String password, final AlertDialog pd){
        db.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    pd.dismiss();
                    db.user = task.getResult().getUser();
                    toMainActivity();
                    showMessage(R.string.login_successfull);
                }else{
                    pd.dismiss();
                    showMessage(task.getException().toString());
                }
            }
        });
    }

    public void updateUserInfo(String username, String userSpeciality, String image){
        String userId = db.mAuth.getCurrentUser().getUid();
        if(TextUtils.isEmpty(username)){
            showMessage("Your username has not been updated");
        }if(TextUtils.isEmpty(userSpeciality)){
            showMessage("Your speciality has not been updated");
        }else{
            Map<String, String> profile = new HashMap<>();
            profile.put("uid", userId);
            profile.put("name", username);
            profile.put("speciality", userSpeciality);
            profile.put("image", image);
            db.ref.child("Users").child(userId).setValue(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        showMessage(R.string.profile_updated);
                    }else{
                        showMessage(task.getException().toString());
                    }
                }
            });
        }
    }

    public LiveData<User> getUserInfo(){
        db.ref.child("Users").child(db.mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = (dataSnapshot.child("name").exists())?dataSnapshot.child("name").getValue().toString():null;
                String speciality = (dataSnapshot.child("speciality").exists())?dataSnapshot.child("speciality").getValue().toString():null;
                String picture = (dataSnapshot.child("image").exists())?dataSnapshot.child("image").getValue().toString():null;

                User userInfo = new User(dataSnapshot.getKey().toString(), name, speciality, picture);
                user.postValue(userInfo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return user;
    }

    public void signInWithNumber(final PhoneAuthCredential credential, final AlertDialog pd) {
        db.mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity)app.getApplicationContext(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            Log.d("uid", task.getResult().getUser().getUid());
                            final String userId = task.getResult().getUser().getUid();
                            showMessage(R.string.login_successfull);

                            db.ref.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.hasChild(userId)){
                                        db.ref.child("Users").child(userId).setValue("");
                                        db.ref.child("UserId").child(credential.toString()).setValue(userId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            toMainActivity();
                        } else {
                            showMessage(task.getException().toString());
                            pd.dismiss();

                        }
                    }
                });
    }

    public FirebaseUser getUser(){

        return db.mAuth.getCurrentUser();
    }

    public void signOut(){
        this.updateUserState(app.getString(R.string.offline));
        db.mAuth.signOut();

    }


    private void showMessage(int id){
        Toast.makeText(app.getApplicationContext(), app.getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(String s){
        Toast.makeText(app.getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void toMainActivity(){
        Intent sendToMain = new Intent(app, MainActivity.class);
        sendToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        app.startActivity(sendToMain);
    }

    public void storeImage(byte[]  imageBytes) {
        final StorageReference filePath = db.storageRef.child("ProfileImages").child(db.user.getUid() + ".jpg");
        filePath.putBytes(imageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        showMessage(R.string.profile_image_updated);
                        db.ref.child("Users").child(db.user.getUid()).child("image").setValue(uri.toString());
                    }
                });
            }
        });
    }


    public void searchUser(String userIdValue) {
        final String currentUserId = db.user.getUid();
        if(userIdValue.split("[@]").length>1){
            userIdValue = getProperEmail(userIdValue);
        }
        db.ref.child("UserId").child(userIdValue).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !dataSnapshot.getValue().toString().equals(currentUserId)){
                    db.ref.child("Requests").child(dataSnapshot.getValue().toString()).child(db.user.getUid()).child("request_status").setValue("received");
                }else{
                    showMessage("The user does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void sendMessage(final String message, final String receiver){
        if(!TextUtils.isEmpty(message)){
            final String senderId = db.user.getUid(),
                    messSenderRef = "Messages/"+ senderId + "/" + receiver,
            messReceiverRef  =  "Messages/"+ receiver +  "/" + senderId;
            DatabaseReference messageKeyRef = db.ref.child("Messages").child(senderId).child(receiver).push();
            String messageId = messageKeyRef.getKey();
            Map messageInfo = getMessage(message, senderId, " ", "text", "none"), messageToRef = new HashMap();
            messageToRef.put(messSenderRef + "/" + messageId, messageInfo);
            messageToRef.put(messReceiverRef + "/" + messageId, messageInfo);

            db.ref.updateChildren(messageToRef).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(!task.isSuccessful()){
                        showMessage(task.getException().getMessage().toString());
                    }else{
                        updateLastMessage(senderId, receiver);
                    }
                }
            });


        }
    }

    public void updateUserState(String state){
        Calendar calD =  Calendar.getInstance();
        SimpleDateFormat dateF = new SimpleDateFormat("MMM dd, yyyy");
        String cDate = dateF.format(calD.getTime());
        Calendar calT =  Calendar.getInstance();
        SimpleDateFormat dateT = new SimpleDateFormat("hh:mm a");
        String cTime = dateT.format(calT.getTime());
        Map userState = new HashMap();
        userState.put("date", cDate);
        userState.put("time", cTime);
        userState.put("status", state);
        db.ref.child("Users").child(db.user.getUid()).child("State").updateChildren(userState);
    }


    public void sendImage(Uri uri, final String receiver) {
        final String senderId = db.user.getUid(),
                messSenderRef = "Messages/"+ senderId + "/" + receiver,
                messReceiverRef  =  "Messages/"+ receiver +  "/" + senderId;
        DatabaseReference messageKeyRef = db.ref.child("Messages").child(senderId).child(receiver).push();
        final String messageId = messageKeyRef.getKey();

        final StorageReference filePath = db.storageRef.child("Images").child(messageId+".jpg");
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map messageToRef = new HashMap(),
                        messageInfo = getMessage(uri.toString(), senderId, " ", "image", "none");
                        messageToRef.put(messSenderRef + "/" + messageId, messageInfo);
                        messageToRef.put(messReceiverRef + "/" + messageId, messageInfo);

                        db.ref.updateChildren(messageToRef).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(!task.isSuccessful()){
                                    showMessage(task.getException().getMessage().toString());
                                }else{
                                    updateLastMessage(senderId, receiver);
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    public void updateLastMessage(String senderId, String receiver){
        long currentTime = Calendar.getInstance().getTimeInMillis();
        String contactSenderRef = "Contacts/"+ senderId + "/" + receiver,
                contactReceiverRef  =  "Contacts/"+ receiver +  "/" + senderId;
        Map toUpdate = new HashMap();
        toUpdate.put(contactSenderRef+"/lastMessage", db.d2099 - currentTime);
        toUpdate.put(contactSenderRef+"/seen", "seen");
        toUpdate.put(contactReceiverRef+"/lastMessage", db.d2099 - currentTime);
        toUpdate.put(contactReceiverRef+"/seen", "not_seen");
        db.ref.updateChildren(toUpdate);
    }

    public void sendFile(Uri uri, final String type, final String receiver) {
        Cursor returnCursor =
                app.getContentResolver().query(uri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        final String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        final String senderId = db.user.getUid(),
                messSenderRef = "Messages/"+ senderId + "/" + receiver,
                messReceiverRef  =  "Messages/"+ receiver +  "/" + senderId;
        DatabaseReference messageKeyRef = db.ref.child("Messages").child(senderId).child(receiver).push();
        final String messageId = messageKeyRef.getKey();

        final StorageReference filePath = db.storageRef.child("DocFiles").child(messageId+"."+type);
        filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map messageToRef = new HashMap(),
                        messageInfo = getMessage(uri.toString(), senderId, " ", type, fileName);
                        messageToRef.put(messSenderRef + "/" + messageId, messageInfo);
                        messageToRef.put(messReceiverRef + "/" + messageId, messageInfo);

                        db.ref.updateChildren(messageToRef).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(!task.isSuccessful()){
                                    showMessage(task.getException().getMessage().toString());
                                }else{
                                    updateLastMessage(senderId, receiver);
                                }
                            }
                        });
                    }
                });
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
