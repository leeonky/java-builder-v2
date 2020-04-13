package com.github.leeonky.jfactory.util;

import com.github.leeonky.jfactory.Argument;
import com.github.leeonky.jfactory.Factory;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ObjectFactory<T> implements Factory<T> {
    private final BeanClass<T> type;
    private Function<Argument, T> constructor = this::newInstance;

    ObjectFactory(BeanClass<T> type) {
        this.type = type;
    }

    public static <T> ObjectFactory<T> create(Class<T> type) {
        return Factories.of(type).orElseGet(() -> new ObjectFactory<>(BeanClass.create(type)));
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
}
