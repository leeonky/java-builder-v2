package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.Map;

public class Factory<T> {
    private final BeanClass<T> type;

    Factory(BeanClass<T> type) {
        this.type = type;
    }

    static <T> Factory<T> create(Class<T> type) {
        return ValueFactories.componentOf(type).orElseGet(() -> new Factory<>(BeanClass.create(type)));
    }

    public T newInstance(String property, int sequence) {
        return getType().newInstance();
    }

    public BeanClass<T> getType() {
        return type;
    }

    public Map<String, PropertyWriter<T>> getPropertyWriters() {
        return type.getPropertyWriters();
    }
}
