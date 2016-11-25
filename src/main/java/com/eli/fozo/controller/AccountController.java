package com.eli.fozo.controller;

import com.google.appengine.api.datastore.*;
import model.Account;
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
public class AccountController {

    private static final Logger logger = Logger.getLogger(AccountController.class.getName());

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private Key defaultGroupKey;

    public AccountController() {
        Entity defaultGroup = new Entity("personGroup", "defaultGroup");
        this.defaultGroupKey = defaultGroup.getKey();
        datastore.put(defaultGroup);
    }


    /* TODO: Have a "UserController.getUsers" which calls this method to return a list of User objects. */
    @RequestMapping(value="/accounts", method=RequestMethod.GET)
    public ResponseEntity<List<Account>> getAccounts() {
        Query accountQuery = new Query("Account");

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);
        List<Entity> allAccountEntities =
                this.datastore.prepare(accountQuery).asList(FetchOptions.Builder.withDefaults());

        List<Account> accounts = new ArrayList<>();

        String userId;

        for (Entity entity : allAccountEntities) {
            userId = (String) entity.getProperty("userId");
            Account account = new Account(userId);

            List<Key> challengesCompleted = (List<Key>) entity.getProperty("challengesCompleted");
            account.setChallengesCompleted(challengesCompleted);

            List<Key> challengesPending = (List<Key>) entity.getProperty("challengesPending");
            account.setChallengesPending(challengesPending);

            accounts.add(account);
        }

        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptableMediaTypes);

        return new ResponseEntity<>(accounts, requestHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/accounts/{userId}", method=RequestMethod.GET)
    public ResponseEntity<Account> getAccount(@PathVariable String userId) {
        Query.Filter filter =
                new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        Entity accountEntity = this.datastore.prepare(accountQuery).asSingleEntity();
        Account account = new Account(userId);

        List<Key> challengesCompleted = (List<Key>) accountEntity.getProperty("challengesCompleted");
        account.setChallengesCompleted(challengesCompleted);

        List<Key> challengesPending = (List<Key>) accountEntity.getProperty("challengesPending");
        account.setChallengesPending(challengesPending);

        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptableMediaTypes);

        return new ResponseEntity<>(account, requestHeaders, HttpStatus.OK);
    }

    /* You can't create a User without an account. */
    @RequestMapping(value="/account", method=RequestMethod.POST)
    public ResponseEntity<?> createAccount(@Valid @RequestBody Account account) {

        /* Make sure this account does not already exist. */
        Query.Filter filter = new Query.FilterPredicate(
                "userId",
                Query.FilterOperator.EQUAL,
                account.getUserId()
        );

        Query personQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        personQuery.setAncestor(this.defaultGroupKey);

        if (null != this.datastore.prepare(personQuery).asSingleEntity()) {
            String message = "Attempted to create an Account with a user ID that already exists.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        /* Give the key for this entity the same as its userId property. */
        Entity accountEntity = new Entity("Account", account.getUserId(), this.defaultGroupKey);

        accountEntity.setProperty("userId", account.getUserId());
        accountEntity.setProperty("challengesCompleted", account.getChallengesCompleted());
        accountEntity.setProperty("challengesPending", account.getChallengesPending());
        this.datastore.put(accountEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    @RequestMapping(value="/accounts/{userId}", method=RequestMethod.PUT)
    public ResponseEntity<?> updateAccount(@Valid @RequestBody Account account, @PathVariable String userId) {

        /* TODO: Make sure that the provided Account is valid. */

        /* Check if this account exists. */
        Query.Filter filter =
                new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        Entity accountEntity = this.datastore.prepare(accountQuery).asSingleEntity();

        if (null == accountEntity) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to update an Account that does not exist.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
        }


        /* No update operation in Google Datastore, so replace old Account with updated one. */
        accountEntity.setProperty("userId", account.getUserId());
        accountEntity.setProperty("challengesCompleted", account.getChallengesCompleted());
        accountEntity.setProperty("challengesPending", account.getChallengesPending());

        this.datastore.put(accountEntity);

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