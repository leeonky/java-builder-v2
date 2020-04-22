package com.github.leeonky.jfactory;

class LinkProducer<T> extends Producer<T> {
    private final Producer<T> producer;

    public LinkProducer(String property, Producer<T> producer) {
        super(property);
        this.producer = producer;
    }

    @Override
    public T produce() {
        return producer.produce();
    }
}
