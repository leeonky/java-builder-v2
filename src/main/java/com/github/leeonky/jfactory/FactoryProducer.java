package com.github.leeonky.jfactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class FactoryProducer<T> implements Producer<T> {
    private final Factory<T> factory;
    private final String property;
    private final int sequence;
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();

    FactoryProducer(Factory<T> factory, String property, int sequence) {
        this.factory = factory;
        this.property = property;
        this.sequence = sequence;
        factory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueFactories.componentOf(propertyWriter.getPropertyType()).ifPresent(fieldComponent ->
                                propertyProducers.put(name, new FactoryProducer<>(fieldComponent, name, sequence))));
    }

    @Override
    public T produce() {
        T data = factory.newInstance(property, sequence);
        propertyProducers.forEach((k, v) -> factory.getType().setPropertyValue(data, k, v.produce()));
        return data;
    }

    public void specifyProperties(Map<String, ?> inputProperties) {
        inputProperties.forEach((k, v) -> propertyProducers.put(k, new ValueProducer<>(v)));
    }
}
