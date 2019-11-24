package com.demsit.dems.ViewModel;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;

import com.demsit.dems.LoginUserActivity;
import com.demsit.dems.MainActivity;
import com.demsit.dems.Model.User;
import com.demsit.dems.Repository.TeamRepository;
import com.demsit.dems.Repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class UserVM extends AndroidViewModel {

    private UserRepository userRepository;


    public UserVM(@NonNull Application application) {
        super(application);
        this.userRepository = UserRepository.getInstance(application);
    }


    public void createUser(String email, String password, final AlertDialog pd){
        userRepository.createUser(email, password, pd);
    }

    public void updateUserInfo(String username, String userSpeciality, String image){
        userRepository.updateUserInfo(username, userSpeciality, image);
    }

    public LiveData<User> getUserInfo(){
        return userRepository.getUserInfo();
    }

    public FirebaseUser getUser(){
        return userRepository.getUser();
    }

    public void loginUser(String email,String password,final AlertDialog pd){
        userRepository.loginUser(email, password, pd);
    }

    public void signOut(){
        userRepository.signOut();
    }

    public void signInWithNumber(PhoneAuthCredential credential, final AlertDialog pd){
            userRepository.signInWithNumber(credential, pd);
    }

    public void storeImage(byte[] imageBytes) {
        userRepository.storeImage(imageBytes);
    }

    public void searchUser(String userIdValue) {
        userRepository.searchUser(userIdValue);
    }

    public void sendMessage(String message, String receiver){
        userRepository.sendMessage(message, receiver);
    }


    public void updateUserState(String state){
        userRepository.updateUserState(state);
    }


    public void sendImage(Uri data, String receiverUID) {
        userRepository.sendImage(data, receiverUID);
    }

    public void sendFile(Uri uri, String type, String receiverUID) {
        userRepository.sendFile(uri, type, receiverUID);
    }
}
