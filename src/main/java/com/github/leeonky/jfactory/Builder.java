package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.util.FactoryProducer;
import com.github.leeonky.jfactory.util.ObjectFactory;

import java.util.Collection;
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
        T object = producer().produce();
        factorySet.getDataRepository().save(object);
        return object;
    }

    public FactoryProducer<T> producer() {
        return new FactoryProducer<>(factorySet, objectFactory, null, factorySet.getSequence(objectFactory.getType()), params, inputProperties);
    }

    public T query() {
        Collection<T> collection = queryAll();
        return collection.isEmpty() ? null : collection.iterator().next();
    }

    public Collection<T> queryAll() {
        return factorySet.getDataRepository().query(objectFactory.getType(), inputProperties);
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
