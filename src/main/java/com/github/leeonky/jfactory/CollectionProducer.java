package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionProducer<T> extends Producer<T> {
    private final PropertyWriter<?> property;
    private List<Producer<?>> elementProducers = new ArrayList<>();

    public CollectionProducer(PropertyWriter<?> property, int size) {
        super(property.getName());
        this.property = property;
        while (size-- > 0)
            elementProducers.add(new ValueProducer<>(null, null));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        return (T) property.getPropertyTypeWrapper().createCollection(elementProducers.stream().map(Producer::produce).collect(Collectors.toList()));
    }

    public void setElementProducer(int index, Producer<?> producer) {
        elementProducers.set(index, producer);
    }
}
