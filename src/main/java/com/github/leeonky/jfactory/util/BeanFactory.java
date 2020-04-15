package com.github.leeonky.jfactory.util;

import com.github.leeonky.jfactory.Argument;
import com.github.leeonky.jfactory.Factory;
import com.github.leeonky.jfactory.Spec;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BeanFactory<T> implements Factory<T> {
    private final BeanClass<T> type;
    private Function<Argument, T> constructor = this::newInstance;
    private BiConsumer<Argument, Spec<T>> spec = (arg, spec) -> {
    };

    BeanFactory(BeanClass<T> type) {
        this.type = type;
    }

    public static <T> BeanFactory<T> create(Class<T> type) {
        return ValueFactories.of(type).orElseGet(() -> new BeanFactory<>(BeanClass.create(type)));
    }

    protected T newInstance(Argument argument) {
        return getType().newInstance();
    }

    public T create(Argument argument) {
        return constructor.apply(argument);
    }

    public BeanClass<T> getType() {
        return type;
    }

    public Map<String, PropertyWriter<T>> getPropertyWriters() {
        return type.getPropertyWriters();
    }

    @Override
    public Factory<T> construct(Function<Argument, T> constructor) {
        this.constructor = Objects.requireNonNull(constructor);
        return this;
    }

    @Override
    public Factory<T> define(BiConsumer<Argument, Spec<T>> spec) {
        this.spec = spec;
        return this;
    }

    public void collectSpec(Argument argument, BeanSpec<T> beanSpec) {
        spec.accept(argument, beanSpec);
    }
}
