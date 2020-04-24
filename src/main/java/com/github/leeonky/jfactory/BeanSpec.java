package com.github.leeonky.jfactory;

import java.util.function.Consumer;

public class BeanSpec implements Spec {
    private final BeanProducers beanProducers;
    private final FactorySet factorySet;
    private final Argument argument;

    public BeanSpec(BeanProducers beanProducers, FactorySet factorySet, Argument argument) {
        this.beanProducers = beanProducers;
        this.factorySet = factorySet;
        this.argument = argument;
    }

    @Override
    public PropertySpec property(String property) {
        return new PropertySpec(property);
    }

    public class PropertySpec {
        private final String property;

        PropertySpec(String property) {
            this.property = property;
        }

        public void value(Object value) {
            beanProducers.add(property, new ValueProducer<>(value));
        }

        public <T> void supposeFrom(Class<? extends Definition<T>> definition) {
            beanProducers.add(property, factorySet.toBuild(definition).params(argument.getParams()).producer(property));
        }

        public <T, D extends Definition<T>> void supposeFrom(Class<D> definition, Consumer<D> mixIn) {
            beanProducers.add(property, factorySet.toBuild(definition, mixIn).params(argument.getParams()).producer(property));
        }
    }
}
