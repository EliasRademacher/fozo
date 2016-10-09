package com.eli.fozo.controller;

import com.eli.fozo.model.Person;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class MainController {

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


    /* TODO: "Users should be able to view the data for any record they
    previously entered in a state such that it cannot be changed.
    In other words a page to just 'view' the data." */

    /* TODO: "Users should be able to enter an edit mode for any record
    (either on the same page or loaded on a different page) allowing
    them to change all the data associated with that record" */

    @RequestMapping(value="/home", method=RequestMethod.GET)
    public String personForm(Model model) {
        model.addAttribute("pageTitle", "Add a User");
        model.addAttribute("personAddedMessage", "");
        return "home";
    }

    @RequestMapping(value={"/", "/home"}, method=RequestMethod.POST)
    public String personSubmit(@ModelAttribute Person person, Model model) {
        model.addAttribute("pageTitle", "Thanks for Adding a User!");
        person.setJoinDate(new Date());
        Entity personEntity = new Entity("Person", person.getUserName());

        personEntity.setProperty("userName", person.getUserName());
        personEntity.setProperty("birthDate", person.getBirthDate());
        personEntity.setProperty("email", person.getEmail());
        personEntity.setProperty("ethnicity", person.getEthnicity());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("usCitizen", person.isUsCitizen());
        this.datastore.put(personEntity);

        model.addAttribute("personAddedMessage", "Person Successfully Added");

        return "home";
    }

    @RequestMapping(value="/viewAllPeople", method=RequestMethod.GET)
    public String viewAllPeople(Model model) throws EntityNotFoundException {
        model.addAttribute("pageTitle", "All People");

        Query personQuery = new Query("Person");
        List<Entity> allPeopleEntities = this.datastore.prepare(personQuery).asList(FetchOptions.Builder.withDefaults());
        model.addAttribute("allPeopleEntities", allPeopleEntities);

        String numberOfPeople = String.valueOf(allPeopleEntities.size());
        model.addAttribute("numberOfPeople", numberOfPeople);

        logger.info("\nNumber of people retrieved: " + numberOfPeople);
        for (Entity entity : allPeopleEntities) {
            logger.info("Retrieved user: " + entity.toString());
        }

        return "viewAllPeople";
    }

    @RequestMapping(value="/edit", method=RequestMethod.GET)
    public String editPerson(@RequestParam(value="userName") String userName, Model model) {
        logger.info("About to edit " + userName);
        model.addAttribute("pageTitle", "edit");
        model.addAttribute("personEditedMessage", "");

        Filter filter = new FilterPredicate("userName", FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);
        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();

        model.addAttribute("personEntityToEdit", personEntity);

        return "edit";
    }

    @RequestMapping(value="/edit", method=RequestMethod.POST)
    public String editPerson(@ModelAttribute Person person, Model model) {

        model.addAttribute("pageTitle", "Person Successfully Updated");

        Entity personEntity = new Entity("Person", person.getUserName());

        personEntity.setProperty("userName", person.getUserName());
        personEntity.setProperty("birthDate", person.getBirthDate());
        personEntity.setProperty("email", person.getEmail());
        personEntity.setProperty("ethnicity", person.getEthnicity());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("usCitizen", person.isUsCitizen());
        this.datastore.put(personEntity);

        model.addAttribute("personEditedMessage", person.getUserName() + "'s information was successfully updated.");
        model.addAttribute("personEntityToEdit", personEntity);

        return "edit";
    }
}
