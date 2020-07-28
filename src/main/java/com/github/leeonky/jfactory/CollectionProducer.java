package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class CollectionProducer<T> extends Producer<T> {
    private final BeanClass<?> collectionType;
    private List<Handler<?>> elementHandlers = new ArrayList<>();

    public CollectionProducer(BeanClass<?> collectionType) {
        this.collectionType = collectionType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T produce() {
        List<Object> elements = new ArrayList<>();
        // Should not use java stream here, elementHandlers.size may be changed in produce
        for (int i = 0; i < elementHandlers.size(); i++)
            elements.add(elementHandlers.get(i).produce());
        return (T) collectionType.createCollection(elements);
    }

    @SuppressWarnings("unchecked")
    public void setElementProducer(int index, Producer<?> producer) {
        for (int i = elementHandlers.size(); i <= index; i++)
            elementHandlers.add(new Handler<>(new SuggestedValueProducer<>(() -> null), this));
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

    @Override
    public Handler<?> getBy(Object key) {
        for (int i = elementHandlers.size(); i <= (int) key; i++)
            elementHandlers.add(new Handler<>(new SuggestedValueProducer<>(() -> null), this));
        return elementHandlers.get((int) key);
    }

    @Override
    protected void changeBy(Object key, Producer<T> producer) {
        elementHandlers.set((Integer) key, new Handler<>(producer, this));
    }
}
