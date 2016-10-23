package com.eli.fozo.controller;

import com.eli.fozo.model.Challenge;
import com.google.appengine.api.datastore.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Elias on 10/23/2016.
 *
 */

@RestController
public class ChallengeController {

    private static final Logger logger = Logger.getLogger(PersonController.class.getName());
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


    @RequestMapping(value="/challenges", method= RequestMethod.POST)
    public ResponseEntity<?> createChallenge(@Valid @RequestBody Challenge challenge) {

        /* Make sure this challenge does not already exist. */
        Query.Filter filter = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, challenge.getId());
        Query challengeQuery = new Query("Challenge").setFilter(filter);

        if (null != this.datastore.prepare(challengeQuery).asSingleEntity()) {
            String message = "Attempted to create a Challenge with an ID that already exists.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        Entity challengeEntity = new Entity("Challenge", challenge.getId());

        challengeEntity.setProperty("id", challenge.getId());
        challengeEntity.setProperty("points", challenge.getPoints());
        challengeEntity.setProperty("location", challenge.getLocation());
        challengeEntity.setProperty("description", challenge.getDescription());
        challengeEntity.setProperty("completions", challenge.getCompletions());
        challengeEntity.setProperty("tags", challenge.getTags());

        this.datastore.put(challengeEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }


    @RequestMapping(value="/challenges", method=RequestMethod.GET)
    public ResponseEntity<List<Challenge>> getChallenges() {
        Query challengeQuery = new Query("Challenge");
        List<Entity> challengeEntities =
                this.datastore.prepare(challengeQuery).asList(FetchOptions.Builder.withDefaults());

        List<Challenge> challenges = new ArrayList<>();

        for (Entity entity : challengeEntities) {
            challenges.add(new Challenge(entity));
        }

        return new ResponseEntity<>(challenges, HttpStatus.OK);
    }

    @RequestMapping(value="/challenges/{id}", method=RequestMethod.GET)
    public ResponseEntity<Challenge> getChallenge(@PathVariable("id") long id) {
        Query.Filter filter = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id);
        Query challengeQuery = new Query("Challenge").setFilter(filter);

        Entity challengeEntity = this.datastore.prepare(challengeQuery).asSingleEntity();
        Challenge challenge = new Challenge(challengeEntity);
        return new ResponseEntity<>(challenge, HttpStatus.OK);
    }

}
