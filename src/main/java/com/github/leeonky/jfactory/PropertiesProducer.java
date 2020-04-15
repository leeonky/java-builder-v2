package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

class PropertiesProducer {
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();
    private final BeanClass type;

    public PropertiesProducer(BeanFactory<?> beanFactory, FactorySet factorySet, Argument argument) {
        type = beanFactory.getType();
        beanFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueFactories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                add(new FactoryProducer<>(factorySet, fieldFactory, argument.newProperty(name), Collections.emptyMap()))));
        beanFactory.collectSpec(argument, new BeanSpec<>(this));
    }

    public void add(Producer<?> producer) {
        propertyProducers.put(producer.getProperty(), producer);
    }

    @SuppressWarnings("unchecked")
    public void produce(Object data) {
        propertyProducers.forEach((k, v) -> type.setPropertyValue(data, k, v.produce()));
    }
}
