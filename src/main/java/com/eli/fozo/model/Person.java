package com.eli.fozo.model;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Created by Elias on 10/6/2016.
 *
 */

public class Person {

    @NotEmpty(message="Username must not be left blank.")
    private String userName;

    @NotEmpty(message="Ethnicity must be specified.")
    private String ethnicity;

    @NotNull(message="Birth date must not be left blank")
    @DateTimeFormat(pattern="MM/dd/yyyy")
    private Date birthDate;

    private Date joinDate;

    @NotEmpty(message="email must not be left blank.")
    private String email;

    @NotNull(message="Citizenship must be either true or false.")
    private Boolean usCitizen;

    private List<Key> challengesCompleted;

    private List<Key> challengesPending;

    public Person() {
        this.challengesPending = new ArrayList<>();
        this.challengesCompleted = new ArrayList<>();
    }

    public Person(String userName, String ethnicity, Date birthDate, String email, Boolean usCitizen) {
        this(userName, ethnicity, birthDate, null, email, usCitizen);
    }

    public Person(String userName, String ethnicity, Date birthDate, Date joinDate, String email, Boolean usCitizen) {
        this.challengesPending = new ArrayList<>();
        this.challengesCompleted = new ArrayList<>();
        this.userName = userName;
        this.ethnicity = ethnicity;
        this.birthDate = birthDate;
        this.joinDate = joinDate;
        this.email = email;
        this.usCitizen = usCitizen;
    }

    public Person(Entity entity) {
        this.userName = (String) entity.getProperty("userName");
        this.ethnicity = (String) entity.getProperty("ethnicity");
        this.birthDate = (Date) entity.getProperty("birthDate");
        this.joinDate = (Date) entity.getProperty("joinDate");
        this.email = (String) entity.getProperty("email");
        this.usCitizen = (Boolean) entity.getProperty("usCitizen");
        this.challengesPending = (List<Key>) entity.getProperty("challengesPending");
        this.challengesCompleted = (List<Key>) entity.getProperty("challengesCompleted");
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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean isUsCitizen() {
        return usCitizen;
    }

    public void setUsCitizen(boolean usCitizen) {
        this.usCitizen = usCitizen;
    }

    public List<Key> getChallengesCompleted() {
        return challengesCompleted;
    }

    public void setChallengesCompleted(List<Key> challengesCompleted) {
        this.challengesCompleted = challengesCompleted;
    }

    public List<Key> getChallengesPending() {
        return challengesPending;
    }

    public void setChallengesPending(List<Key> challengesPending) {
        this.challengesPending = challengesPending;
    }

    public void addChallenge(Key challenge) {
        this.challengesPending.add(challenge);
    }

    public void completeChallenge(Key challenge) {
        this.challengesCompleted.add(challenge);
        this.challengesPending.remove(challenge);
    }

}
