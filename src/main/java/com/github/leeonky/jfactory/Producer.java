package com.github.leeonky.jfactory;

public abstract class Producer<T> {
    protected final String property;

    public Producer(String property) {
        this.property = property;
    }

    public abstract T produce();
}
