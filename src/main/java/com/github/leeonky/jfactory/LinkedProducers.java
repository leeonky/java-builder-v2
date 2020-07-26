package com.github.leeonky.jfactory;

import java.util.HashSet;
import java.util.Set;

class LinkedProducers<T> {
    Set<Producer<T>> producers = new HashSet<>();
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
}
