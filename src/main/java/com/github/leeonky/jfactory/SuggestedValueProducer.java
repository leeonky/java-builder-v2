package com.github.leeonky.jfactory;

import java.util.function.Supplier;

class SuggestedValueProducer<T> extends Producer<T> {
    private final Supplier<T> supplier;

    SuggestedValueProducer(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T produce() {
        return supplier.get();
    }
}
