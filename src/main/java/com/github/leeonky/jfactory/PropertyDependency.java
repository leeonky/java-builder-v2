package com.github.leeonky.jfactory;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class PropertyDependency<T> {
    private final List<Object> property;
    private final List<List<Object>> dependencies;
    private final Function<List<Object>, T> rule;

    public PropertyDependency(List<Object> property, List<List<Object>> dependencies, Function<List<Object>, T> rule) {
        this.property = property;
        this.dependencies = dependencies;
        this.rule = rule;
    }

    // TODO producer maybe changed by another dependency
    @SuppressWarnings("unchecked")
    public void processDependency(Producer<?> producer) {
        producer.changeByIndex(property, new DependencyProducer(
                dependencies.stream().map(index -> (Supplier<?>) () -> producer.getByIndex(index).produce()).collect(Collectors.toList()),
                rule));
    }
}
