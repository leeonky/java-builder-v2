package com.github.leeonky.jfactory;

class BeanSpec<T> implements Spec<T> {
    private final PropertiesProducer propertiesProducer;

    public BeanSpec(PropertiesProducer propertiesProducer) {
        this.propertiesProducer = propertiesProducer;
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
            propertiesProducer.add(new ValueProducer<>(property, value));
        }
    }
}
