package com.github.leeonky.jfactory;

import com.github.leeonky.util.BeanClass;

import java.util.*;
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
    public Handler<?> getByIndex(List<Object> index) {
        LinkedList<Object> leftProperty = new LinkedList<>(index);
        Handler<?> handler = getHandler((int) leftProperty.removeFirst());
        return leftProperty.isEmpty() ?
                handler
                : handler.get().getByIndex(leftProperty);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void changeByIndex(List<Object> index, Producer<?> producer) {
        LinkedList<Object> leftProperty = new LinkedList<>(index);
        int i = (int) leftProperty.removeFirst();
        Handler<?> handler = getHandler(i);
        if (leftProperty.isEmpty()) {
            if (handler == null)
                elementHandlers.set(i, new Handler<>(producer, this));
            else
                handler.changeProducer((Producer) producer);
        } else
            handler.get().changeByIndex(leftProperty, producer);
    }

    private Handler<?> getHandler(int index) {
        for (int i = elementHandlers.size(); i <= index; i++)
            elementHandlers.add(new Handler<>(new SuggestedValueProducer<>(() -> null), this));
        return elementHandlers.get(index);
    }
}
