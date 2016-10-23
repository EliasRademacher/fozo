package com.eli.fozo.model;

import java.util.Set;

/**
 * Created by Elias on 10/23/2016.
 */
public class Challenge {

    private long id;
    private Integer points;
    private String location;
    private String description;
    private Integer completions;
    private Set<String> tags;

    public Challenge() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
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

    public Integer getCompletions() {
        return completions;
    }

    public void setCompletions(Integer completions) {
        this.completions = completions;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
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
