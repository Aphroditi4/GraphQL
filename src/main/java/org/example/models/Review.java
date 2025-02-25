package org.example.models;

public class Review {
    private final String id;
    private final String text;
    private final int rating;
    private final Beer beer;
    private final User user;

    public Review(String id, String text, int rating, Beer beer, User user) {
        this.id = id;
        this.text = text;
        this.rating = rating;
        this.beer = beer;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public int getRating() {
        return rating;
    }

    public Beer getBeer() {
        return beer;
    }

    public User getUser() {
        return user;
    }
}