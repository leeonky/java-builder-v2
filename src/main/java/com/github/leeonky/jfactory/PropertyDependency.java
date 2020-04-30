package com.github.leeonky.jfactory;

import java.util.List;
import java.util.function.Function;
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

    @SuppressWarnings("unchecked")
    public void processDependency(Producer<?> producer) {
        ProducerRef<?> producerRef = producer.getByIndexes(property);
        if (producerRef == null) {
            producer.changeByIndexes(property, new DependencyProducer(
                    dependencies.stream().map(producer::getByIndexes).collect(Collectors.toList()),
                    rule
            ));
        } else
            producerRef.changeProducer(new DependencyProducer(
                    dependencies.stream().map(producer::getByIndexes).collect(Collectors.toList()),
                    rule
            ));
    }
}
