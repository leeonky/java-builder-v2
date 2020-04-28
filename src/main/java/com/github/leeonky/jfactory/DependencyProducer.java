package com.github.leeonky.jfactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DependencyProducer<T> extends Producer<T> {
    private final List<ProducerRef<?>> dependencies;
    private final Function<List<Object>, T> rule;

    public DependencyProducer(List<ProducerRef<?>> dependencies, Function<List<Object>, T> rule) {
        this.dependencies = dependencies;
        this.rule = rule;
    }

    @Override
    public T produce() {
        return rule.apply(dependencies.stream().map(ProducerRef::produce).collect(Collectors.toList()));
    }
}
