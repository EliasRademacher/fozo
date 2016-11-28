package com.eli.fozo.controller;


import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.logging.Logger;

@RestController
public class UserController {

    private static final Logger logger = Logger.getLogger(AccountController.class.getName());

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @RequestMapping(value="/users", method= RequestMethod.POST)
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {

        /* Make sure this account does not already exist. */
        Query.Filter filter = new Query.FilterPredicate(
                "userId",
                Query.FilterOperator.EQUAL,
                user.getUserId()
        );

        Query userQuery = new Query("User").setFilter(filter);

        if (null != this.datastore.prepare(userQuery).asSingleEntity()) {
            String message = "Attempted to create a User that already exists.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        /* Give the key for this entity the same as its userId property. */
        Entity userEntity = new Entity("User", user.getUserId());

        userEntity.setProperty("userId", user.getUserId());
        userEntity.setProperty("email", user.getEmail());
        userEntity.setProperty("authDomain", user.getAuthDomain());
        this.datastore.put(userEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

}
