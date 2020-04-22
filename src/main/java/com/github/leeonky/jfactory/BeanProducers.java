package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class BeanProducers {
    private final Map<String, ProducerRef<?>> propertyProducerRefs = new LinkedHashMap<>();
    private final BeanClass type;

    public <T> BeanProducers(BeanFactory<T> beanFactory, Argument argument, List<String> mixIns) {
        type = beanFactory.getType();
        beanFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueFactories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                add(new ValueFactoryProducer<>(fieldFactory, argument.newProperty(name)))));
        BeanSpec<T> beanSpec = new BeanSpec<>(this);
        beanFactory.collectSpec(argument, beanSpec);
        beanFactory.collectMixInSpecs(argument, mixIns, beanSpec);
    }

    @SuppressWarnings("unchecked")
    public void add(Producer<?> producer) {
        propertyProducerRefs.computeIfAbsent(producer.getProperty(), k -> new ProducerRef<>())
                .setProducer((Producer) producer);
    }

    @SuppressWarnings("unchecked")
    public void produce(Object data) {
        propertyProducerRefs.forEach((k, v) -> type.setPropertyValue(data, k, v.getProducer().produce()));
    }

    Collection<ProducerRef<?>> getProducers() {
        return propertyProducerRefs.values();
    }
}
