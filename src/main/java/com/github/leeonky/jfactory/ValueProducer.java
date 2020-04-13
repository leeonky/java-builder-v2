package com.github.leeonky.jfactory;

public class ValueProducer<T> implements Producer<T> {
    private final T value;

    public ValueProducer(T value) {
        this.value = value;
    }

    @Override
    public T produce() {
        return value;
    }
}
