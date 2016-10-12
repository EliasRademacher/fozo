package com.eli.fozo.controller;

import com.eli.fozo.model.Person;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class MainController {

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @RequestMapping(value={"/", "/home"}, method=RequestMethod.GET)
    public String personForm(Model model) {
        model.addAttribute("pageTitle", "Add a Person");
        model.addAttribute("personAddedMessage", "");
        return "home";
    }

    @RequestMapping(value="/home", method=RequestMethod.POST)
    public String personSubmit(@Valid @ModelAttribute Person person, BindingResult result, Model model) {
        if(result.hasErrors()) {
            model.addAttribute("personAddedMessage", result.getAllErrors().get(0).getDefaultMessage());
            return "home";
        }

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
        model.addAttribute("personEditedMessage", "\n ");
        model.addAttribute("pageTitle", "edit");

        Filter filter = new FilterPredicate("userName", FilterOperator.EQUAL, userName);
        Query personQuery = new Query("Person").setFilter(filter);
        Entity personEntity = this.datastore.prepare(personQuery).asSingleEntity();

        model.addAttribute("personEntityToEdit", personEntity);

        return "edit";
    }

    @RequestMapping(value="/edit", method=RequestMethod.POST)
    public String editPerson(@Valid @ModelAttribute Person person, BindingResult result, Model model) {
        Entity personEntity = new Entity("Person", person.getUserName());
        model.addAttribute("personEntityToEdit", personEntity);
        model.addAttribute("pageTitle", "edit");

        if(result.hasErrors()) {
            model.addAttribute("personEditedMessage", result.getAllErrors().get(0).getDefaultMessage());
            return "edit";
        }


        personEntity.setProperty("userName", person.getUserName());
        personEntity.setProperty("birthDate", person.getBirthDate());
        personEntity.setProperty("email", person.getEmail());
        personEntity.setProperty("ethnicity", person.getEthnicity());
        personEntity.setProperty("joinDate", person.getJoinDate());
        personEntity.setProperty("usCitizen", person.isUsCitizen());

        String personEditedMessage = person.getUserName() + "'s information was successfully updated.";
        model.addAttribute("personEditedMessage", personEditedMessage);

        return "edit";
    }
}
