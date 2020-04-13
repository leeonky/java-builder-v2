package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;

public class FactorySet {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final Map<Class<?>, Factory<?>> components = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Builder<T> type(Class<T> type) {
        return new Builder<>(this, (Factory<T>) components.computeIfAbsent(type, Factory::create));
    }

    <T> int getSequence(BeanClass<T> type) {
        synchronized (FactorySet.class) {
            int sequence = sequences.getOrDefault(type.getType(), 0) + 1;
            sequences.put(type.getType(), sequence);
            return sequence;
        }
    }

    public <T> T create(Class<T> type) {
        return type(type).create();
    }
}
