package org.example.models;


public class Beer {
    private final String id;
    private final String name;
    private final String style;
    private final Brewery brewery;

    public Beer(String id, String name, String style, Brewery brewery) {
        this.id = id;
        this.name = name;
        this.style = style;
        this.brewery = brewery;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStyle() {
        return style;
    }

    public Brewery getBrewery() {
        return brewery;
    }
}