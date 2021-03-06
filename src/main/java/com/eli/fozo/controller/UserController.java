package com.eli.fozo.controller;


import com.eli.fozo.repository.UserRepo;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class UserController {

    private static final Logger logger = Logger.getLogger(AccountController.class.getName());

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private UserRepo userRepo = new UserRepo();


    @RequestMapping(value="/users", method= RequestMethod.POST)
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {

        /* Make sure this user does not already exist. */
        if (userRepo.exists(user.getUserId())) {
            String message = "Attempted to create a User that already exists.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.FORBIDDEN);
        }

        userRepo.put(user);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    @RequestMapping(value="/users/{userId}", method=RequestMethod.GET)
    public ResponseEntity<?> getUser(@PathVariable String userId) {
        if (!userRepo.exists(userId)) {
            /* TODO: Handle this in the ControllerAdvice class. */
            String message = "Attempted to retrieve a User that does not exist.";
            logger.warning(message);
            return new ResponseEntity<Object>(message, HttpStatus.NOT_FOUND);
        }

        User user = userRepo.get(userId);

        /* TODO: Consider extracting creating of HTTP headers to separate method. */
        HttpHeaders requestHeaders = new HttpHeaders();
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        requestHeaders.setAccept(acceptableMediaTypes);


        return new ResponseEntity<>(user, requestHeaders, HttpStatus.OK);
    }

}
