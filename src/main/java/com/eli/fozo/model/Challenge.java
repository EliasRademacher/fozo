package com.eli.fozo.model;

import com.google.appengine.api.datastore.Entity;

import java.util.List;

/**
 * Created by Elias on 10/23/2016.
 */
public class Challenge {

    private long id;
    private long points;
    private String location;
    private String description;
    private long completions;
    private List<String> tags;

    public Challenge() {
    }

    public Challenge(Entity entity ) {
        this.id = (long) entity.getProperty("id");
        this.points = (long) entity.getProperty("points");
        this.location= (String) entity.getProperty("location");
        this.description = (String) entity.getProperty("description");
        this.completions = (long) entity.getProperty("completions");
        this.tags = (List<String>) entity.getProperty("tags");
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCompletions() {
        return completions;
    }

    public void setCompletions(long completions) {
        this.completions = completions;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Challenge challenge = (Challenge) o;

        return id == challenge.id;
    }

    public void incrementPoints() {
        this.points++;
    }

    public void decrementPoints() {
        this.points--;
    }

    public void incrementCompletions() {
        this.completions++;
    }
}
