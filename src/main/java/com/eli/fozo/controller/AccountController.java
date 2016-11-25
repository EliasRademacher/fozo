package com.eli.fozo.controller;

import com.google.appengine.api.datastore.*;
import model.Account;
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

    /* Add a challenge to a person. */
    @RequestMapping(value="/accounts/{userId}/challenges/{challengeId}", method=RequestMethod.PUT)
    public ResponseEntity<?> addChallengeToAccount(
            @PathVariable String userId,
            @PathVariable long challengeId
    ) {

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

        /* Check if the Challenge already exists*/
        filter = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, challengeId);
        Query challengeQuery = new Query("Challenge").setFilter(filter);
        Entity challengeEntity = this.datastore.prepare(challengeQuery).asSingleEntity();

        if (null == challengeEntity) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to update a Challenge that does not exist.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
        }

        List<Key> challengesPending = (List<Key>) accountEntity.getProperty("challengesPending");

        if (null == challengesPending) {
            /* The list of pending challenges has not been previously initialized. */
            challengesPending = new ArrayList<Key>();
        }

        challengesPending.add(challengeEntity.getKey());
        accountEntity.setProperty("challengesPending", challengesPending);

        this.datastore.put(accountEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    @RequestMapping(value="/accounts", method=RequestMethod.PUT)
    public ResponseEntity<?> updateAccounts(@Valid @RequestBody List<Account> accounts) {

        List<Entity> accountEntities = new ArrayList<>();

        for (Account account : accounts) {
            Query.Filter filter = new Query.FilterPredicate(
                    "userId",
                    Query.FilterOperator.EQUAL,
                    account.getUserId()
            );
            Query accountQuery = new Query("Account").setFilter(filter);

            /* Ancestor queries are guaranteed to maintain strong consistency. */
            accountQuery.setAncestor(this.defaultGroupKey);

            Entity accountEntity = this.datastore.prepare(accountQuery).asSingleEntity();

            /* Check if each account exists. */
            if (null == accountEntity) {
                String message = "Attempted to update an Account that does not exist.";
                logger.warning(message);
                return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
            }

            accountEntities.add(accountEntity);
        }

        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            Entity accountEntity = accountEntities.get(i);

            /* No update operation in Google Datastore, so replace old Account with updated one. */
            accountEntity.setProperty("userId", account.getUserId());
            accountEntity.setProperty("challengesCompleted", account.getChallengesCompleted());
            accountEntity.setProperty("challengesPending", account.getChallengesPending());
            this.datastore.put(accountEntity);
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
