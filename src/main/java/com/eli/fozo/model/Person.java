package com.eli.fozo.model;

import java.util.Date;

/**
 * Created by Elias on 10/6/2016.
 */
public class Person {

    private String userName;
    private String ethnicity;
    private Date joinDate;
    private String email;
    private boolean usCitizen;

    public Person() {
    }

    public Person(String userName, String ethnicity, Date joinDate, String email, boolean usCitizen) {
        this.userName = userName;
        this.ethnicity = ethnicity;
        this.joinDate = joinDate;
        this.email = email;
        this.usCitizen = usCitizen;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public Date getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Date joinDate) {
        this.joinDate = joinDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isUsCitizen() {
        return usCitizen;
    }

    public void setUsCitizen(boolean usCitizen) {
        this.usCitizen = usCitizen;
    }
}
