package com.github.leeonky.jfactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Producer<T> {
    private Producer<?> parent;

    static Collection<Handler<?>> collectChildren(Collection<Handler<?>> producers) {
        return producers.stream().flatMap(producer -> {
            List<Handler<?>> subProducers = new ArrayList<>();
            subProducers.add(producer);
            subProducers.addAll(producer.getChildren());
            return subProducers.stream();
        }).collect(Collectors.toList());
    }

    public abstract T produce();

    protected Collection<Handler<?>> getChildren() {
        return Collections.emptyList();
    }

    public Producer<T> changeTo(Producer<T> producer) {
        return producer;
    }

    protected Producer<T> changeFrom(Builder<T>.BeanFactoryProducer beanFactoryProducer) {
        return this;
    }

    public List<Object> getIndex() {
        if (parent == null)
            return new ArrayList<>();
        List<Object> indexes = parent.getIndex();
        indexes.add(parent.indexOf(this));
        return indexes;
    }

    public Producer<?> getRoot() {
        if (parent == null)
            return this;
        return parent.getRoot();
    }

    public Producer<?> getParent() {
        return parent;
    }

    public Producer<T> setParent(Producer<?> parent) {
        this.parent = parent;
        return this;
    }

    protected Object indexOf(Producer<?> element) {
        throw new IllegalStateException(String.format("`%s` has no nested producers", getClass().getName()));
    }

    public Handler<?> getByIndex(List<Object> index) {
        throw new IllegalStateException(String.format("Only %s support query sub Handler", Builder.BeanFactoryProducer.class.getName()));
    }

    public void changeByIndex(List<Object> index, Producer<?> producer) {
        throw new IllegalStateException(String.format("Only %s support query sub Handler", Builder.BeanFactoryProducer.class.getName()));
    }

    static class Handler<T> {
        private Producer<T> producer;
        private T value;
        private boolean produced = false;

        Handler(Producer<T> producer) {
            this.producer = Objects.requireNonNull(producer);
        }

        public Collection<Handler<?>> getChildren() {
            return producer.getChildren();
        }

        public void changeProducer(Producer<T> producer) {
            Producer<?> parent = this.producer.getParent();
            (this.producer = this.producer.changeTo(producer)).setParent(parent);
        }

        public Handler<T> link(Handler<T> another) {
            another.producer = new LinkProducer<>(this);
            return this;
        }

        public T produce() {
            if (!produced) {
                produced = true;
                value = producer.produce();
            }
            return value;
        }

        public Producer<T> get() {
            return producer;
        }
    }
}
