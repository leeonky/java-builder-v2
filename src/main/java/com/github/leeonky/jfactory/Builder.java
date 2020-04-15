package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.util.BeanFactory;
import com.github.leeonky.jfactory.util.FactoryProducer;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Builder<T> {
    private final FactorySet factorySet;
    private final BeanFactory<T> beanFactory;
    private final Map<String, Object> inputProperties = new LinkedHashMap<>();
    private final Map<String, Object> params = new HashMap<>();

    Builder(FactorySet factorySet, BeanFactory<T> beanFactory) {
        this.factorySet = factorySet;
        this.beanFactory = beanFactory;
    }

    public T create() {
        T object = producer().produce();
        factorySet.getDataRepository().save(object);
        return object;
    }

    public FactoryProducer<T> producer() {
        return new FactoryProducer<>(factorySet, beanFactory, null, factorySet.getSequence(beanFactory.getType()), params, inputProperties);
    }

    public T query() {
        Collection<T> collection = queryAll();
        return collection.isEmpty() ? null : collection.iterator().next();
    }

    public Collection<T> queryAll() {
        return factorySet.getDataRepository().query(beanFactory.getType(), inputProperties);
    }

    public Builder<T> property(String property, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.inputProperties.put(property, value);
        return newBuilder;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factorySet, beanFactory);
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
