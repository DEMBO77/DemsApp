package com.demsit.dems.Model;

public class Team {

    private String uid;
    private String admin;
    private String description;
    private String project;

    public Team(){

    }

    public Team(String uid, String admin, String description, String project) {
        this.uid = uid;
        this.admin = admin;
        this.description = description;
        this.project = project;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
