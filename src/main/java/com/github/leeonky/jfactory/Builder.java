package com.github.leeonky.jfactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class Builder<T> {
    private final FactorySet factorySet;
    private final Factory<T> factory;
    private final Map<String, Object> inputProperties = new LinkedHashMap<>();

    Builder(FactorySet factorySet, Factory<T> factory) {
        this.factorySet = factorySet;
        this.factory = factory;
    }

    public T create() {
        FactoryProducer<T> producer = new FactoryProducer<>(factory, null, factorySet.getSequence(factory.getType()));
        producer.specifyProperties(inputProperties);
        return producer.produce();
    }

    public Builder<T> property(String property, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.inputProperties.put(property, value);
        return newBuilder;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factorySet, factory);
        newBuilder.inputProperties.putAll(inputProperties);
        return newBuilder;
    }

    public Builder<T> properties(Map<String, ?> properties) {
        Builder<T> newBuilder = copy();
        newBuilder.inputProperties.putAll(properties);
        return newBuilder;
    }
}
