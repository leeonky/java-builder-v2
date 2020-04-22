package com.github.leeonky.jfactory;

import com.github.leeonky.util.PropertyWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionProducer<T> extends Producer<T> {
    private final PropertyWriter<?> property;
    private List<ProducerRef<?>> elementProducerRefs = new ArrayList<>();

    public CollectionProducer(PropertyWriter<?> property, int size) {
        super(property.getName());
        this.property = property;
        while (size-- > 0)
            elementProducerRefs.add(new ProducerRef<>(new ValueProducer<>(null, null)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        return (T) property.getPropertyTypeWrapper().createCollection(elementProducerRefs.stream()
                .map(producerRef -> producerRef.getProducer().produce()).collect(Collectors.toList()));
    }

    public void setElementProducer(int index, Producer<?> producer) {
        elementProducerRefs.set(index, new ProducerRef<>(producer));
    }

    @Override
    protected Collection<ProducerRef<?>> getChildren() {
        return collectChildren(elementProducerRefs);
    }
}
