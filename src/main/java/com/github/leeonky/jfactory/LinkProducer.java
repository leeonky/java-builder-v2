package com.github.leeonky.jfactory;

class LinkProducer<T> extends Producer<T> {
    final Linker<T> linker = new Linker<>();

    @Override
    public T produce() {
        return linker.produce();
    }
}
