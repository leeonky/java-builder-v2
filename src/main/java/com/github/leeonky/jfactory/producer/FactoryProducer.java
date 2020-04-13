package com.github.leeonky.jfactory.producer;

import com.github.leeonky.jfactory.Argument;
import com.github.leeonky.jfactory.Producer;
import com.github.leeonky.jfactory.factory.Factories;
import com.github.leeonky.jfactory.factory.ObjectFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class FactoryProducer<T> extends Producer<T> {
    private final ObjectFactory<T> objectFactory;
    private final int sequence;
    private final Map<String, Producer<?>> propertyProducers = new LinkedHashMap<>();

    public FactoryProducer(ObjectFactory<T> objectFactory, String property, int sequence) {
        super(property);
        this.objectFactory = objectFactory;
        this.sequence = sequence;
        objectFactory.getPropertyWriters()
                .forEach((name, propertyWriter) ->
                        Factories.of(propertyWriter.getPropertyType()).ifPresent(fieldFactory ->
                                propertyProducers.put(name, new FactoryProducer<>(fieldFactory, name, sequence))));
    }

    @Override
    public T produce() {
        T data = objectFactory.create(new Argument(getProperty(), sequence));
        propertyProducers.forEach((k, v) -> objectFactory.getType().setPropertyValue(data, k, v.produce()));
        return data;
    }

    public void specifyProperties(Map<String, ?> inputProperties) {
        inputProperties.forEach((k, v) -> propertyProducers.put(k, new ValueProducer<>(v)));
    }
}
