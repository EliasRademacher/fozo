package com.eli.fozo.controller;

import com.eli.fozo.model.Person;
import com.google.appengine.api.datastore.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
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

    @RequestMapping(value="/people/{userName}", method=RequestMethod.GET)
    public Person people(@PathVariable String userName) {
        Query.Filter filter = new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);
        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();
        return new Person(personEntity);
    }

    @RequestMapping(value="/people", method=RequestMethod.POST)
    public ResponseEntity<?> people(@RequestBody Person person) {

        person.setJoinDate(new Date());

        Entity personEntity = new Entity("Person", person.getUserName());

        personEntity.setProperty("userName", person.getUserName());
        personEntity.setProperty("birthDate", person.getBirthDate());
        personEntity.setProperty("email", person.getEmail());
        personEntity.setProperty("ethnicity", person.getEthnicity());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("usCitizen", person.isUsCitizen());
        this.datastore.put(personEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

}
