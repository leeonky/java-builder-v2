package com.github.leeonky.jfactory;

class ValueProducer<T> extends Producer<T> {
    private final T value;

    ValueProducer(T value) {
        this.value = value;
    }

    @Override
    public T produce() {
        return value;
    }
}
