package com.github.leeonky.jfactory;

class ValueProducer<T> extends Producer<T> {
    private final T value;

    ValueProducer(String property, T value) {
        super(property);
        this.value = value;
    }

    @Override
    public T produce() {
        return value;
    }
}
