package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.factory.ObjectFactory;
import com.github.leeonky.jfactory.producer.FactoryProducer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Builder<T> {
    private final FactorySet factorySet;
    private final ObjectFactory<T> objectFactory;
    private final Map<String, Object> inputProperties = new LinkedHashMap<>();
    private final Map<String, Object> params = new HashMap<>();

    Builder(FactorySet factorySet, ObjectFactory<T> objectFactory) {
        this.factorySet = factorySet;
        this.objectFactory = objectFactory;
    }

    public T create() {
        T object = new FactoryProducer<>(objectFactory, null, factorySet.getSequence(objectFactory.getType()), params, inputProperties).produce();
        factorySet.getDataRepository().save(object);
        return object;
    }

    public T query() {
        return factorySet.getDataRepository().query(objectFactory.getType(), inputProperties).stream().findFirst().orElse(null);
    }

    public Builder<T> property(String property, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.inputProperties.put(property, value);
        return newBuilder;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factorySet, objectFactory);
        newBuilder.inputProperties.putAll(inputProperties);
        newBuilder.params.putAll(params);
        return newBuilder;
    }

    public Builder<T> properties(Map<String, ?> properties) {
        Builder<T> newBuilder = copy();
        newBuilder.inputProperties.putAll(properties);
        return newBuilder;
    }

    public Builder<T> param(String name, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.params.put(name, value);
        return newBuilder;
    }
}
