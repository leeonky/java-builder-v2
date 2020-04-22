package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Producer<T> {
    private final String property;

    Producer(String property) {
        this.property = property;
    }

    static Collection<ProducerRef<?>> collectChildren(Collection<ProducerRef<?>> producers) {
        return producers.stream().flatMap(producer -> {
            List<ProducerRef<?>> subProducers = new ArrayList<>();
            subProducers.add(producer);
            subProducers.addAll(producer.getProducer().getChildren());
            return subProducers.stream();
        }).collect(Collectors.toList());
    }

    public abstract T produce();

    public String getProperty() {
        return property;
    }

    protected Collection<ProducerRef<?>> getChildren() {
        return Collections.emptyList();
    }
}
