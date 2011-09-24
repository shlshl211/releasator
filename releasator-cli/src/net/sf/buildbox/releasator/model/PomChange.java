package net.sf.buildbox.releasator.model;

public class PomChange {
    private final String location;
    private final String value;

    public PomChange(String location, String value) {
        this.location = location;
        this.value = value;
    }

    public String getLocation() {
        return location;
    }

    public String getValue() {
        return value;
    }
}
