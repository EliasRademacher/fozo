package com.eli.fozo.controller;

import com.eli.fozo.model.Person;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Elias on 10/22/2016.
 */

@RestController
public class WebAPIController {

    @RequestMapping(value="/people", method=RequestMethod.GET)
    public Person people() {
        Person person = new Person(
                "web_api_user",
                "Islander",
                new Date(),
                "user@yahoo.com",
                Boolean.FALSE
        );

        ArrayList<Person> people = new ArrayList<>();
        people.add(person);
        return person;
    }



}
