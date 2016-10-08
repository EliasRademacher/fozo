package com.eli.fozo.controller;

import com.eli.fozo.model.Person;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

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
        model.addAttribute("person", new Person());
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
}
