package com.github.leeonky.jfactory.util;

import com.github.leeonky.jfactory.Argument;
import com.github.leeonky.jfactory.FactorySet;
import com.github.leeonky.jfactory.Producer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FactoryProducer<T> extends Producer<T> {
    private final ObjectFactory<T> objectFactory;
    private final int sequence;
    private final Map<String, Object> params;
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();

    public FactoryProducer(FactorySet factorySet, ObjectFactory<T> objectFactory,
                           String property, int sequence, Map<String, Object> params, Map<String, Object> properties) {
        super(property);
        this.objectFactory = objectFactory;
        this.sequence = sequence;
        this.params = new HashMap<>(params);
        objectFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        Factories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                propertyProducers.put(name, new FactoryProducer<>(factorySet, fieldFactory, name, sequence, params, Collections.emptyMap()))));
        properties.forEach((k, v) ->
                new QueryExpression<>(objectFactory.getType(), k, v).queryOrCreateNested(factorySet, k, v, propertyProducers));
    }

    @Override
    public T produce() {
        T data = objectFactory.create(new Argument(property, sequence, params));
        propertyProducers.forEach((k, v) -> objectFactory.getType().setPropertyValue(data, k, v.produce()));
        return data;
    }
}
