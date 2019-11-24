package com.demsit.dems;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.demsit.dems.ViewModel.UserVM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.Reference;

public class RegistrationActivity extends AppCompatActivity {

    private Button registrationButton;
    private EditText userEmail, userPassword;
    private TextView accountAlreadyCreatedL;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private AlertDialog pd;
    private UserVM userVM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        init();
        accountAlreadyCreatedL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLoginUserActivity();
            }
        });
        this.registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
        userVM = ViewModelProviders.of(this).get(UserVM.class);

    }

    private void init() {
        this.registrationButton = findViewById(R.id.registration_button);
        this.userEmail = findViewById(R.id.registration_mail);
        this.userPassword = findViewById(R.id.registration_password);
        this.accountAlreadyCreatedL = findViewById(R.id.account_already_created_link);
        pd = new ProgressDialog(this);
    }

    private void toLoginUserActivity(){
        Intent sendtoLoginUser = new Intent(RegistrationActivity.this, LoginUserActivity.class);
        startActivity(sendtoLoginUser);
    }

    public void toMainActivity(){
        Intent sendToMain = new Intent(RegistrationActivity.this, MainActivity.class);
        sendToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendToMain);
        finish();
    }

    public void createAccount(){

        String email = this.userEmail.getText().toString();
        String password = this.userPassword.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            showMessage(R.string.field_registration_login_required);
        }else{
            userVM.createUser(email, password, pd);
        }
    }

    private void showMessage(int id){
        Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}
