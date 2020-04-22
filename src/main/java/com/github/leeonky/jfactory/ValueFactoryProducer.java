package com.github.leeonky.jfactory;

class ValueFactoryProducer<T> extends Producer<T> {
    private final BeanFactory<T> beanFactory;
    private final Argument argument;

    public ValueFactoryProducer(BeanFactory<T> beanFactory, Argument argument) {
        super(argument.getProperty());
        this.beanFactory = beanFactory;
        this.argument = argument;
    }

    @Override
    public T produce() {
        return beanFactory.create(argument);
    }
}
