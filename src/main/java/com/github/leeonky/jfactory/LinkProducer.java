package com.github.leeonky.jfactory;

class LinkProducer<T> extends Producer<T> {
    private final Handler<T> handler;

    public LinkProducer(Handler<T> handler) {
        this.handler = handler;
    }

    @Override
    public T produce() {
        return handler.produce();
    }
}
