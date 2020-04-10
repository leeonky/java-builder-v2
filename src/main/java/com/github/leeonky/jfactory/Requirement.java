package com.github.leeonky.jfactory;

public class Requirement<T> {
    private final Factory factory;
    private final Shape<T> shape;

    public Requirement(Factory factory, Shape<T> shape) {
        this.factory = factory;
        this.shape = shape;
    }

    public T create() {
        return new Mould<>(shape, factory.getSequence(shape.getType())).create();
    }
}
