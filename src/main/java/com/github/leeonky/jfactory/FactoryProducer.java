package com.github.leeonky.jfactory;

import java.util.Map;

class FactoryProducer<T> extends Producer<T> {
    private final BeanFactory<T> beanFactory;
    private final Argument argument;
    private final PropertiesProducer propertiesProducer;

    public FactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory, Argument argument, Map<String, Object> properties) {
        super(argument.getProperty());
        this.beanFactory = beanFactory;
        this.argument = argument;
        propertiesProducer = new PropertiesProducer(beanFactory, factorySet, argument);
        properties.forEach((k, v) ->
                new QueryExpression<>(beanFactory.getType(), k, v).queryOrCreateNested(factorySet, k, v, propertiesProducer));
    }

    @Override
    public T produce() {
        T data = beanFactory.create(argument);
        propertiesProducer.produce(data);
        return data;
    }
}
