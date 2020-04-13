package com.github.leeonky.jfactory.producer;

import com.github.leeonky.jfactory.Argument;
import com.github.leeonky.jfactory.Producer;
import com.github.leeonky.jfactory.factory.Factories;
import com.github.leeonky.jfactory.factory.ObjectFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FactoryProducer<T> extends Producer<T> {
    private final ObjectFactory<T> objectFactory;
    private final int sequence;
    private final Map<String, Object> params;
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();

    public FactoryProducer(ObjectFactory<T> objectFactory,
                           String property, int sequence, Map<String, Object> params, Map<String, Object> properties) {
        super(property);
        this.objectFactory = objectFactory;
        this.sequence = sequence;
        this.params = new HashMap<>(params);
        objectFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        Factories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                propertyProducers.put(name, new FactoryProducer<>(fieldFactory, name, sequence, params, Collections.emptyMap()))));
        properties.forEach((k, v) -> propertyProducers.put(k, new ValueProducer<>(v)));
    }

    @Override
    public T produce() {
        T data = objectFactory.create(new Argument(property, sequence, params));
        propertyProducers.forEach((k, v) -> objectFactory.getType().setPropertyValue(data, k, v.produce()));
        return data;
    }
}
