package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class CollectionProducer<T> extends Producer<T> {
    private final BeanClass<?> collectionType;
    private List<ProducerRef<?>> elementProducerRefs = new ArrayList<>();

    public CollectionProducer(BeanClass<?> collectionType) {
        this.collectionType = collectionType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        return (T) collectionType.createCollection(elementProducerRefs.stream()
                .map(ProducerRef::produce).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public void setElementProducer(int index, Producer<?> producer) {
        for (int i = elementProducerRefs.size(); i <= index; i++)
            elementProducerRefs.add(new ProducerRef<>(new ValueProducer<>(() -> null)));
        elementProducerRefs.get(index).changeProducer((Producer) producer);
    }

    @Override
    protected Collection<ProducerRef<?>> getChildren() {
        return collectChildren(elementProducerRefs);
    }
}
