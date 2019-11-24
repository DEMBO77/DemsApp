package com.demsit.dems.Model;

public class User {

    private String uid;
    private String name;
    private String speciality;
    private String image;

    public User(){

    }

    public User(String uid){
        this.uid = uid;
    }

    public User(String uid, String name, String speciality, String picture) {
        this.uid = uid;
        this.name = name;
        this.speciality = speciality;
        this.image = picture;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
