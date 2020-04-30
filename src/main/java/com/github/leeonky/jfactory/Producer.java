package com.github.leeonky.jfactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Producer<T> {
    private Producer<?> parent;

    static Collection<ProducerRef<?>> collectChildren(Collection<ProducerRef<?>> producers) {
        return producers.stream().flatMap(producer -> {
            List<ProducerRef<?>> subProducers = new ArrayList<>();
            subProducers.add(producer);
            subProducers.addAll(producer.getChildren());
            return subProducers.stream();
        }).collect(Collectors.toList());
    }

    public abstract T produce();

    protected Collection<ProducerRef<?>> getChildren() {
        return Collections.emptyList();
    }

    public Producer<T> changeTo(Producer<T> producer) {
        return producer;
    }

    protected Producer<T> changeFrom(Builder<T>.BeanFactoryProducer beanFactoryProducer) {
        return this;
    }

    public List<Object> getIndexes() {
        if (parent == null)
            return new ArrayList<>();
        List<Object> indexes = parent.getIndexes();
        indexes.add(parent.indexOf(this));
        return indexes;
    }

    public Producer<?> getRoot() {
        if (parent == null)
            return this;
        return parent.getRoot();
    }

    protected Object indexOf(Producer<?> element) {
        throw new IllegalStateException(String.format("`%s` has no nested producers", getClass().getName()));
    }

    public <T> void addDependency(List<Object> property, List<List<Object>> dependencies, Function<List<Object>, T> rule) {
        throw new IllegalStateException(String.format("Only %s support add dependencies", Builder.BeanFactoryProducer.class.getName()));
    }

    public ProducerRef<?> getByIndexes(List<Object> property) {
        throw new IllegalStateException(String.format("Only %s support query sub ProducerRef", Builder.BeanFactoryProducer.class.getName()));
    }

    public Producer<?> getParent() {
        return parent;
    }

    public Producer<T> setParent(Producer<?> parent) {
        this.parent = parent;
        return this;
    }

    public void changeByIndexes(List<Object> property, Producer<?> producer) {
        throw new IllegalStateException(String.format("Only %s support query sub ProducerRef", Builder.BeanFactoryProducer.class.getName()));
    }
}
