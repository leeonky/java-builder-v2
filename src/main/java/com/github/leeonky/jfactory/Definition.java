package com.github.leeonky.jfactory;

import com.github.leeonky.util.GenericType;

public abstract class Definition<T> {
    private Spec spec;
    private Argument argument;

    public Spec spec() {
        return spec;
    }

    public Argument argument() {
        return argument;
    }

    public Definition<T> setContext(Argument argument, Spec spec) {
        this.argument = argument;
        this.spec = spec;
        return this;
    }

    public void define() {
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
