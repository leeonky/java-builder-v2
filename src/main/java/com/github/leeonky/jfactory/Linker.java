package com.github.leeonky.jfactory;

class Linker<T> {
    private LinkedProducers<T> linkedProducers = new LinkedProducers<>();

    public Linker<T> addProducer(Producer<T> producer) {
        linkedProducers.add(producer);
        return this;
    }

    public T produce() {
        return linkedProducers.produce();
    }

    public void merge(Linker<T> another) {
        linkedProducers = another.linkedProducers.merge(linkedProducers);
    }
}
