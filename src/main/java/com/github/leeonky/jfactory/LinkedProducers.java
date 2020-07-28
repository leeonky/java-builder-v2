package com.github.leeonky.jfactory;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return chooseProducer(DestinedValueProducer.class).orElseGet(() ->
                chooseProducer(DependencyProducer.class).orElseGet(() ->
                        chooseProducer(SuggestedValueProducer.class).orElseGet(() ->
                                producers.iterator().next())
                )).produce();
    }

    private Optional<Producer<T>> chooseProducer(Class<?> type) {
        Stream<Producer<T>> producerStream = producers.stream().filter(type::isInstance);
        List<Producer<T>> producers = producerStream.collect(Collectors.toList());
        if (producers.size() > 1)
            throw new IllegalStateException("Ambiguous value in link");
        return producers.stream().findFirst();
    }

    public void add(Producer<T> producer) {
        producers.add(producer);
    }

    public LinkedProducers<T> merge(LinkedProducers<T> another) {
        producers.addAll(another.producers);
        return this;
    }
}
