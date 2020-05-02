package com.github.leeonky.jfactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DependencyProducer<T> extends Producer<T> {
    private final List<Handler<?>> dependencies;
    private final Function<List<Object>, T> rule;

    public DependencyProducer(List<Handler<?>> dependencies, Function<List<Object>, T> rule) {
        this.dependencies = dependencies;
        this.rule = rule;
    }

    @Override
    public T produce() {
        return rule.apply(dependencies.stream().map(Handler::produce).collect(Collectors.toList()));
    }
}
