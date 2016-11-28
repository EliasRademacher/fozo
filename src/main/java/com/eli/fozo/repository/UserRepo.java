package com.eli.fozo.repository;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;

/**
 * Created by Elias on 11/28/2016.
 */
public class UserRepo {

    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public Boolean exists(String userId) {
        Query.Filter filter = new Query.FilterPredicate(
                "userId",
                Query.FilterOperator.EQUAL,
                userId
        );
        Query userQuery = new Query("User").setFilter(filter);

        if (null == datastore.prepare(userQuery).asSingleEntity()) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public void put(User user) {
        /* Give the key for this entity the same as its userId property. */
        Entity userEntity = new Entity("User", user.getUserId());
        userEntity.setProperty("userId", user.getUserId());
        userEntity.setProperty("email", user.getEmail());
        userEntity.setProperty("authDomain", user.getAuthDomain());
        datastore.put(userEntity);
    }

    public User get(String userId) {
        Query.Filter filter =
                new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
        Query userQuery = new Query("User").setFilter(filter);
        Entity userEntity = datastore.prepare(userQuery).asSingleEntity();
        String email = (String) userEntity.getProperty("email");
        String authDomain = (String) userEntity.getProperty("authDomain");
        return new User(email, authDomain, userId);
    }


}
