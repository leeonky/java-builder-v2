package com.github.leeonky.jfactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class FactoryProducer<T> extends Producer<T> {
    private final BeanFactory<T> beanFactory;
    private final Argument argument;
    private final PropertiesProducer propertiesProducer;

    public FactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory, Argument argument, Map<String, Object> properties, List<String> mixIns) {
        super(argument.getProperty());
        this.beanFactory = beanFactory;
        this.argument = argument;
        propertiesProducer = new PropertiesProducer(beanFactory, factorySet, argument, mixIns);
        properties.forEach((k, v) ->
                new QueryExpression<>(beanFactory.getType(), k, v).queryOrCreateNested(factorySet, propertiesProducer));
    }

    public FactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory, Argument argument) {
        this(factorySet, beanFactory, argument, Collections.emptyMap(), Collections.emptyList());
    }

    @Override
    public T produce() {
        T data = beanFactory.create(argument);
        propertiesProducer.produce(data);
        return data;
    }
}
