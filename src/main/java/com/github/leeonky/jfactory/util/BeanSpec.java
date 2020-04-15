package com.github.leeonky.jfactory.util;

import com.github.leeonky.jfactory.Spec;

public class BeanSpec<T> implements Spec<T> {

    private final FactoryProducer<T> factoryProducer;

    public BeanSpec(FactoryProducer<T> factoryProducer) {
        this.factoryProducer = factoryProducer;
    }

    @Override
    public PropertySpec property(String property) {
        return new PropertySpec(property);
    }

    public class PropertySpec {
        private final String property;

        public PropertySpec(String property) {
            this.property = property;
        }

        public void value(Object value) {
            factoryProducer.getPropertyProducers().put(property, new ValueProducer<>(value));
        }
    }
}
