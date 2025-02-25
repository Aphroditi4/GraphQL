package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final String id;
    private final String name;
    private final String email;
    private final List<Review> reviews = new ArrayList<>();

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Review> getReviews() {
        return reviews;
    }
}