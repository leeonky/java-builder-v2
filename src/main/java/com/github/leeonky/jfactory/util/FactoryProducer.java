package com.github.leeonky.jfactory.util;

import com.github.leeonky.jfactory.Argument;
import com.github.leeonky.jfactory.FactorySet;
import com.github.leeonky.jfactory.Producer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class FactoryProducer<T> extends Producer<T> {
    private final BeanFactory<T> beanFactory;
    private final Argument argument;
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();

    public FactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory,
                           String property, int sequence, Map<String, Object> params, Map<String, Object> properties) {
        super(property);
        this.beanFactory = beanFactory;
        argument = new Argument(property, sequence, params);
        beanFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueFactories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                propertyProducers.put(name, new FactoryProducer<>(factorySet, fieldFactory, name, sequence, params, Collections.emptyMap()))));
        beanFactory.collectSpec(argument, new BeanSpec<>(this));
        properties.forEach((k, v) ->
                new QueryExpression<>(beanFactory.getType(), k, v).queryOrCreateNested(factorySet, k, v, propertyProducers));
    }

    @Override
    public T produce() {
        T data = beanFactory.create(argument);
        propertyProducers.forEach((k, v) -> beanFactory.getType().setPropertyValue(data, k, v.produce()));
        return data;
    }

    @Deprecated
    public Map<String, Producer<?>> getPropertyProducers() {
        return propertyProducers;
    }
}
