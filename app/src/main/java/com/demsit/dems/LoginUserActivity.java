package com.demsit.dems;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import com.demsit.dems.ViewModel.UserVM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;


import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginUserActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText userEmail, userPassword;
    private TextView passwordForgottenL, createAccountL, loginNumberL;
    private AlertDialog pd;
    private UserVM userVM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);
        init();
        userVM = ViewModelProviders.of(this).get(UserVM.class);
    }

    private void init() {
        this.loginButton = findViewById(R.id.login_mail_button);
        this.userEmail = findViewById(R.id.login_mail);
        this.userPassword = findViewById(R.id.login_password);
        this.passwordForgottenL = findViewById(R.id.password_forgotten_link);
        this.createAccountL = findViewById(R.id.new_account_link);
        this.loginNumberL = findViewById(R.id.login_phone_link);
        pd = new ProgressDialog(this);
        createAccountL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toRegistrationActivity();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        loginNumberL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toLoginNumberActivity();
            }
        });
    }


    private void toMainActivity(){
        Intent sendToMain = new Intent(LoginUserActivity.this, MainActivity.class);
        sendToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendToMain);
        finish();
    }

    private void toRegistrationActivity(){
        Intent sendToRegistration = new Intent(LoginUserActivity.this, RegistrationActivity.class);
        startActivity(sendToRegistration );
    }

    public void toLoginNumberActivity(){
        Intent sendToRegistration = new Intent(LoginUserActivity.this, LoginNumberActivity.class);
        startActivity(sendToRegistration );
    }

    private  void login(){
        String email = this.userEmail.getText().toString();
        String password = this.userPassword.getText().toString();
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            showMessage(R.string.field_registration_login_required);
        }else{
            pd.setTitle(getString(R.string.alert_login_title));
            pd.setMessage(getString(R.string.alert_login_message));
            pd.setCanceledOnTouchOutside(true);
            pd.show();
            userVM.loginUser(email, password, pd);
        }
    }

    private void showMessage(int id){
        Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }


}
