package com.github.leeonky.jfactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class FactoryProducer<T> extends Producer<T> {
    private final FactorySet factorySet;
    private final BeanFactory<T> beanFactory;
    private final Argument argument;
    private final BeanProducers beanProducers;

    public FactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory, Argument argument, Map<String, Object> properties, List<String> mixIns) {
        super(argument.getProperty());
        this.factorySet = factorySet;
        this.beanFactory = beanFactory;
        this.argument = argument;
        beanProducers = new BeanProducers(beanFactory, factorySet, argument, mixIns);
        QueryExpression.createQueryExpressions(beanFactory.getType(), properties)
                .forEach(exp -> exp.queryOrCreateNested(factorySet, beanProducers));
    }

    public FactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory, Argument argument) {
        this(factorySet, beanFactory, argument, Collections.emptyMap(), Collections.emptyList());
    }

    @Override
    public T produce() {
        T data = beanFactory.create(argument);
        beanProducers.produce(data);
        factorySet.getDataRepository().save(data);
        return data;
    }
}
