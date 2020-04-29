package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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
            elementProducerRefs.add(new ProducerRef<>(new ValueProducer<>(() -> null).setParent(this)));
        elementProducerRefs.get(index).changeProducer((Producer) producer);
    }

    @Override
    protected Collection<ProducerRef<?>> getChildren() {
        return collectChildren(elementProducerRefs);
    }

    @Override
    protected Object indexOf(Producer<?> sub) {
        for (int i = 0; i < elementProducerRefs.size(); i++)
            if (Objects.equals(elementProducerRefs.get(i).get(), sub))
                return i;
        throw new IllegalStateException();
    }
}
