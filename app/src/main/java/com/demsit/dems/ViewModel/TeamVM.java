package com.demsit.dems.ViewModel;

import android.app.Application;
import android.icu.util.Measure;
import android.net.Uri;

import com.demsit.dems.Model.Message;
import com.demsit.dems.Model.Team;
import com.demsit.dems.Repository.TeamRepository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class TeamVM extends AndroidViewModel {

    private TeamRepository teamRepository;

    public TeamVM(@NonNull Application app){
        super(app);
        teamRepository = TeamRepository.getInstance(app);
    }

    public void sendMessage(String teamUID, String message){
        teamRepository.sendMessage(teamUID, message);
    }

    public void createTeam(String p,String d){
        teamRepository.createTeam(p, d);
    }


    public void updateTeam(String teamId, String name, String desc) {
        teamRepository.updateTeam(teamId, name, desc);
    }

    public void deleteTeam(String teamId) {
        teamRepository.deleteTeam(teamId);
    }

    public void sendFile(Uri uri, String selectedFile, String teamUID) {
        teamRepository.sendFile(uri, selectedFile, teamUID);
    }

    public void sendImage(Uri uri, String teamUID) {
        teamRepository.sendImage(uri, teamUID);
    }
}


