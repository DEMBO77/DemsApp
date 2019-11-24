package com.demsit.dems;

import android.app.Activity;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.demsit.dems.ViewModel.UserVM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class LoginNumberActivity extends AppCompatActivity {

    private Button sendCode, verifyCode;
    private EditText numberF, codeF;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private UserVM userVM;
    private AlertDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_number);
        init();
        userVM = ViewModelProviders.of(this).get(UserVM.class);
    }

    private void init() {
        sendCode = findViewById(R.id.send_code_button);
        verifyCode = findViewById(R.id.verify_code_button);
        numberF = findViewById(R.id.number_field);
        codeF = findViewById(R.id.number_code_field);
        pd = new ProgressDialog(this);

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = numberF.getText().toString();
                if(TextUtils.isEmpty(number)){
                    showMessage(R.string.phone_required);
                }else{
                    pd.setTitle("Phone number verification");
                    pd.setMessage("Your phone number is being authenticated...");
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            number,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            LoginNumberActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks

                }


            }
        });

        verifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code =  codeF.getText().toString();
                if(TextUtils.isEmpty(code)){
                    showMessage(R.string.code_number_required);
                }else{
                    pd.setTitle("Verification code");
                    pd.setMessage("The code is being verified...");
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                    signInWithNumber(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithNumber(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                pd.dismiss();
                showMessage(R.string.number_wrong);
                Log.d("uid", e.getMessage());
                sendCode.setVisibility(View.VISIBLE);
                numberF.setVisibility(View.VISIBLE);
                codeF.setVisibility(View.INVISIBLE);
                verifyCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                pd.dismiss();
                super.onCodeSent(s, forceResendingToken);
                showMessage(R.string.code_sent);
                sendCode.setVisibility(View.INVISIBLE);
                numberF.setVisibility(View.INVISIBLE);
                codeF.setVisibility(View.VISIBLE);
                verifyCode.setVisibility(View.VISIBLE);
                verificationId = s;
                resendingToken = forceResendingToken;
            }
        };
    }

    public void signInWithNumber(PhoneAuthCredential credential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            final Mydb db = Mydb.getInstance();
                            db.user = task.getResult().getUser();
                            final String userId = task.getResult().getUser().getUid();
                            showMessage(R.string.login_successfull);
                            db.ref.child("UserId").child(numberF.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()){
                                        db.ref.child("Users").child(userId).setValue("");
                                        db.ref.child("UserId").child(numberF.getText().toString()).setValue(userId);
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

    private void toMainActivity(){
        Intent sendToMain = new Intent(LoginNumberActivity.this, MainActivity.class);
        sendToMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(sendToMain);
    }



    private void showMessage(int id){
        Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}
