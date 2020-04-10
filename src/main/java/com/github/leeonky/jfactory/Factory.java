package com.github.leeonky.jfactory;

import com.github.leeonky.jfactory.shape.StringShape;
import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;

public class Factory {
    private final Map<Class<?>, Integer> sequences = new HashMap<>();

    public <T> Requirement<T> type(Class<T> type) {
        if (String.class.equals(type))
            return (Requirement<T>) new Requirement<>(this, new StringShape());
        throw new IllegalStateException();
    }

    <T> int getSequence(BeanClass<T> type) {
        synchronized (Factory.class) {
            int sequence = sequences.getOrDefault(type.getType(), 0) + 1;
            sequences.put(type.getType(), sequence);
            return sequence;
        }
    }
}
