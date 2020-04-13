package com.github.leeonky.jfactory.producer;

import com.github.leeonky.jfactory.Producer;

public class ValueProducer<T> extends Producer<T> {
    private final T value;

    ValueProducer(T value) {
        super(null);
        this.value = value;
    }

    @Override
    public T produce() {
        return value;
    }
}
