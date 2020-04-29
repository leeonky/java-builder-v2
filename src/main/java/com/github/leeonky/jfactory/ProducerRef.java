package com.github.leeonky.jfactory;

import java.util.Collection;
import java.util.Objects;

class ProducerRef<T> {
    private Producer<T> producer;
    private T value;
    private boolean produced = false;

    ProducerRef(Producer<T> producer) {
        this.producer = Objects.requireNonNull(producer);
    }

    public Collection<ProducerRef<?>> getChildren() {
        return producer.getChildren();
    }

    public void changeProducer(Producer<T> producer) {
        producer.setParent(this.producer.getParent());
        this.producer = this.producer.changeTo(producer);
    }

    public ProducerRef<T> link(ProducerRef<T> another) {
        another.producer = new LinkProducer<>(this);
        return this;
    }

    public boolean isBeanFactoryProducer() {
        return producer instanceof BeanFactoryProducer;
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

    @Override
    public int hashCode() {
        return producer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProducerRef<?>)
            return Objects.equals(producer, ((ProducerRef) obj).producer);
        return super.equals(obj);
    }
}
