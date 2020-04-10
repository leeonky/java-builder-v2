package com.github.leeonky.jfactory;

public class Mould<T> {
    private final Shape<T> shape;
    private final int sequence;

    public Mould(Shape<T> shape, int sequence) {
        this.shape = shape;
        this.sequence = sequence;
    }

    public T create() {
        return shape.newInstance(sequence);
    }
}
