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

    @RequestMapping(value="/home", method=RequestMethod.GET)
    public String personForm(Model model) {
        model.addAttribute("pageTitle", "Add a User");
        model.addAttribute("person", new Person());
        return "home";
    }

    @RequestMapping(value="/home", method=RequestMethod.POST)
    public String personSubmit(@ModelAttribute Person person, Model model) {
        model.addAttribute("pageTitle", "Thanks for Adding a User!");

        System.out.println("New User: " + person.getUserName() + "\n");


//		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//
//		Entity employee = new Entity("Person", "user1");
//		employee.setProperty("firstName", "Antonio");
//		employee.setProperty("lastName", "Salieri");
//		employee.setProperty("joinDate", new Date());
//
//		datastore.put(employee);


        return "home";
    }

}
