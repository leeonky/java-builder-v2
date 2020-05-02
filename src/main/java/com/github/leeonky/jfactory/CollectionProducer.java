package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class CollectionProducer<T> extends Producer<T> {
    private final BeanClass<?> collectionType;
    private List<Handler<?>> elementHandlers = new ArrayList<>();

    public CollectionProducer(BeanClass<?> collectionType) {
        this.collectionType = collectionType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        return (T) collectionType.createCollection(elementHandlers.stream()
                .map(Handler::produce).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    public void setElementProducer(int index, Producer<?> producer) {
        for (int i = elementHandlers.size(); i <= index; i++)
            elementHandlers.add(new Handler<>(new ValueProducer<>(() -> null).setParent(this)));
        elementHandlers.get(index).changeProducer((Producer) producer);
    }

    @Override
    protected Collection<Handler<?>> getChildren() {
        return collectChildren(elementHandlers);
    }

    @Override
    protected Object indexOf(Producer<?> sub) {
        for (int i = 0; i < elementHandlers.size(); i++)
            if (Objects.equals(elementHandlers.get(i).get(), sub))
                return i;
        throw new IllegalStateException();
    }
}
