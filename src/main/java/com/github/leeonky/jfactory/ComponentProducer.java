package com.github.leeonky.jfactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class ComponentProducer<T> implements Producer<T> {
    private final Component<T> component;
    private final String property;
    private final int sequence;
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();

    public ComponentProducer(Component<T> component, String property, int sequence) {
        this.component = component;
        this.property = property;
        this.sequence = sequence;
        component.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueComponents.componentOf(propertyWriter.getPropertyType()).ifPresent(fieldComponent ->
                                propertyProducers.put(name, new ComponentProducer<>(fieldComponent, name, sequence))));
    }

    @Override
    public T produce() {
        T data = component.newInstance(property, sequence);
        propertyProducers.forEach((k, v) -> component.getType().setPropertyValue(data, k, v.produce()));
        return data;
    }
}
