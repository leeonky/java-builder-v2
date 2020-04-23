package com.github.leeonky.jfactory;

class LinkProducer<T> extends Producer<T> {
    private final ProducerRef<T> producerRef;

    public LinkProducer(ProducerRef<T> producerRef) {
        this.producerRef = producerRef;
    }

    @Override
    public T produce() {
        return producerRef.produce();
    }
}
