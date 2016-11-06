package com.eli.fozo.controller;

import com.google.appengine.api.datastore.*;
import model.Person;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Elias Rademacher on 10/22/2016.
 *
 */

@RestController
public class PersonController {

    private static final Logger logger = Logger.getLogger(PersonController.class.getName());

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private Key defaultGroupKey;

    public PersonController() {
        Entity defaultGroup = new Entity("personGroup", "defaultGroup");
        this.defaultGroupKey = defaultGroup.getKey();
        datastore.put(defaultGroup);
    }

    @RequestMapping(value="/quote", method=RequestMethod.GET)
    public String getQuote() {
        String quote = "Twenty years from now you will be more disappointed by the things that you didn't do than by the ones you did do. So throw off the bowlines. Sail away from the safe harbor. Catch the trade winds in your sails. Explore. Dream. Discover.";
        return quote;
    }

    @RequestMapping(value="/people", method=RequestMethod.GET)
    public ResponseEntity<List<Person>> getPeople() {
        Query personQuery = new Query("Person");

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);
        List<Entity> allPeopleEntities =
                this.datastore.prepare(personQuery).asList(FetchOptions.Builder.withDefaults());

        List<Person> people = new ArrayList<>();

        for (Entity entity : allPeopleEntities) {
            people.add(new Person(entity));
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptableMediaTypes);

        return new ResponseEntity<>(people, requestHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/people/{userName}", method=RequestMethod.GET)
    public ResponseEntity<Person> getPerson(@PathVariable String userName) {
        Query.Filter filter = new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);

        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();
        Person person = new Person(personEntity);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @RequestMapping(value="/people", method=RequestMethod.POST)
    public ResponseEntity<?> createPerson(@Valid @RequestBody Person person) {

        /* Make sure this person does not already exist. */
        Query.Filter filter = new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, person.getUserName());
        Query personQuery = new Query("Person").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);

        if (null != this.datastore.prepare(personQuery).asSingleEntity()) {
            String message = "Attempted to create a Person with a userName that already exists.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }


        person.setJoinDate(new Date());

        /* Give the key for this entity the same name as its userName property. */
        Entity personEntity = new Entity("Person", person.getUserName(), this.defaultGroupKey);

        personEntity.setProperty("userName", person.getUserName());
        personEntity.setProperty("birthDate", person.getBirthDate());
        personEntity.setProperty("email", person.getEmail());
        personEntity.setProperty("ethnicity", person.getEthnicity());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("usCitizen", person.isUsCitizen());
        personEntity.setProperty("challengesCompleted", person.getChallengesCompleted());
        personEntity.setProperty("challengesPending", person.getChallengesPending());

        this.datastore.put(personEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    @RequestMapping(value="/people/{userName}", method=RequestMethod.PUT)
    public ResponseEntity<?> updatePerson(@Valid @RequestBody Person person, @PathVariable String userName) {

        /* TODO: Make sure that the provided Person is valid. */

        /* Check if this person exists. */
        Query.Filter filter =
                new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);

        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();

        if (null == personEntity) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to update a Person that does not exist.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
        }


        /* No update operation in Google Datastore, so replace old Person with updated one. */
        /* Note that this currently allows a person's username to be updated. */
        personEntity.setProperty("userName", person.getUserName());
        personEntity.setProperty("birthDate", person.getBirthDate());
        personEntity.setProperty("email", person.getEmail());
        personEntity.setProperty("ethnicity", person.getEthnicity());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("usCitizen", person.isUsCitizen());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("challengesCompleted", person.getChallengesCompleted());
        personEntity.setProperty("challengesPending", person.getChallengesPending());
        this.datastore.put(personEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }



    @RequestMapping(value="/people/{userName}/challenges/{id}", method=RequestMethod.PUT)
    public ResponseEntity<?> updatePerson(@PathVariable String userName, @PathVariable long id) {

        /* Check if this person exists. */
        Query.Filter filter =
                new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);

        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();

        if (null == personEntity) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to update a Person that does not exist.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
        }

        /* Check if the Challenge already exists*/
        filter = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id);
        Query challengeQuery = new Query("Challenge").setFilter(filter);
        Entity challengeEntity = this.datastore.prepare(challengeQuery).asSingleEntity();

        if (null == challengeEntity) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to update a Challenge that does not exist.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
        }

        List<Key> challengesPending = (List<Key>) personEntity.getProperty("challengesPending");

        if (null == challengesPending) {
            /* The list of pending challenges has not been previously initialized. */
            challengesPending = new ArrayList<Key>();
        }

        challengesPending.add(challengeEntity.getKey());
        personEntity.setProperty("challengesPending", challengesPending);

        this.datastore.put(personEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }




    @RequestMapping(value="/people", method=RequestMethod.PUT)
    public ResponseEntity<?> updatePeople(@Valid @RequestBody List<Person> people) {

        List<Entity> personEntities = new ArrayList<>();

        for (Person person : people) {
            Query.Filter filter =
                    new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, person.getUserName());
            Query personQuery = new Query("Person").setFilter(filter);

            /* Ancestor queries are guaranteed to maintain strong consistency. */
            personQuery.setAncestor(this.defaultGroupKey);

            Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();

            /* Check if each person exists. */
            if (null == personEntity) {
                String message = "Attempted to update a Person that does not exist.";
                logger.warning(message);
                return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
            }

            personEntities.add(personEntity);
        }

        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            Entity personEntity = personEntities.get(i);

            /* No update operation in Google Datastore, so replace old Person with updated one. */
            /* Note that this currently allows a person's username to be updated. */
            personEntity.setProperty("userName", person.getUserName());
            personEntity.setProperty("birthDate", person.getBirthDate());
            personEntity.setProperty("email", person.getEmail());
            personEntity.setProperty("ethnicity", person.getEthnicity());
            personEntity.setProperty("joinDate", person.getJoinDate());
            personEntity.setProperty("usCitizen", person.isUsCitizen());
            personEntity.setProperty("joinDate", person.getJoinDate());
            personEntity.setProperty("challengesCompleted", person.getChallengesCompleted());
            personEntity.setProperty("challengesPending", person.getChallengesPending());
            this.datastore.put(personEntity);
        }

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value="/people", method=RequestMethod.DELETE)
    public ResponseEntity<?> deletePeople() {
        Query personQuery = new Query("Person");

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);

        List<Entity> allPeopleEntities =
                this.datastore.prepare(personQuery).asList(FetchOptions.Builder.withDefaults());

        List<Key> challengeEntitiesToDelete = new ArrayList<>();

        for (Entity entity : allPeopleEntities) {
            challengeEntitiesToDelete.addAll((List<Key>) entity.getProperty("challengesPending"));
            this.datastore.delete(entity.getKey());
        }

        this.datastore.delete(challengeEntitiesToDelete);

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value="/people/{userName}", method=RequestMethod.DELETE)
    public ResponseEntity<?> deletePerson(@PathVariable String userName) {
        Query.Filter filter = new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);

        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();
        List<Key> challengeEntitiesToDelete = (List<Key>) personEntity.getProperty("challengesPending");

        this.datastore.delete(personEntity.getKey());
        this.datastore.delete(challengeEntitiesToDelete);

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

}
