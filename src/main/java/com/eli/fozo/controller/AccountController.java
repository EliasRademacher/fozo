package com.eli.fozo.controller;

import com.google.appengine.api.datastore.*;
import model.Account;
import model.Response;
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

    private Boolean isCorrectToken(String userId, String token) {
        String realToken = (String) cache.get(userId);

        if (null == realToken || !realToken.equals(token)) {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    private String generateToken() {
        Random random = new Random();
        int val = random.nextInt();
        return Integer.toHexString(val);
    }

    /**
     * Generate HTTP headers.
     *
     * @return {@link HttpHeaders} with media type set to application json.
     */
    private HttpHeaders getHttpHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptableMediaTypes);
        return requestHeaders;
    }

    /* TODO: Have a "UserController.getUsers" which calls this method to return a list of User objects. */
    @RequestMapping(value="/accounts", method=RequestMethod.GET)
    public ResponseEntity<?> getAccounts(
            @RequestHeader(value="token") String headerToken,
            @RequestHeader(value="userId") String headerUserId
    ) {
        if (!isCorrectToken(headerUserId, headerToken)) {
            String message = "User must be logged in to view accounts.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, getHttpHeaders(), HttpStatus.FORBIDDEN);
        }

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

        return new ResponseEntity<>(accounts, getHttpHeaders(), HttpStatus.OK);
    }



    @RequestMapping(value="/accounts/{userId}", method=RequestMethod.GET)
    public ResponseEntity<?> getAccount(
            @PathVariable String userId,
            @RequestHeader(value="token") String headerToken
            ) {

        if (!isCorrectToken(userId, headerToken)) {
            String message = "User must be logged in to view account.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, getHttpHeaders(), HttpStatus.FORBIDDEN);
        }


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
            return new ResponseEntity<Object>(message, getHttpHeaders(), HttpStatus.NOT_FOUND);
        }

        Account account = new Account(userId);

        List<Key> challengesCompleted = (List<Key>) accountEntity.getProperty("challengesCompleted");
        account.setChallengesCompleted(challengesCompleted);

        List<Key> challengesPending = (List<Key>) accountEntity.getProperty("challengesPending");
        account.setChallengesPending(challengesPending);

        return new ResponseEntity<>(account, getHttpHeaders(), HttpStatus.OK);
    }

    /* You can't create a User without an account. */
    @RequestMapping(value="/accounts", method=RequestMethod.POST)
    public ResponseEntity<Response> createAccount(@Valid @RequestBody Account account) {

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
            String message = "An account with user ID \"" +  account.getUserId() + "\" already exists.";
            logger.warning(message);
            return new ResponseEntity<>(new Response(message), getHttpHeaders(), HttpStatus.FORBIDDEN);
        }

        /* Give the key for this entity the same as its userId property. */
        Entity accountEntity = new Entity("Account", account.getUserId(), this.defaultGroupKey);

        accountEntity.setProperty("userId", account.getUserId());
        accountEntity.setProperty("password", account.getPassword());
        accountEntity.setProperty("challengesCompleted", account.getChallengesCompleted());
        accountEntity.setProperty("challengesPending", account.getChallengesPending());
        this.datastore.put(accountEntity);

        String message = "Successfully created account with user ID " + account.getUserId();
        Response response = new Response(message);
        logger.info(message);

        return new ResponseEntity<>(response, getHttpHeaders(), HttpStatus.CREATED);
    }

    @RequestMapping(value="/accounts/{userId}", method=RequestMethod.PUT)
    public ResponseEntity<String> updateAccount(
            @Valid @RequestBody Account account,
            @PathVariable String userId,
            @RequestHeader(value="token") String headerToken
    ) {

        /* TODO: Make sure that the provided Account is valid. */
        if (null == account.getUserId()) {
            String message = "User ID must be specified.";
            logger.warning(message);
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        if (!isCorrectToken(userId, headerToken)) {
            String message = "User must be logged in to update account.";
            logger.warning(message);
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.FORBIDDEN);
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
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.NOT_FOUND);
        }

        /* No update operation in Google Datastore, so replace old Account with updated one. */
        accountEntity.setProperty("userId", account.getUserId());
        accountEntity.setProperty("challengesCompleted", account.getChallengesCompleted());
        accountEntity.setProperty("challengesPending", account.getChallengesPending());

        this.datastore.put(accountEntity);

        String message = "Successfully updated account with user ID \"" + account.getUserId() + "\"";
        logger.info(message);
        return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.CREATED);
    }

    @RequestMapping(value="/accounts/{userId}", method=RequestMethod.DELETE)
    public ResponseEntity<String> deleteAccount(
            @PathVariable String userId,
            @RequestHeader(value="token") String headerToken
            ) {

        if (!isCorrectToken(userId, headerToken)) {
            String message = "User must be logged in to delete account.";
            logger.warning(message);
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.FORBIDDEN);
        }

        Query.Filter filter = new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
        Query accountQuery = new Query("Account").setFilter(filter);

        /* Ancestor queries are guaranteed to maintain strong consistency. */
        accountQuery.setAncestor(this.defaultGroupKey);

        Entity accountEntity = this.datastore.prepare(accountQuery).asSingleEntity();
        List<Key> challengeEntitiesToDelete = (List<Key>) accountEntity.getProperty("challengesPending");
        /* TODO: Also delete associated completed challenges. */

        this.datastore.delete(accountEntity.getKey());

        /* Log user out */
        cache.put(userId, Boolean.FALSE);

        if (null != challengeEntitiesToDelete) {
            this.datastore.delete(challengeEntitiesToDelete);
        }

        String message = "Successfully deleted account with user ID \"" + userId + "\"";
        logger.info(message);
        return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.OK);
    }

    /* Add a challenge to an account. */
    @RequestMapping(value="/accounts/{userId}/challenges/{challengeId}", method=RequestMethod.PUT)
    public ResponseEntity<String> addChallengeToAccount(
            @PathVariable String userId,
            @PathVariable long challengeId,
            @RequestHeader(value="token") String headerToken
    ) {

        if (!isCorrectToken(userId, headerToken)) {
            String message = "User must be logged in to update account.";
            logger.warning(message);
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.FORBIDDEN);
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
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.NOT_FOUND);
        }

        /* Check if the Challenge already exists*/
        filter = new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, challengeId);
        Query challengeQuery = new Query("Challenge").setFilter(filter);
        Entity challengeEntity = this.datastore.prepare(challengeQuery).asSingleEntity();

        if (null == challengeEntity) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to update a Challenge that does not exist.";
            logger.warning(message);
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.NOT_FOUND);
        }

        List<Key> challengesPending = (List<Key>) accountEntity.getProperty("challengesPending");

        if (null == challengesPending) {
            /* The list of pending challenges has not been previously initialized. */
            challengesPending = new ArrayList<Key>();
        }

        challengesPending.add(challengeEntity.getKey());
        accountEntity.setProperty("challengesPending", challengesPending);

        this.datastore.put(accountEntity);

        String message = "Successfully added Challenge to account with user ID \"" + userId + "\"";
        logger.info(message);
        return new ResponseEntity<String>(getHttpHeaders(), HttpStatus.CREATED);
    }

    @RequestMapping(value="accounts/login", method=RequestMethod.POST)
    public ResponseEntity<Response> login(@Valid @RequestBody Account account) {
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
            logger.warning("User ID \'" + account.getUserId() + "\' not found");
            Response response = new Response("Incorrect user ID or password.");
            return new ResponseEntity<>(response, getHttpHeaders(), HttpStatus.FORBIDDEN);
        }

        String retrievedPassword = (String) accountEntity.getProperty("password");
        if (!account.getPassword().equals(retrievedPassword)) {
            logger.warning("Wrong password");
            Response response = new Response("Incorrect user ID or password.");
            return new ResponseEntity<>(response, getHttpHeaders(), HttpStatus.FORBIDDEN);
        }

        /* Record that the user is logged in */
        String token = generateToken();
        cache.put(account.getUserId(), token);

        HttpHeaders httpHeaders = getHttpHeaders();
        httpHeaders.add("token", token);

        String message = "Logged in to account with user ID " + account.getUserId();
        logger.info(message);
        return new ResponseEntity<>(new Response(message), httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value="/accounts/logout", method=RequestMethod.POST)
    public ResponseEntity<String> logout(@Valid @RequestBody Account account) {
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
            return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.FORBIDDEN);
        }

        /* Record that the user is logged out */
        cache.put(userId, "");

        String message = "Logged out of account with user ID \"" + account.getUserId() + "\"";
        logger.info(message);
        return new ResponseEntity<String>(message, getHttpHeaders(), HttpStatus.OK);
    }


}
