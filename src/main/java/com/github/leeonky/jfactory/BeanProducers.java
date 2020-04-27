package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.github.leeonky.jfactory.Producer.collectChildren;

class BeanProducers {
    private final Map<String, ProducerRef<?>> propertyProducerRefs = new LinkedHashMap<>();
    private final BeanClass type;

    public <T> BeanProducers(BeanFactory<T> beanFactory, Argument argument, List<String> mixIns,
                             BiConsumer<Argument, Spec> typeMixIn, FactorySet factorySet, Map<String, BiConsumer<Argument, BeanSpec.PropertySpec>> propertySpecs) {
        type = beanFactory.getType();
        beanFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        ValueFactories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                add(name, new ValueFactoryProducer<>(fieldFactory, argument.newProperty(name)))));
        BeanSpec beanSpec = new BeanSpec(this, factorySet, argument);
        beanFactory.collectSpec(argument, beanSpec);
        typeMixIn.accept(argument, beanSpec);
        beanFactory.collectMixInSpecs(argument, mixIns, beanSpec);
        propertySpecs.forEach((property, spec) -> spec.accept(argument, beanSpec.property(property)));
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

    public Collection<ProducerRef<?>> getProducers() {
        return collectChildren(propertyProducerRefs.values());
    }

    public BeanClass<?> getType() {
        return type;
    }

    private Producer<?> getProducer(String property) {
        ProducerRef<?> producerRef = propertyProducerRefs.get(property);
        return producerRef == null ? null : producerRef.get();
    }

    public Producer<?> getOrAdd(String property, Supplier<Producer<?>> supplier) {
        Producer<?> producer = getProducer(property);
        if (producer == null)
            add(property, producer = supplier.get());
        return producer;
    }
}
