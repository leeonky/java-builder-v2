package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class CollectionProducer<T> extends Producer<T> {
    private final BeanClass<?> collectionType;
    private List<ProducerRef<?>> elementProducerRefs = new ArrayList<>();

    public CollectionProducer(BeanClass<?> collectionType, int size) {
        this.collectionType = collectionType;
        while (size-- > 0)
            elementProducerRefs.add(new ProducerRef<>(new ValueProducer<>(null)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        return (T) collectionType.createCollection(elementProducerRefs.stream()
                .map(ProducerRef::produce).collect(Collectors.toList()));
    }

    public void setElementProducer(int index, Producer<?> producer) {
        elementProducerRefs.set(index, new ProducerRef<>(producer));
    }

    @Override
    protected Collection<ProducerRef<?>> getChildren() {
        return collectChildren(elementProducerRefs);
    }
}
