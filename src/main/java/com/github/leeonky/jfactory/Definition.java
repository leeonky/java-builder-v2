package com.github.leeonky.jfactory;

import com.github.leeonky.util.GenericType;

public abstract class Definition<T> {
    public void define(Argument arg, Spec<T> spec) {
    }

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) GenericType.createGenericType(getClass().getGenericSuperclass()).getGenericTypeParameter(0)
                .orElseThrow(() -> new IllegalStateException(String.format("Invalid Definition '%s' should specify generic type or override getType() method", getClass().getName())))
                .getRawType();
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
