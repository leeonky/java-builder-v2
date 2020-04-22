package com.github.leeonky.jfactory;

import java.util.*;

import static java.util.Arrays.asList;

public class Builder<T> {
    private final FactorySet factorySet;
    private final BeanFactory<T> beanFactory;
    private final Map<String, Object> properties = new LinkedHashMap<>();
    private final Map<String, Object> params = new HashMap<>();
    private final List<String> mixIns = new ArrayList<>();

    Builder(FactorySet factorySet, BeanFactory<T> beanFactory) {
        this.factorySet = factorySet;
        this.beanFactory = beanFactory;
    }

    public T create() {
        T object = producer(null).processSpec().produce();
        factorySet.getDataRepository().save(object);
        return object;
    }

    BeanFactoryProducer<T> producer(String property) {
        return new BeanFactoryProducer<>(factorySet, beanFactory, new Argument(property, factorySet.getSequence(beanFactory.getType()), params), properties, mixIns);
    }

    public T query() {
        Collection<T> collection = queryAll();
        return collection.isEmpty() ? null : collection.iterator().next();
    }

    public Collection<T> queryAll() {
        return factorySet.getDataRepository().query(beanFactory.getType(), properties);
    }

    public Builder<T> property(String property, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.properties.put(property, value);
        return newBuilder;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factorySet, beanFactory);
        newBuilder.properties.putAll(properties);
        newBuilder.params.putAll(params);
        newBuilder.mixIns.addAll(mixIns);
        return newBuilder;
    }

    public Builder<T> properties(Map<String, ?> properties) {
        Builder<T> newBuilder = copy();
        newBuilder.properties.putAll(properties);
        return newBuilder;
    }

    public Builder<T> param(String name, Object value) {
        Builder<T> newBuilder = copy();
        newBuilder.params.put(name, value);
        return newBuilder;
    }

    public Builder<T> mixIn(String... names) {
        Builder<T> newBuilder = copy();
        newBuilder.mixIns.addAll(asList(names));
        return newBuilder;
    }
}
