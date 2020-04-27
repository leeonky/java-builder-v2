package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class CollectionProducer<T> extends Producer<T> {
    private final BeanClass<?> collectionType;
    private Map<Integer, ProducerRef<?>> elementProducerRefs = new LinkedHashMap<>();

    public CollectionProducer(BeanClass<?> collectionType) {
        this.collectionType = collectionType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        return (T) collectionType.createCollection(getElementProducerList().stream()
                .map(ProducerRef::produce).collect(Collectors.toList()));
    }

    public void setElementProducer(int index, Producer<?> producer) {
        elementProducerRefs.put(index, new ProducerRef<>(producer));
    }

    private List<ProducerRef<?>> getElementProducerList() {
        int size = elementProducerRefs.keySet().stream().max(Integer::compareTo).orElse(-1) + 1;
        return IntStream.range(0, size)
                .mapToObj(i -> elementProducerRefs.get(i))
                .map(o -> o == null ? new ProducerRef<>(new ValueProducer<>(() -> null)) : o)
                .collect(Collectors.toList());
    }

    @Override
    protected Collection<ProducerRef<?>> getChildren() {
        return collectChildren(getElementProducerList());
    }
}
