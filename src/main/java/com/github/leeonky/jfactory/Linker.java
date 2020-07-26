package com.github.leeonky.jfactory;

class Linker<T> {
    LinkedProducers<T> linkedProducers = new LinkedProducers<>();

    public T produce() {
        return linkedProducers.produce();
    }
}
