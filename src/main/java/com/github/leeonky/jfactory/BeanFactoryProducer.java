package com.github.leeonky.jfactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class BeanFactoryProducer<T> extends Producer<T> {
    private final FactorySet factorySet;
    private final BeanFactory<T> beanFactory;
    private final Argument argument;
    private final Builder<T>.BeanProducers beanProducers;
    private Builder<T> builder;
    private Map<List<Object>, PropertyDependency<?>> dependencies = new LinkedHashMap<>();

    public BeanFactoryProducer(FactorySet factorySet, BeanFactory<T> beanFactory, Argument argument, Builder<T> builder) {
        this.factorySet = factorySet;
        this.beanFactory = beanFactory;
        this.argument = argument;
        this.builder = builder;
        beanProducers = builder.new BeanProducers(argument, this);
    }

    @Override
    public T produce() {
        T value = beanFactory.create(argument);
        argument.setCurrent(value);
        beanProducers.produce(value);
        factorySet.getDataRepository().save(value);
        return value;
    }

    @SuppressWarnings("unchecked")
    public BeanFactoryProducer<T> processSpec() {
        dependencies.values().forEach(propertyDependency -> propertyDependency.processDependency(this));

        getChildren().stream()
                .filter(ProducerRef::isBeanFactoryProducer)
                .collect(Collectors.groupingBy(Function.identity()))
                .forEach((_ignore, refs) -> refs.stream().reduce((r1, r2) -> r1.link((ProducerRef) r2)));
        return this;
    }

    @Override
    protected Collection<ProducerRef<?>> getChildren() {
        return beanProducers.getProducers();
    }

    @Override
    public int hashCode() {
        return beanProducers.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BeanFactoryProducer)
            return Objects.equals(beanProducers, ((BeanFactoryProducer) obj).beanProducers);
        return super.equals(obj);
    }

    @Override
    public Producer<T> changeTo(Producer<T> producer) {
        return producer.changeFrom(this);
    }

    @Override
    protected Producer<T> changeFrom(BeanFactoryProducer<T> beanFactoryProducer) {
        if (beanFactory instanceof CustomizedFactory)
            return this;
        return new BeanFactoryProducer<>(factorySet, beanFactoryProducer.beanFactory, argument, builder.baseOn(beanFactoryProducer.builder));
    }

    @Override
    protected Object indexOf(Producer<?> sub) {
        return beanProducers.indexOf(sub);
    }

    @Override
    public <T> void addDependency(List<Object> property, List<List<Object>> dependencies, Function<List<Object>, T> rule) {
        this.dependencies.put(property, new PropertyDependency<>(property, dependencies, rule));
    }

    @Override
    public ProducerRef<?> getByIndexes(List<Object> property) {
        LinkedList<Object> leftProperty = new LinkedList<>(property);
        ProducerRef<?> producerRef = beanProducers.getProducerRef((String) leftProperty.removeFirst());
        if (leftProperty.isEmpty())
            return producerRef;
        else
            return producerRef.get().getByIndexes(leftProperty);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void changeByIndexes(List<Object> property, Producer<?> producer) {
        LinkedList<Object> leftProperty = new LinkedList<>(property);
        String p = (String) leftProperty.removeFirst();
        ProducerRef producerRef = beanProducers.getProducerRef(p);
        if (leftProperty.isEmpty()) {
            if (producerRef == null)
                beanProducers.add(p, producer);
            else
                producerRef.changeProducer(producer);
        } else
            producerRef.get().changeByIndexes(property, producer);
    }
}
