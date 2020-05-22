package com.example.safe;

public class Signup_helper {
    public String FullName, Email, DOB, Gender, UID;

    public Signup_helper(){

    }


    public Signup_helper(String fullname, String lemail, String date, String gend, String Phone) {
        this.FullName = fullname;
        this.Email = lemail;
        this.DOB = date;
        this.Gender = gend;
        this.UID = Phone;
    }
}
