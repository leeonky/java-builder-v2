package com.github.leeonky.jfactory;

class BeanSpec<T> implements Spec<T> {
    private final BeanProducers beanProducers;

    public BeanSpec(BeanProducers beanProducers) {
        this.beanProducers = beanProducers;
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
            beanProducers.add(new ValueProducer<>(property, value));
        }
    }
}
