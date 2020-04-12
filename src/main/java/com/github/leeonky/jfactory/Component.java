package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.Map;

public class Component<T> {
    private final BeanClass<T> type;

    public Component(BeanClass<T> type) {
        this.type = type;
    }

    public static <T> Component<T> createComponent(Class<T> type) {
        return ValueComponents.componentOf(type).orElseGet(() -> new Component<>(BeanClass.create(type)));
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
