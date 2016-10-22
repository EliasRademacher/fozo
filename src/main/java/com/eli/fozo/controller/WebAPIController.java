package com.eli.fozo.controller;

import com.eli.fozo.model.Person;
import com.google.appengine.api.datastore.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Elias on 10/22/2016.
 */

@RestController
public class WebAPIController {

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @RequestMapping(value="/people", method=RequestMethod.GET)
    public List<Person> people() {
        Query personQuery = new Query("Person");
        List<Entity> allPeopleEntities =
                this.datastore.prepare(personQuery).asList(FetchOptions.Builder.withDefaults());

        List<Person> people = new ArrayList<>();

        for (Entity entity : allPeopleEntities) {
            people.add(new Person(entity));
        }

        return people;
    }

    @RequestMapping(value="/people", method=RequestMethod.POST)
    public ResponseEntity<?> people(@RequestBody Person person) {

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

}
