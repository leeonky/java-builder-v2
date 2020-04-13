package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.factory.ObjectFactory;
import com.github.leeonky.jfactory.producer.FactoryProducer;

import java.util.LinkedHashMap;
import java.util.Map;

public class Builder<T> {
    private final FactorySet factorySet;
    private final ObjectFactory<T> objectFactory;
    private final Map<String, Object> inputProperties = new LinkedHashMap<>();

    Builder(FactorySet factorySet, ObjectFactory<T> objectFactory) {
        this.factorySet = factorySet;
        this.objectFactory = objectFactory;
    }

    public T create() {
        FactoryProducer<T> producer = new FactoryProducer<>(objectFactory, null, factorySet.getSequence(objectFactory.getType()));
        producer.specifyProperties(inputProperties);
        return producer.produce();
    }

    public Builder<T> property(String property, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.inputProperties.put(property, value);
        return newBuilder;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factorySet, objectFactory);
        newBuilder.inputProperties.putAll(inputProperties);
        return newBuilder;
    }

    public Builder<T> properties(Map<String, ?> properties) {
        Builder<T> newBuilder = copy();
        newBuilder.inputProperties.putAll(properties);
        return newBuilder;
    }
}
