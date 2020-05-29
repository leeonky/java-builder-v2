package com.github.leeonky.jfactory;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Producer<T> {
    private Producer<?> parent;

    static Collection<Handler<?>> collectChildren(Collection<Handler<?>> producers) {
        return producers.stream().flatMap(handler -> {
            List<Handler<?>> subProducers = new ArrayList<>();
            subProducers.add(handler);
            subProducers.addAll(handler.producer.getChildren());
            return subProducers.stream();
        }).collect(Collectors.toList());
    }

    public abstract T produce();

    protected Collection<Handler<?>> getChildren() {
        return Collections.emptyList();
    }

    protected Producer<T> changeTo(Producer<T> producer) {
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

    protected Object indexOf(Producer<?> element) {
        throw new IllegalStateException(String.format("`%s` has no nested producers", getClass().getName()));
    }

    public Handler<?> getByIndex(List<Object> index) {
        throw new IllegalStateException(String.format("Only %s support query sub Handler", Builder.BeanFactoryProducer.class.getName()));
    }

    public void changeByIndex(List<Object> index, Producer<?> producer) {
        //do nothing if not collection or BeanFactoryProducer
    }

    protected void processDependencies(List<Object> root) {
    }

    static class Handler<T> {
        private Producer<T> producer;
        private T value;
        private boolean produced = false;

        Handler(Producer<T> producer, Producer<?> parent) {
            producer.parent = parent;
            this.producer = Objects.requireNonNull(producer);
        }

        public <P extends Producer<T>> P changeProducer(P producer) {
            if (producer == this.producer)
                return producer;
            Producer<?> parent = this.producer.parent;
            this.producer = this.producer.changeTo(producer);
            this.producer.parent = parent;
            return producer;
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
