package org.example.models;

import java.util.ArrayList;
import java.util.List;

public class Brewery {
    private final String id;
    private final String name;
    private final String country;
    private final List<Beer> beers = new ArrayList<>();

    public Brewery(String id, String name, String country) {
        this.id = id;
        this.name = name;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public List<Beer> getBeers() {
        return beers;
    }
}