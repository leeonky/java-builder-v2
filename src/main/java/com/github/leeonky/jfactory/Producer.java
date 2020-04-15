package com.github.leeonky.jfactory;

public abstract class Producer<T> {
    private final String property;

    Producer(String property) {
        this.property = property;
    }

    public abstract T produce();

    public String getProperty() {
        return property;
    }
}
