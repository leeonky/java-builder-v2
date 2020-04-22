package com.github.leeonky.jfactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

class BeanFactoryProducer<T> extends Producer<T> {
    private final FactorySet factorySet;
    private final BeanFactory<T> beanFactory;
    private final Argument argument;
    private final Map<String, Object> properties;
    private final List<String> mixIns;
    private final BeanProducers beanProducers;
    private T value;
    private boolean produced = false;

    public BeanFactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory, Argument argument, Map<String, Object> properties, List<String> mixIns) {
        super(argument.getProperty());
        this.factorySet = factorySet;
        this.beanFactory = beanFactory;
        this.argument = argument;
        this.properties = properties;
        this.mixIns = mixIns;
        beanProducers = new BeanProducers(beanFactory, argument, mixIns);
        QueryExpression.createQueryExpressions(beanFactory.getType(), properties)
                .forEach(exp -> exp.queryOrCreateNested(factorySet, beanProducers));
    }

    @Override
    public T produce() {
        if (!produced) {
            produced = true;
            value = beanFactory.create(argument);
            beanProducers.produce(value);
            factorySet.getDataRepository().save(value);
        }
        return value;
    }

    public BeanFactoryProducer<T> processSpec() {
        getChildren().stream()
                .filter(producerRef -> producerRef.getProducer() instanceof BeanFactoryProducer)
                .collect(Collectors.groupingBy(ProducerRef::getProducer))
                .values()
                .forEach(refs -> refs.stream().reduce((r1, r2) -> r1.link((ProducerRef) r2)));
        return this;
    }

    @Override
    protected Collection<ProducerRef<?>> getChildren() {
        return collectChildren(beanProducers.getProducers());
    }

    @Override
    public int hashCode() {
        return String.format("BeanFactory:%d;MixIn:%d;Properties:%d", beanFactory.hashCode(), mixIns.hashCode(), properties.hashCode()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanFactoryProducer) {
            BeanFactoryProducer another = (BeanFactoryProducer) obj;
            return Objects.equals(beanFactory, another.beanFactory)
                    && Objects.equals(mixIns, another.mixIns)
                    && Objects.equals(properties, another.properties);
        }
        return super.equals(obj);
    }
}
