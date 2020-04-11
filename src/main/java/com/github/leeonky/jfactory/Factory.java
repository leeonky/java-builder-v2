package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;

public class Factory {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();

    public <T> Requirement<T> type(Class<T> type) {
        return new Requirement<>(this, ValueShapes.shapeOf(type).orElseThrow(IllegalStateException::new));
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
