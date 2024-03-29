package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.NullPointerInChainException;

import java.util.*;
import java.util.stream.Collectors;

import static com.github.leeonky.jfactory.LinkProducer.create;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public abstract class Producer<T> {

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

    protected Handler<?> getBy(Object key) {
        return null;
    }

    protected void changeBy(Object key, Producer<T> producer) {
    }

    public Handler<?> getChildBy(List<Object> index) {
        LinkedList<Object> leftProperty = new LinkedList<>(index);
        Handler<?> handler = getBy(leftProperty.removeFirst());
        if (handler == null)
            return null;
        return leftProperty.isEmpty() ?
                handler
                : handler.get().getChildBy(leftProperty);
    }

    public Producer<?> tryGetChildBy(LinkedList<Object> index) {
        Handler<?> handler = getBy(index.getFirst());
        if (handler == null)
            return this;
        index.removeFirst();
        return handler.get().tryGetChildBy(index);
    }

    @SuppressWarnings("unchecked")
    public void changeChildBy(List<Object> index, Producer<?> producer) {
        LinkedList<Object> leftProperty = new LinkedList<>(index);
        Object key = leftProperty.removeFirst();
        Handler handler = getBy(key);
        if (leftProperty.isEmpty()) {
            if (handler == null)
                changeBy(key, (Producer<T>) producer);
            else
                handler.changeProducer(producer);
        } else {
            if (handler != null)
                handler.get().changeChildBy(leftProperty, producer);
        }
    }

    protected void processLinks() {
    }

    protected void processDependencies() {
    }

    protected Producer<T> link(Handler<T> another) {
        return another.get().linkedByProducer(another, this);
    }

    protected Producer<T> linkedByProducer(Handler<T> another, Producer<T> producer) {
        LinkProducer<T> linkProducer = create(producer);
        another.changeProducer(linkProducer.absorb(this));
        return linkProducer;
    }

    protected Producer<T> linkedByLink(Handler<T> another, LinkProducer<T> linkProducer) {
        another.changeProducer(linkProducer.absorb(this));
        return linkProducer;
    }

    @SuppressWarnings("unchecked")
    protected Optional<Producer<?>> forLink(LinkedList<Object> leftIndex) {
        T value = produce();
        if (value == null)
            return empty();
        BeanClass beanClass = BeanClass.create(value.getClass());
        try {
            return of(new SuggestedValueProducer<>(() -> beanClass.getPropertyChainValue(value, leftIndex)));
        } catch (NullPointerInChainException e) {
            return empty();
        }
    }

    static class Handler<T> {
        private Producer<T> producer;
        private T value;
        private boolean produced = false;

        Handler(Producer<T> producer) {
            this.producer = Objects.requireNonNull(producer);
        }

        public <P extends Producer<T>> P changeProducer(P producer) {
            if (producer == this.producer)
                return producer;
            this.producer = this.producer.changeTo(producer);
            return producer;
        }

        public Handler<T> link(Handler<T> another) {
            changeProducer(producer.link(another));
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
