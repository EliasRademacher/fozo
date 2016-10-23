package com.eli.fozo.controller;

import com.eli.fozo.model.Challenge;
import com.eli.fozo.model.ChallengeType;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.collect.ArrayListMultimap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.logging.Logger;

/**
 * Created by Elias on 10/23/2016.
 *
 */

@RestController
public class ChallengeController {

    private static final Logger logger = Logger.getLogger(PersonController.class.getName());
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


    @RequestMapping(value="/challenge", method= RequestMethod.POST)
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

        challengeEntity.setProperty("points", challenge.getPoints());
        challengeEntity.setProperty("location", challenge.getLocation());
        challengeEntity.setProperty("description", challenge.getDescription());
        challengeEntity.setProperty("completions", challenge.getCompletions());
        challengeEntity.setProperty("tags", challenge.getTags());

        this.datastore.put(challengeEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

}
