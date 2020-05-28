package com.github.leeonky.jfactory;

public class DestinedValueProducer<T> extends Producer<T> {
    private final T value;

    DestinedValueProducer(T value) {
        this.value = value;
    }

    @Override
    public T produce() {
        return value;
    }
}
