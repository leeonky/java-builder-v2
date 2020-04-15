package com.github.leeonky.jfactory;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Factory<T> {
    Factory<T> construct(Function<Argument, T> constructor);

    Factory<T> define(BiConsumer<Argument, Spec<T>> spec);

    Factory<T> canMixIn(String name, BiConsumer<Argument, Spec<T>> spec);
}
