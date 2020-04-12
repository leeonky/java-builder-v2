package com.github.leeonky.jfactory;

public class Builder<T> {
    private final Factory factory;
    private final Component<T> component;

    public Builder(Factory factory, Component<T> component) {
        this.factory = factory;
        this.component = component;
    }

    public T create() {
        return new ComponentProducer<>(component, null, factory.getSequence(component.getType())).produce();
    }
}
