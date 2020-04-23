package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.github.leeonky.jfactory.Producer.collectChildren;

class BeanProducers {
    private final Map<String, ProducerRef<?>> propertyProducerRefs = new LinkedHashMap<>();
    private final BeanClass type;

    public <T> BeanProducers(BeanFactory<T> beanFactory, Argument argument, List<String> mixIns,
                             BiConsumer<Argument, Spec<T>> typeMixIn) {
        type = beanFactory.getType();
        beanFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueFactories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                add(name, new ValueFactoryProducer<>(fieldFactory, argument.newProperty(name)))));
        BeanSpec<T> beanSpec = new BeanSpec<>(this);
        beanFactory.collectSpec(argument, beanSpec);
        typeMixIn.accept(argument, beanSpec);
        beanFactory.collectMixInSpecs(argument, mixIns, beanSpec);
    }

    @SuppressWarnings("unchecked")
    public void add(String property, Producer<?> producer) {
        propertyProducerRefs.computeIfAbsent(property, k -> new ProducerRef<>(null))
                .changeProducer((Producer) producer);
    }

    @SuppressWarnings("unchecked")
    public void produce(Object data) {
        propertyProducerRefs.forEach((k, v) -> type.setPropertyValue(data, k, v.produce()));
    }

    Collection<ProducerRef<?>> getProducers() {
        return collectChildren(propertyProducerRefs.values());
    }
}
