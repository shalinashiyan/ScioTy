package com.ideabytes.scioty.user;

/**
 * Created by ideabytes on 3/2/16.
 */
public class User {

    private final String id;
    private final String password;

    public User(final String id,final String password) {
        this.id=id;
        this.password=password;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }
}
