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
 * Created by Elias Rademacher on 10/22/2016.
 *
 */

@RestController
public class WebAPIController {

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private Key defaultGroupKey;

    public WebAPIController() {
        Entity defaultGroup = new Entity("personGroup", "defaultGroup");
        this.defaultGroupKey = defaultGroup.getKey();
        datastore.put(defaultGroup);
    }

    @RequestMapping(value="/people", method=RequestMethod.GET)
    public ResponseEntity<List<Person>> getPeople() {
        Query personQuery = new Query("Person");
        List<Entity> allPeopleEntities =
                this.datastore.prepare(personQuery).asList(FetchOptions.Builder.withDefaults());

        List<Person> people = new ArrayList<>();

        for (Entity entity : allPeopleEntities) {
            people.add(new Person(entity));
        }

        return new ResponseEntity<>(people, HttpStatus.OK);
    }

    @RequestMapping(value="/people/{userName}", method=RequestMethod.GET)
    public ResponseEntity<Person> getPerson(@PathVariable String userName) {
        Query.Filter filter = new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);
        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();
        Person person = new Person(personEntity);
        return new ResponseEntity<>(person, HttpStatus.OK);
    }

    @RequestMapping(value="/people", method=RequestMethod.POST)
    public ResponseEntity<?> createPerson(@RequestBody Person person) {

        /* TODO: Make sure person does not already exist. */

        person.setJoinDate(new Date());

        Entity personEntity = new Entity("Person", person.getUserName(), this.defaultGroupKey);

        personEntity.setProperty("userName", person.getUserName());
        personEntity.setProperty("birthDate", person.getBirthDate());
        personEntity.setProperty("email", person.getEmail());
        personEntity.setProperty("ethnicity", person.getEthnicity());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("usCitizen", person.isUsCitizen());
        this.datastore.put(personEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    @RequestMapping(value="/people/{userName}", method=RequestMethod.PUT)
    public ResponseEntity<?> updatePerson(@RequestBody Person person, @PathVariable String userName) {

        /* Check if this person exists. */
        Query.Filter filter =
                new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);
        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();

        if (null == personEntity) {
            logger.warning("Attempted to update a Person that does not exist.");
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
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
        this.datastore.put(personEntity);

        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }

    @RequestMapping(value="/people", method=RequestMethod.PUT)
    public ResponseEntity<?> updatePeople(@RequestBody List<Person> people) {

        List<Entity> personEntities = new ArrayList<>();

        for (Person person : people) {
            Query.Filter filter =
                    new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, person.getUserName());
            Query personQuery = new Query("Person").setFilter(filter);
            Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();

            /* Check if each person exists. */
            if (null == personEntity) {
                logger.warning("Attempted to update a Person that does not exist.");
                return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
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
            this.datastore.put(personEntity);
        }

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value="/people", method=RequestMethod.DELETE)
    public ResponseEntity<?> deletePeople() {
        Query personQuery = new Query("Person");
        personQuery.setKeysOnly();
        List<Entity> allPeopleEntities =
                this.datastore.prepare(personQuery).asList(FetchOptions.Builder.withDefaults());

        for (Entity entity : allPeopleEntities) {
            this.datastore.delete(entity.getKey());
        }

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @RequestMapping(value="/people/{userName}", method=RequestMethod.DELETE)
    public ResponseEntity<?> deletePerson(@PathVariable String userName) {
        Query.Filter filter = new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);
        personQuery.setKeysOnly();
        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();
        this.datastore.delete(personEntity.getKey());
        return new ResponseEntity<Object>(HttpStatus.OK);
    }

}
