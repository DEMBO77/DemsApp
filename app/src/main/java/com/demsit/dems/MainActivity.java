package com.demsit.dems;


import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.demsit.dems.Fragment.ContactFragment;
import com.demsit.dems.Fragment.SettingFragment;
import com.demsit.dems.Fragment.TeamFragment;

import com.demsit.dems.Fragment.UserRequestFragment;
import com.demsit.dems.ViewModel.TeamVM;
import com.demsit.dems.ViewModel.UserVM;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;

import android.widget.Toast;



public class MainActivity extends AppCompatActivity {



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Boolean find = true;
            switch (item.getItemId()) {
                case R.id.navigation_contact:
                    fragment = new ContactFragment();
                    break;
                case R.id.navigation_team:
                    fragment = new TeamFragment();
                    break;
                case R.id.navigation_request:
                    fragment = new UserRequestFragment();
                    break;
                default:
                    find = false;
            }
            if(find){
                ft.replace(R.id.to_replace, fragment);
                ft.commit();
                return true;
            }return false;
        }
    };

    private TeamVM teamVM;
    private UserVM userVM;
    private BottomNavigationView navigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        teamVM = ViewModelProviders.of(this).get(TeamVM.class);
        userVM = ViewModelProviders.of(this).get(UserVM.class);
        if(savedInstanceState == null && userVM.getUser()!=null){
            Fragment f = new TeamFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.to_replace, f);
            ft.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(userVM.getUser() == null){
            toLoginUserActivity();
        }else{
            userVM.updateUserState(getString(R.string.online));


        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(userVM.getUser()!=null){
            userVM.updateUserState(getString(R.string.offline));
        }
    }

    private void toLoginUserActivity() {
        Intent login = new Intent(MainActivity.this, LoginUserActivity.class);
        startActivity(login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.options, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logout:
                userVM.signOut();
                toLoginUserActivity();
                return true;
            case R.id.add_contact:
                addContactAlert();
                return true;
            case R.id.user_setting:
                Fragment fragment = new SettingFragment();
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.to_replace, fragment);
                ft.commit();
                return true;
            case R.id.create_team:
                createTeamAlert();
                return true;
            default:
                return false;
        }
    }

    private void createTeamAlert(){
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 8, 20, 8);

        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        ad.setTitle(getString(R.string.alert_create_team_title));

        final EditText teamProjectF = new EditText(MainActivity.this);
        teamProjectF.setHint(getString(R.string.alert_create_team_project_hint));
        teamProjectF.setMaxLines(1);
        final EditText teamDescriptionF = new EditText(MainActivity.this);
        teamDescriptionF.setHint(getString(R.string.alert_create_team_desc_hint));
        teamDescriptionF.setMaxLines(1);

        layout.addView(teamProjectF);
        layout.addView(teamDescriptionF);
        ad.setView(layout);

        ad.setPositiveButton(getString(R.string.alert_create_team_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String teamProject = teamProjectF.getText().toString();
                String teamDesc = teamDescriptionF.getText().toString();
                if(TextUtils.isEmpty(teamDesc)){
                    teamDesc = null;
                }
                if(TextUtils.isEmpty(teamProject)){
                    showMessage(R.string.alert_create_team_hint1);
                }else{
                    createTeam(teamProject, teamDesc);
                }
            }
        });

        ad.setNegativeButton(getString(R.string.alert_create_team_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        ad.show();
    }

    public void createTeam(String p, String d){
       teamVM.createTeam(p, d);
    }

    private void addContactAlert(){
        LinearLayout layout = new LinearLayout(MainActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 8, 20, 8);

        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        ad.setTitle(getString(R.string.alert_add_contact_title));

        final EditText userId = new EditText(MainActivity.this);
        userId.setHint(getString(R.string.alert_add_contact_hint));
        userId.setMaxLines(1);

        layout.addView(userId);
        ad.setView(layout);

        ad.setPositiveButton(getString(R.string.alert_add_contact_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userIdValue = userId.getText().toString();
                if(TextUtils.isEmpty(userIdValue)){
                    showMessage(R.string.add_contact_empty_field);
                }else{
                    userVM.searchUser(userIdValue);
                }
                dialog.cancel();
            }
        });

        ad.setNegativeButton(getString(R.string.alert_create_team_cancel_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ad.show();
    }


    private void showMessage(int id){
        Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT).show();

    }

    private void showMessage(String s){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}
