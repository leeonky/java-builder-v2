package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;

public class Factory {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final Map<Class<?>, Component<?>> components = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Builder<T> type(Class<T> type) {
        return new Builder<>(this, (Component<T>) components.computeIfAbsent(type, Component::createComponent));
    }

    public <T> int getSequence(BeanClass<T> type) {
        synchronized (Factory.class) {
            int sequence = sequences.getOrDefault(type.getType(), 0) + 1;
            sequences.put(type.getType(), sequence);
            return sequence;
        }
    }

    public <T> T create(Class<T> type) {
        return type(type).create();
    }
}
