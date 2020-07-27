package com.github.leeonky.jfactory;

import java.util.HashSet;
import java.util.Set;

class LinkedProducers<T> {
    private Set<Producer<T>> producers = new HashSet<>();
    private boolean produced = false;
    private T value;

    public T produce() {
        if (produced)
            return value;
        produced = true;
        return value = makeValue();
    }

    private T makeValue() {
        return producers.stream().filter(DestinedValueProducer.class::isInstance).findFirst()
                .orElseGet(() -> producers.iterator().next()).produce();
    }

    public void add(Producer<T> producer) {
        producers.add(producer);
    }

    public LinkedProducers<T> merge(LinkedProducers<T> another) {
        producers.addAll(another.producers);
        return this;
    }
}
