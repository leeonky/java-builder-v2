package com.github.leeonky.jfactory;

public class Argument {
    private final String property;
    private final int sequence;

    public Argument(String property, int sequence) {
        this.property = property;
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public String getProperty() {
        return property;
    }
}
