package com.github.leeonky.jfactory;

class ProducerRef<T> {
    private Producer<T> producer;

    ProducerRef(Producer<T> producer) {
        this.producer = producer;
    }

    ProducerRef() {
    }

    public Producer<T> getProducer() {
        return producer;
    }

    public void setProducer(Producer<T> producer) {
        this.producer = producer;
    }

    public ProducerRef<T> link(ProducerRef<T> another) {
        another.producer = new LinkProducer<>(another.getProducer().getProperty(), getProducer());
        return this;
    }
}
