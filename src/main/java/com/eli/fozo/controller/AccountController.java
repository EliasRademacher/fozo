package com.eli.fozo.controller;

import com.google.appengine.api.datastore.*;
import model.Account;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;


import javax.validation.BootstrapConfiguration;
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

    private Cache cache;

    private Key defaultGroupKey;

    public AccountController() {
        Entity defaultGroup = new Entity("AccountGroup", "defaultGroup");
        this.defaultGroupKey = defaultGroup.getKey();
        datastore.put(defaultGroup);

        try {
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
        } catch (CacheException cacheException) {
            cacheException.printStackTrace();
        }
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
    public ResponseEntity<?> getAccount(@PathVariable String userId) {
        Query.Filter filter =
                new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        Entity accountEntity = this.datastore.prepare(accountQuery).asSingleEntity();

        if (null == accountEntity) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to retrieve an Account that does not exist.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
        }

        /* TODO: Consider extracting creating of HTTP headers to separate method. */
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptableMediaTypes);

        Account account = new Account(userId);

        List<Key> challengesCompleted = (List<Key>) accountEntity.getProperty("challengesCompleted");
        account.setChallengesCompleted(challengesCompleted);

        List<Key> challengesPending = (List<Key>) accountEntity.getProperty("challengesPending");
        account.setChallengesPending(challengesPending);

        return new ResponseEntity<>(account, requestHeaders, HttpStatus.OK);
    }

    /* You can't create a User without an account. */
    @RequestMapping(value="/accounts", method=RequestMethod.POST)
    public ResponseEntity<?> createAccount(@Valid @RequestBody Account account) {

        /* Make sure this account does not already exist. */
        Query.Filter filter = new Query.FilterPredicate(
                "userId",
                Query.FilterOperator.EQUAL,
                account.getUserId()
        );

        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        if (null != this.datastore.prepare(accountQuery).asSingleEntity()) {
            String message = "Attempted to create an Account with a user ID that already exists.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        /* Give the key for this entity the same as its userId property. */
        Entity accountEntity = new Entity("Account", account.getUserId(), this.defaultGroupKey);

        accountEntity.setProperty("userId", account.getUserId());
        accountEntity.setProperty("password", account.getPassword());
        accountEntity.setProperty("challengesCompleted", account.getChallengesCompleted());
        accountEntity.setProperty("challengesPending", account.getChallengesPending());
        this.datastore.put(accountEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    @RequestMapping(value="/accounts/{userId}", method=RequestMethod.PUT)
    public ResponseEntity<?> updateAccount(@Valid @RequestBody Account account, @PathVariable String userId) {

        /* TODO: Make sure that the provided Account is valid. */
        if (null == account.getUserId()) {
            String message = "User ID must be specified.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.BAD_REQUEST);
        }

        /* Make sure this user is logged in. */
        Boolean loggedIn = (Boolean) cache.get(account.getUserId());
        if (null == loggedIn || !(Boolean) loggedIn) {
            String message = "User must be logged in to update account.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

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

    @RequestMapping(value="/accounts", method=RequestMethod.DELETE)
    public ResponseEntity<?> deleteAccounts() {
        Query accountQuery = new Query("Account");

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        List<Entity> allAccountEntities =
                this.datastore.prepare(accountQuery).asList(FetchOptions.Builder.withDefaults());

        List<Key> accountEntitiesToDelete = new ArrayList<>();

        /* Clean up Challenges associated with Accounts before you delete the Accounts. */
        for (Entity entity : allAccountEntities) {
            List<Key> challengesPendingKeys = (List<Key>) entity.getProperty("challengesPending");
            if (null != challengesPendingKeys) {
                accountEntitiesToDelete.addAll(challengesPendingKeys);
            /* TODO: Don't forget to also delete challengesCompleted when that is implemented. */
            }

            this.datastore.delete(entity.getKey());
        }

        this.datastore.delete(accountEntitiesToDelete);

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value="/accounts/{userId}", method=RequestMethod.DELETE)
    public ResponseEntity<?> deleteAccount(@PathVariable String userId) {
        Query.Filter filter = new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        Entity accountEntity = this.datastore.prepare(accountQuery).asSingleEntity();
        List<Key> challengeEntitiesToDelete = (List<Key>) accountEntity.getProperty("challengesPending");
        /* TODO: Also delete associated completed challenges. */

        this.datastore.delete(accountEntity.getKey());

        if (null != challengeEntitiesToDelete) {
            this.datastore.delete(challengeEntitiesToDelete);
        }

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    /* Add a challenge to an account. */
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

    @RequestMapping(value="accounts/login", method=RequestMethod.POST)
    public ResponseEntity<?> login(@Valid @RequestBody Account account) {
        Query.Filter filter = new Query.FilterPredicate(
                "userId",
                Query.FilterOperator.EQUAL,
                account.getUserId()
        );

        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        Entity accountEntity = this.datastore.prepare(accountQuery).asSingleEntity();
        if (null == accountEntity) {
            String message = "Incorrect user ID or password.";
            logger.warning("User ID not found");
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        String retrievedPassword = (String) accountEntity.getProperty("password");
        if (!account.getPassword().equals(retrievedPassword)) {
            String message = "Incorrect user ID or password.";
            logger.warning("Wrong password");
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        /* Record that the user is logged in */
        cache.put(account.getUserId(), Boolean.TRUE);

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value="/accounts/logout", method=RequestMethod.POST)
    public ResponseEntity<?> logout(@Valid @RequestBody Account account) {
        String userId = account.getUserId();
        Query.Filter filter = new Query.FilterPredicate(
                "userId",
                Query.FilterOperator.EQUAL,
                userId
        );

        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        if (null == datastore.prepare(accountQuery).asSingleEntity()) {
            String message = "User ID not found";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        /* Record that the user is logged in */
        cache.put(userId, Boolean.FALSE);

        return new ResponseEntity<Object>(HttpStatus.OK);
    }


}
