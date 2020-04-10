package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

public class Shape<T> {
    private final BeanClass<T> type;

    public Shape(BeanClass<T> type) {
        this.type = type;
    }

    public T newInstance(int sequence) {
        return type.newInstance();
    }

    public BeanClass<T> getType() {
        return type;
    }
}
