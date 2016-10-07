package com.eli.fozo.controller;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class MainController {

	@RequestMapping(value="/", method = RequestMethod.GET)
	public String loadExample(Model model) {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Entity employee = new Entity("User", "user1");
		employee.setProperty("firstName", "Antonio");
		employee.setProperty("lastName", "Salieri");
		employee.setProperty("joinDate", new Date());

		datastore.put(employee);




		// Send the variable "pageTitle" to the view.
		// This can be accessed by ${pageTitle} in the FreeMarker file "hello-world.ftl"
		model.addAttribute("pageTitle", "Add a User");

		// When the user navigates to http://<deploy-url>/<context>/, tell the server to use
		// `/WEB-INF/ftl/views/hello-world.ftl` to render the view
		return "hello-world";
	}
}
