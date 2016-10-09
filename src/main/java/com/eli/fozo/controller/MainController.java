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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MainController {

    /* TODO: "Users should be able to view the data for any record they
    previously entered in a state such that it cannot be changed.
    In other words a page to just 'view' the data." */

    /* TODO: "Users should be able to enter an edit mode for any record
    (either on the same page or loaded on a different page) allowing
    them to change all the data associated with that record" */

    @RequestMapping(value="/home", method=RequestMethod.GET)
    public String personForm(Model model) {
        model.addAttribute("pageTitle", "Add a User");
        return "home";
    }

    @RequestMapping(value="/home", method=RequestMethod.POST)
    public String personSubmit(@ModelAttribute Person person, Model model) {
        model.addAttribute("pageTitle", "Thanks for Adding a User!");

        System.out.println("Username: " + person.getUserName());
        System.out.println("Ethnicity: " + person.getEthnicity());
        System.out.println("Email: " + person.getEmail());
        System.out.println("Birth Date: " + person.getBirthDate());
        System.out.println("U.S. Citizen: " + person.isUsCitizen() + "\n");

        person.setJoinDate(new Date());

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(new Entity("Person", person.getUserName()));

        return "home";
    }

    @RequestMapping(value="/viewAllPeople", method=RequestMethod.GET)
    public String viewAllPeople(Model model) throws EntityNotFoundException {
        model.addAttribute("pageTitle", "All People");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query personQuery = new Query("Person");

//        Filter propertyFilter = new FilterPredicate("userName", FilterOperator.GREATER_THAN, "");
//        personQuery.setFilter(propertyFilter);

        List<Entity> allPeopleEntities = datastore.prepare(personQuery).asList(FetchOptions.Builder.withDefaults());
        model.addAttribute("allPeopleEntities", allPeopleEntities);

        String numberOfPeople = String.valueOf(allPeopleEntities.size());

        System.out.print("\nNumber of people retrieved: " + numberOfPeople);
        for (Entity entity : allPeopleEntities) {
            System.out.print("Retrieved user: " + entity.getProperty("userName"));
        }

        model.addAttribute("numberOfPeople", numberOfPeople);

//        Key personKey = KeyFactory.stringToKey("preston");
//        Entity person = datastore.get(personKey);
//        System.out.print("Retrieved user: " + person.getProperty("name"));



        return "viewAllPeople";
    }
}
