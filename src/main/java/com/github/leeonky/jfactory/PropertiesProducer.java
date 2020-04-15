package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class PropertiesProducer {
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();
    private final BeanClass type;

    public <T> PropertiesProducer(BeanFactory<T> beanFactory, FactorySet factorySet, Argument argument, List<String> mixIns) {
        type = beanFactory.getType();
        beanFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueFactories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                add(new FactoryProducer<>(factorySet, fieldFactory, argument.newProperty(name)))));
        BeanSpec<T> beanSpec = new BeanSpec<>(this);
        beanFactory.collectSpec(argument, beanSpec);
        beanFactory.collectMixInSpecs(argument, mixIns, beanSpec);
    }

    public void add(Producer<?> producer) {
        propertyProducers.put(producer.getProperty(), producer);
    }

    @SuppressWarnings("unchecked")
    public void produce(Object data) {
        propertyProducers.forEach((k, v) -> type.setPropertyValue(data, k, v.produce()));
    }
}
